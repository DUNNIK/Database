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
        databaseMap = new HashMap<>();
    }

    @Override
    public Optional<Database> getDatabase(String name) {
        if (databaseMap.containsKey(name)) {
            return Optional.of(databaseMap.get(name));
        }
        return Optional.empty();
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
