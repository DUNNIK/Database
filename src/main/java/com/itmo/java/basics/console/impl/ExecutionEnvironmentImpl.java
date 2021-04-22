package com.itmo.java.basics.console.impl;

import com.itmo.java.basics.config.DatabaseConfig;
import com.itmo.java.basics.console.ExecutionEnvironment;
import com.itmo.java.basics.logic.Database;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ExecutionEnvironmentImpl implements ExecutionEnvironment {

    private final DatabaseConfig config;
    private final Map<String, Database> databaseMap;

    public ExecutionEnvironmentImpl(DatabaseConfig config) {
        this.config = config;
        databaseMap = new HashMap<String, Database>();
    }

    @Override
    public Optional<Database> getDatabase(String name) {
        var database = databaseMap.get(name);
        return Optional.of(database);
    }

    @Override
    public void addDatabase(Database db) {
        databaseMap.put(db.getName(), db);
    }

    @Override
    public Path getWorkingPath() {
        var stringPath = config.getWorkingPath();
        return Path.of(stringPath);
    }
}
