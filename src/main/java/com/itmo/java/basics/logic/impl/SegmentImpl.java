package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.index.SegmentOffsetInfo;
import com.itmo.java.basics.index.impl.SegmentIndex;
import com.itmo.java.basics.index.impl.SegmentOffsetInfoImpl;
import com.itmo.java.basics.logic.Segment;
import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.logic.WritableDatabaseRecord;
import com.itmo.java.basics.logic.io.DatabaseInputStream;
import com.itmo.java.basics.logic.io.DatabaseOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

public class SegmentImpl implements Segment {

    private final String segmentName;
    private final Path segmentPath;
    private final SegmentIndex segmentIndex;
    private static DatabaseOutputStream outputStream;
    private long finalOffset;
    private boolean readonly;

    private SegmentImpl(String segmentName, Path tableRootPath) {
        this.segmentName = segmentName;
        segmentPath = createSegmentPathFromRootPath(tableRootPath, segmentName);
        segmentIndex = new SegmentIndex();
        finalOffset = 0;
        readonly = false;
    }

    private static DataOutputStream createOutputStreamForDataBase(Path segmentPath) throws FileNotFoundException {
        return new DataOutputStream(new FileOutputStream(segmentPath.toString(), true));
    }

    private static Path createSegmentPathFromRootPath(Path segmentRoot, String segmentName) {
        return Path.of(segmentRoot + File.separator + segmentName);
    }

    static Segment create(String segmentName, Path tableRootPath) throws DatabaseException {
        if (segmentName == null || tableRootPath == null) {
            throw new DatabaseException("Error assigning the name and path to the segment.");
        }
        try {
            outputStream = new DatabaseOutputStream(
                    createOutputStreamForDataBase(
                            createSegmentPathFromRootPath(tableRootPath, segmentName)
                    )
            );
        } catch (FileNotFoundException e) {
            throw new DatabaseException("Unable to create segment file.");
        }
        return new SegmentImpl(segmentName, tableRootPath);
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
        AddSegmentIndex(objectKey);
        WritableDatabaseRecord record = createNewRecord(objectKey, objectValue);
        var recordSize = outputStream.write(record);
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

    private void AddSegmentIndex(String objectKey) {
        segmentIndex.onIndexedEntityUpdated(objectKey, new SegmentOffsetInfoImpl(finalOffset));
    }

    private boolean isWriteNotPossible() {
        var maxSegmentSize = 100_000;
        return maxSegmentSize <= finalOffset;
    }

    @Override
    public Optional<byte[]> read(String objectKey) throws IOException {

        long offset;
        if (searchOffsetByKey(objectKey).isPresent()) {
            DatabaseInputStream inputStream = new DatabaseInputStream(createInputStreamForDataBase());
            offset = searchOffsetByKey(objectKey).get().getOffset();
            long skip = inputStream.skip(offset);

            if (!isSkipWasCorrect(offset, skip)) {
                throw new IOException();
            }

            var unit = inputStream.readDbUnit();

            if (unit.isPresent() && unit.get().getValue() != null) {
                return Optional.ofNullable(unit.get().getValue());
            }
            inputStream.close();
        }
        return Optional.empty();
    }

    private boolean isSkipWasCorrect(long offset, long skip) {
        return skip == offset;
    }

    private Optional<SegmentOffsetInfo> searchOffsetByKey(String objectKey) {
        return segmentIndex.searchForKey(objectKey);
    }

    private DataInputStream createInputStreamForDataBase() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(segmentPath.toString());

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
