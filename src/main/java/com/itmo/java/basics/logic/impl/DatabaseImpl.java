package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.index.impl.TableIndex;
import com.itmo.java.basics.initialization.DatabaseInitializationContext;
import com.itmo.java.basics.logic.Database;
import com.itmo.java.basics.logic.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Builder
@AllArgsConstructor
public class DatabaseImpl implements Database {

    private final String dbName;
    private final Path databasePath;
    private final Map<String, Table> tables;

    private DatabaseImpl(String dbName, Path databaseRoot) {
        this.dbName = dbName;
        databasePath = createDatabasePathFromRootPath(databaseRoot, dbName);
        tables = new HashMap<>();
    }

    private static Path createDatabasePathFromRootPath(Path databaseRoot, String dbName) {
        return Path.of(databaseRoot.toString() + File.separator +
                dbName);
    }

    public static Database create(String dbName, Path databaseRoot) throws DatabaseException {
        if (dbName == null || databaseRoot == null) {
            throw new DatabaseException("Error assigning the name and path to the database.");
        }
        makeDatabaseDir(createDatabasePathFromRootPath(databaseRoot, dbName));
        return new DatabaseImpl(dbName, databaseRoot);
    }

    public static Database initializeFromContext(DatabaseInitializationContext context) {

        return DatabaseImpl.builder()
                .dbName(context.getDbName())
                .databasePath(context.getDatabasePath())
                .tables(context.getTables())
                .build();
    }

    @Override
    public String getName() {
        return dbName;
    }

    @Override
    public void createTableIfNotExists(String tableName) throws DatabaseException {
        if (tables.containsKey(tableName)) {
            throw new DatabaseException("The specified key is not in the database.");
        }
        tables.put(tableName, createTable(tableName));
    }

    private Table createTable(String tableName) throws DatabaseException {
        return TableImpl.create(tableName, databasePath, new TableIndex());
    }

    private static void makeDatabaseDir(Path databasePath) throws DatabaseException {
        try {
            Files.createDirectory(databasePath);
        } catch (IOException e) {
            throw new DatabaseException("IO: Directory creation error.", e);
        }
    }

    @Override
    public void write(String tableName, String objectKey, byte[] objectValue) throws DatabaseException {
        Table table;
        try {
            table = searchTable(tableName);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
        table.write(objectKey, objectValue);
    }

    private Table searchTable(String tableName) throws DatabaseException {
        if (tables.containsKey(tableName)) {
            return tables.get(tableName);
        }
        throw new DatabaseException("The table for the specified key does not exist.");
    }

    @Override
    public Optional<byte[]> read(String tableName, String objectKey) throws DatabaseException {
        Table table;
        if (tableName == null) {
            throw new DatabaseException("The value of the table cannot be null.");
        }
        table = searchTable(tableName);
        return table.read(objectKey);
    }

    @Override
    public void delete(String tableName, String objectKey) throws DatabaseException {
        Table table;
        if (tableName == null) {
            throw new DatabaseException("The value of the table cannot be null.");
        }
        table = searchTable(tableName);
        table.delete(objectKey);
    }

}