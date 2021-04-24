package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.index.SegmentOffsetInfo;
import com.itmo.java.basics.index.impl.SegmentIndex;
import com.itmo.java.basics.index.impl.SegmentOffsetInfoImpl;
import com.itmo.java.basics.initialization.SegmentInitializationContext;
import com.itmo.java.basics.logic.Segment;
import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.logic.WritableDatabaseRecord;
import com.itmo.java.basics.logic.io.DatabaseInputStream;
import com.itmo.java.basics.logic.io.DatabaseOutputStream;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

@Builder
@AllArgsConstructor
public class SegmentImpl implements Segment {
    private static final int MAX_SEGMENT_SIZE = 100_000;
    private final String segmentName;
    private final Path segmentPath;
    private final SegmentIndex segmentIndex;
    private DatabaseOutputStream outputStream;
    private long finalOffset;
    private boolean readonly;

    private SegmentImpl(String segmentName, Path tableRootPath) {
        this.segmentName = segmentName;
        segmentPath = createSegmentPathFromRootPath(tableRootPath);
        segmentIndex = new SegmentIndex();
        finalOffset = 0;
        readonly = false;
    }

    private DataOutputStream createOutputStreamForDataBase() throws FileNotFoundException {
        return new DataOutputStream(new FileOutputStream(segmentPath.toString(), true));
    }

    private Path createSegmentPathFromRootPath(Path segmentRoot) {
        return Path.of(segmentRoot + File.separator + segmentName);
    }

    public static Segment create(String segmentName, Path tableRootPath) throws DatabaseException {
        if (segmentName == null || tableRootPath == null) {
            throw new DatabaseException("Error assigning the name and path to the segment.");
        }
        return new SegmentImpl(segmentName, tableRootPath);
    }

    public static Segment initializeFromContext(SegmentInitializationContext context) {

        return SegmentImpl.builder()
                .segmentName(context.getSegmentName())
                .segmentPath(context.getSegmentPath())
                .segmentIndex(context.getIndex())
                .readonly(readonly(context))
                .finalOffset(context.getCurrentSize())
                .build();
    }

    private static boolean readonly(SegmentInitializationContext context) {
        return (int) context.getCurrentSize() > MAX_SEGMENT_SIZE;
    }

    static String createSegmentName(String tableName) {
        return tableName + "_" + System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return segmentName;
    }

    @Override
    public boolean write(String objectKey, byte[] objectValue) throws IOException {
        if (isReadOnly()) {
            return false;
        }
        addSegmentIndex(objectKey);

        var writableDatabaseRecord = createNewRecord(objectKey, objectValue);

        outputStream = new DatabaseOutputStream(createOutputStreamForDataBase());//Если что-то не будет заходить можно перенести открытие потока в само поле
        var recordSize = outputStream.write(writableDatabaseRecord);
        outputStream.close();
        updateFinalOffset(recordSize);

        if (isWriteNotPossible()) {
            closeFileForWriting();
        }
        return !isReadOnly();
    }

    private WritableDatabaseRecord createNewRecord(String objectKey, byte[] objectValue) {
        if (objectValue == null) {
            return new RemoveDatabaseRecord(objectKey.getBytes(StandardCharsets.UTF_8));
        } else {
            return new SetDatabaseRecord(objectKey.getBytes(StandardCharsets.UTF_8), objectValue);
        }
    }

    private void closeFileForWriting() throws IOException {
        outputStream.close();
        readonly = true;
    }

    private void updateFinalOffset(int recordSize) {
        finalOffset += recordSize;
    }

    private void addSegmentIndex(String objectKey) {
        segmentIndex.onIndexedEntityUpdated(objectKey, new SegmentOffsetInfoImpl(finalOffset));
    }

    private boolean isWriteNotPossible() {
        return MAX_SEGMENT_SIZE <= finalOffset;
    }

    @Override
    public Optional<byte[]> read(String objectKey) throws IOException {

        long offset;
        var offsetInfo = searchOffsetByKey(objectKey);
        if (offsetInfo.isPresent()) {
            try (var inputStream = new DatabaseInputStream(createInputStreamForDatabase())) {
                offset = offsetInfo.get().getOffset();
                long skip = inputStream.skip(offset);
                if (skipIsNotCorrect(offset, skip)) throw new IOException("Unable to indent the file");
                var unit = inputStream.readDbUnit();
                if (unit.isPresent() && unit.get().getValue() != null) {
                    return Optional.ofNullable(unit.get().getValue());
                }
            }
        }
        return Optional.empty();
    }

    private boolean skipIsNotCorrect(long offset, long skip) {
        return skip != offset;
    }

    private Optional<SegmentOffsetInfo> searchOffsetByKey(String objectKey) {
        return segmentIndex.searchForKey(objectKey);
    }

    private DataInputStream createInputStreamForDatabase() throws FileNotFoundException {
        var fileInputStream = new FileInputStream(segmentPath.toString());

        return new DataInputStream(fileInputStream);
    }

    @Override
    public boolean isReadOnly() {
        return readonly;
    }

    @Override
    public boolean delete(String objectKey) throws IOException {
        write(objectKey, null);
        return true;
    }

}
