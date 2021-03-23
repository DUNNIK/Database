package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.index.impl.TableIndex;
import com.itmo.java.basics.logic.Table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class TableImpl implements Table {
    private String _tableName;
    private Path _tablePath;
    private TableIndex _tableIndex;

    private TableImpl(String tableName, Path pathToDatabaseRoot, TableIndex tableIndex) throws DatabaseException {
        _tableName = tableName;
        _tablePath = createTablePathFromRootPath(pathToDatabaseRoot);
        _tableIndex = tableIndex;
        makeTableDir();
    }

    static Table create(String tableName, Path pathToDatabaseRoot, TableIndex tableIndex) throws DatabaseException{
        return new TableImpl(tableName, pathToDatabaseRoot, tableIndex);
    }

    private Path createTablePathFromRootPath(Path tableRoot){
        return Path.of(tableRoot + "\\" + _tableName);
    }

    private void makeTableDir() throws DatabaseException {
        try {
            Files.createDirectory(_tablePath);
        } catch (IOException e) {
            throw new DatabaseException("Directory creation error!");
        }
    }

    @Override
    public String getName() {
        return _tableName;
    }

    @Override
    public void write(String objectKey, byte[] objectValue) throws DatabaseException {

    }

    @Override
    public Optional<byte[]> read(String objectKey) throws DatabaseException {
        return Optional.empty();
    }

    @Override
    public void delete(String objectKey) throws DatabaseException {

    }
}
