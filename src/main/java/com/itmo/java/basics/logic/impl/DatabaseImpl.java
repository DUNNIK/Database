package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.index.impl.TableIndex;
import com.itmo.java.basics.logic.Database;
import com.itmo.java.basics.logic.Table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;

public class DatabaseImpl implements Database {

    private final String _dbName;
    private final Path _databasePath;
    private final HashMap<String, Table> _tables;

    private DatabaseImpl(String dbName, Path databaseRoot) throws DatabaseException {
        _dbName = dbName;
        _databasePath = createDatabasePathFromRootPath(databaseRoot);
        _tables = new HashMap<>();
        makeDatabaseDir();
    }

    private Path createDatabasePathFromRootPath(Path databaseRoot) {
        return Path.of(databaseRoot + "\\" + _dbName);
    }

    public static Database create(String dbName, Path databaseRoot) throws DatabaseException, IOException {
        return new DatabaseImpl(dbName, databaseRoot);
    }

    @Override
    public String getName() {
        return _dbName;
    }

    @Override
    public void createTableIfNotExists(String tableName) throws DatabaseException {
        if (!_tables.containsKey(tableName)) {
            _tables.put(tableName, createTable(tableName));
        }
    }

    private Table createTable(String tableName) throws DatabaseException {
        return TableImpl.create(tableName, _databasePath, new TableIndex());
    }

    private void makeDatabaseDir() throws DatabaseException {
        try {
            Files.createDirectory(_databasePath);
        } catch (IOException e) {
            throw new DatabaseException("Message", e);
        }
    }

    @Override
    public void write(String tableName, String objectKey, byte[] objectValue) throws DatabaseException {
        createTableIfNotExists(tableName);
        var table = searchTable(tableName);
        table.write(objectKey, objectValue);
    }

    private Table searchTable(String tableName) {
        return _tables.get(tableName);
    }

    @Override
    public Optional<byte[]> read(String tableName, String objectKey) throws DatabaseException {
        Table table = searchTable(tableName);
        return table.read(objectKey);
    }

    @Override
    public void delete(String tableName, String objectKey) throws DatabaseException {
        Table table = searchTable(tableName);
        table.delete(objectKey);
    }

}