package com.itmo.java.basics.logic.impl;

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
import java.security.KeyException;
import java.util.Optional;

public class SegmentImpl implements Segment {

    private final String _segmentName;
    private final Path _segmentPath;
    private final SegmentIndex _segmentIndex;
    private final DatabaseOutputStream _outputStream;
    private long _finalOffset;
    private boolean _readonly;

    private SegmentImpl(String segmentName, Path tableRootPath) throws FileNotFoundException {
        _segmentName = segmentName;
        _segmentPath = createSegmentPathFromRootPath(tableRootPath);
        _segmentIndex = new SegmentIndex();
        _outputStream = new DatabaseOutputStream(createOutputStreamForDataBase());
        _finalOffset = 0;
        _readonly = false;
    }

    private DataOutputStream createOutputStreamForDataBase() throws FileNotFoundException {
        return new DataOutputStream(new FileOutputStream(_segmentPath.toString(), true));
    }

    private Path createSegmentPathFromRootPath(Path segmentRoot) {
        return Path.of(segmentRoot + "\\" + _segmentName);
    }

    static Segment create(String segmentName, Path tableRootPath) throws DatabaseException {
        try {
            return new SegmentImpl(segmentName, tableRootPath);
        } catch (FileNotFoundException e) {
            throw new DatabaseException(e);
        }
    }

    static String createSegmentName(String tableName) {
        return tableName + "_" + System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return _segmentName;
    }

    @Override
    public boolean write(String objectKey, byte[] objectValue) throws IOException {
        if (!isWritePossible()) {
            closeFileForWriting();
            return false;
        }
        AddSegmentIndex(objectKey);
        WritableDatabaseRecord record = new SetDatabaseRecord(objectKey.getBytes(StandardCharsets.UTF_8), objectValue);
        var recordSize = _outputStream.write(record);
        updateFinalOffset(recordSize);
        return true;
    }

    private void closeFileForWriting() throws IOException {
        _outputStream.close();
        _readonly = true;
    }

    private void updateFinalOffset(int recordSize) {
        _finalOffset += recordSize;
    }

    private void AddSegmentIndex(String objectKey) {
        _segmentIndex.onIndexedEntityUpdated(objectKey, new SegmentOffsetInfoImpl(_finalOffset));
    }

    private boolean isWritePossible() {
        var maxSegmentSize = 100_000;
        return maxSegmentSize >= _finalOffset;
    }

    @Override
    public Optional<byte[]> read(String objectKey) throws IOException {
        DatabaseInputStream inputStream = new DatabaseInputStream(createInputStreamForDataBase());
        long offset;
        try {
            offset = searchOffsetByKey(objectKey);
        } catch (KeyException e) {
            throw new IOException(e);
        }
        long skip = inputStream.skip(offset);
        if (!isSkipWasCorrect(offset, skip)) throw new IOException();
        byte[] value;
        var unit = inputStream.readDbUnit();
        if (unit.isPresent()) {
            value = unit.get().getValue();
        } else {
            inputStream.close();
            return Optional.empty();
        }
        inputStream.close();
        return Optional.ofNullable(value);
    }

    private boolean isSkipWasCorrect(long offset, long skip) {
        return skip == offset;
    }

    private long searchOffsetByKey(String objectKey) throws KeyException {
        return _segmentIndex.searchForKey(objectKey).orElseThrow(KeyException::new).getOffset();
    }

    private DataInputStream createInputStreamForDataBase() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(_segmentPath.toString());

        return new DataInputStream(fileInputStream);
    }

    @Override
    public boolean isReadOnly() {
        return _readonly;
    }

    @Override
    public boolean delete(String objectKey) throws IOException {
        AddSegmentIndex(objectKey);
        WritableDatabaseRecord record = new RemoveDatabaseRecord(objectKey.getBytes(StandardCharsets.UTF_8));
        var recordSize = _outputStream.write(record);
        updateFinalOffset(recordSize);
        return false;
    }
}
