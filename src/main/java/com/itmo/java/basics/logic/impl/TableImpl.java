package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.index.impl.TableIndex;
import com.itmo.java.basics.logic.Segment;
import com.itmo.java.basics.logic.Table;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class TableImpl implements Table {
    private final String tableName;
    private final Path tablePath;
    private final TableIndex tableIndex;
    private Segment lastSegment;

    private TableImpl(String tableName, Path pathToDatabaseRoot, TableIndex tableIndex) {
        this.tableName = tableName;
        tablePath = createTablePathFromRootPath(pathToDatabaseRoot, tableName);
        this.tableIndex = tableIndex;
    }

    static Table create(String tableName, Path pathToDatabaseRoot, TableIndex tableIndex) throws DatabaseException {
        if (tableName == null || pathToDatabaseRoot == null) {
            throw new DatabaseException("Error assigning the name and path to the table.");
        }
        makeTableDir(createTablePathFromRootPath(pathToDatabaseRoot, tableName));
        return new TableImpl(tableName, pathToDatabaseRoot, tableIndex);
    }

    private static Path createTablePathFromRootPath(Path tableRoot, String tableName) {
        return Path.of(tableRoot + File.separator + tableName);
    }

    private static void makeTableDir(Path tablePath) throws DatabaseException {
        try {
            Files.createDirectory(tablePath);
        } catch (IOException e) {
            throw new DatabaseException("IO: Directory creation error.", e);
        }
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    public void write(String objectKey, byte[] objectValue) throws DatabaseException {
        createSegmentIfNull();

        if (lastSegment.isReadOnly()) {
            createSegmentIfFull();
        }

        try {
            lastSegment.write(objectKey, objectValue);
        } catch (IOException e) {
            throw new DatabaseException("Error writing record.", e);
        }
        tableIndex.onIndexedEntityUpdated(objectKey, lastSegment);
    }

    private void createSegmentIfFull() throws DatabaseException {
        lastSegment = SegmentImpl.create(SegmentImpl.createSegmentName(tableName), tablePath);
    }

    private void createSegmentIfNull() throws DatabaseException {
        if (lastSegment == null) {
            lastSegment = SegmentImpl.create(SegmentImpl.createSegmentName(tableName), tablePath);
        }
    }

    private Optional<Segment> searchSegment(String objectKey) {
        return tableIndex.searchForKey(objectKey);
    }

    @Override
    public Optional<byte[]> read(String objectKey) throws DatabaseException {
        var segment = searchSegment(objectKey);

        if (segment.isPresent()) {
            try {
                return segment.get().read(objectKey);
            } catch (IOException e) {
                throw new DatabaseException("Read Write error.", e);
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String objectKey) throws DatabaseException {
        var segment = searchSegment(objectKey);

        if (segment.isEmpty()) {
            throw new DatabaseException("Segment not found.");
        }

        createSegmentIfNull();

        if (lastSegment.isReadOnly()) {
            createSegmentIfFull();
        }

        try {
            lastSegment.delete(objectKey);
        } catch (IOException e) {
            throw new DatabaseException("Error deleting a record.", e);
        }

        tableIndex.onIndexedEntityUpdated(objectKey, lastSegment);
    }
}
