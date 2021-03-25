package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.index.impl.TableIndex;
import com.itmo.java.basics.logic.Segment;
import com.itmo.java.basics.logic.Table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class TableImpl implements Table {
    private final String _tableName;
    private final Path _tablePath;
    private final TableIndex _tableIndex;
    private Segment _lastSegment;

    private TableImpl(String tableName, Path pathToDatabaseRoot, TableIndex tableIndex) throws DatabaseException {
        if (tableName == null || pathToDatabaseRoot == null) throw new DatabaseException("Message");
        _tableName = tableName;
        _tablePath = createTablePathFromRootPath(pathToDatabaseRoot);
        _tableIndex = tableIndex;
        makeTableDir();
    }

    static Table create(String tableName, Path pathToDatabaseRoot, TableIndex tableIndex) throws DatabaseException {
        return new TableImpl(tableName, pathToDatabaseRoot, tableIndex);
    }

    private Path createTablePathFromRootPath(Path tableRoot) {
        return Path.of(tableRoot + "\\" + _tableName);
    }

    private void makeTableDir() throws DatabaseException {
        try {
            Files.createDirectory(_tablePath);
        } catch (IOException e) {
            throw new DatabaseException("Message", e);
        }
    }

    @Override
    public String getName() {
        return _tableName;
    }

    @Override
    public void write(String objectKey, byte[] objectValue) throws DatabaseException {
        createSegmentIfNull(objectKey);
        boolean isWrite;
        try {
            isWrite = _lastSegment.write(objectKey, objectValue);
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
        writeIfFull(objectKey, objectValue, isWrite);
    }

    private void writeIfFull(String objectKey, byte[] objectValue, boolean isWrite) throws DatabaseException {
        if (!isWrite) {
            createSegmentIfFull(objectKey);
            try {
                _lastSegment.write(objectKey, objectValue);
            } catch (IOException e) {
                throw new DatabaseException(e);
            }
        }
    }

    private void createSegmentIfFull(String objectKey) throws DatabaseException {
        _lastSegment = SegmentImpl.create(SegmentImpl.createSegmentName(_tableName), _tablePath);
        _tableIndex.onIndexedEntityUpdated(objectKey, _lastSegment);
    }

    private void createSegmentIfNull(String objectKey) throws DatabaseException {
        if (_lastSegment == null) {
            _lastSegment = SegmentImpl.create(SegmentImpl.createSegmentName(_tableName), _tablePath);
            _tableIndex.onIndexedEntityUpdated(objectKey, _lastSegment);
        }
    }

    private Optional<Segment> searchSegment(String objectKey) {
        return _tableIndex.searchForKey(objectKey);
    }

    @Override
    public Optional<byte[]> read(String objectKey) throws DatabaseException {
        var segment = searchSegment(objectKey);
        Optional<byte[]> value = Optional.empty();
        try {
            if (segment.isPresent()) value = segment.get().read(objectKey);
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
        return value;
    }

    @Override
    public void delete(String objectKey) throws DatabaseException {
        var segment = searchSegment(objectKey);
        boolean isDelete = false;
        try {
            if (segment.isPresent()){
                isDelete = segment.get().delete(objectKey);
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
        deleteIfFull(objectKey, isDelete);
    }

    private void deleteIfFull(String objectKey, boolean isDelete) throws DatabaseException {
        if (!isDelete) {
            createSegmentIfFull(objectKey);
            try {
                _lastSegment.delete(objectKey);
            } catch (IOException e) {
                throw new DatabaseException(e);
            }
        }
    }
}
