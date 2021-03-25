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
            throw new DatabaseException("IO: Directory creation error.", e);
        }
    }

    @Override
    public String getName() {
        return _tableName;
    }

    @Override
    public void write(String objectKey, byte[] objectValue) throws DatabaseException {
        createSegmentIfNull();
        boolean isWrite;
        try {
            isWrite = _lastSegment.write(objectKey, objectValue);
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
        writeIfFull(objectKey, objectValue, isWrite);
        _tableIndex.onIndexedEntityUpdated(objectKey, _lastSegment);
    }

    private void writeIfFull(String objectKey, byte[] objectValue, boolean isWrite) throws DatabaseException {
        if (!isWrite) {
            createSegmentIfFull();
            try {
                _lastSegment.write(objectKey, objectValue);
            } catch (IOException e) {
                throw new DatabaseException(e);
            }
        }
    }

    private void createSegmentIfFull() throws DatabaseException {
        _lastSegment = SegmentImpl.create(SegmentImpl.createSegmentName(_tableName), _tablePath);
    }

    private void createSegmentIfNull() throws DatabaseException {
        if (_lastSegment == null) {
            _lastSegment = SegmentImpl.create(SegmentImpl.createSegmentName(_tableName), _tablePath);
        }
    }

    private Optional<Segment> searchSegment(String objectKey) {

        return _tableIndex.searchForKey(objectKey);
    }

    @Override
    public Optional<byte[]> read(String objectKey) throws DatabaseException {
        var segment = searchSegment(objectKey);
        try {
            if (segment.isPresent()) {
                return segment.get().read(objectKey);
            } else{
                return Optional.empty();
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void delete(String objectKey) throws DatabaseException {
        var segment = searchSegment(objectKey);

        boolean isDelete;
        try {
            if (segment.isPresent()) {
                isDelete = segment.get().delete(objectKey);
            } else {
                throw new DatabaseException("The table does not have a segment for the specified key");
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
        deleteIfFull(objectKey, isDelete);
    }

    private void deleteIfFull(String objectKey, boolean isDelete) throws DatabaseException {
        if (!isDelete) {
            createSegmentIfFull();
            try {
                _lastSegment.delete(objectKey);
            } catch (IOException e) {
                throw new DatabaseException(e);
            }
        }
    }
}
