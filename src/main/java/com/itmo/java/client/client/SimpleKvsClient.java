package com.itmo.java.client.client;

import com.itmo.java.client.command.*;
import com.itmo.java.client.connection.KvsConnection;
import com.itmo.java.client.exception.ConnectionException;
import com.itmo.java.client.exception.DatabaseExecutionException;
import com.itmo.java.protocol.model.RespObject;

import java.util.function.Supplier;

public class SimpleKvsClient implements KvsClient {

    private final String databaseName;
    private final Supplier<KvsConnection> connectionSupplier;

    /**
     * Конструктор
     *
     * @param databaseName       имя базы, с которой работает
     * @param connectionSupplier метод создания подключения к базе
     */
    public SimpleKvsClient(String databaseName, Supplier<KvsConnection> connectionSupplier) {
        if (databaseName == null || connectionSupplier == null) {
            throw new IllegalArgumentException("Exception when creating a SimpleKvsClient object. The passed arguments cannot be null.");
        }
        this.databaseName = databaseName;
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public String createDatabase() throws DatabaseExecutionException {
        var databaseKvsCommand = new CreateDatabaseKvsCommand(databaseName);
        RespObject executionResult;
        try {
            var commandId = databaseKvsCommand.getCommandId();
            var respCommand = databaseKvsCommand.serialize();
            executionResult = connectionSupplier.get().send(commandId, respCommand);
            ifErrorThrowException(executionResult);
        } catch (ConnectionException e) {
            throw new DatabaseExecutionException("Error when calling the createDatabase command" + e);
        }
        return executionResult.asString();
    }

    private void ifErrorThrowException(RespObject executionResult) throws DatabaseExecutionException {
        if (executionResult.isError()) {
            throw new DatabaseExecutionException(executionResult.asString());
        }
    }

    @Override
    public String createTable(String tableName) throws DatabaseExecutionException {
        var tableKvsCommand = new CreateTableKvsCommand(databaseName, tableName);
        RespObject executionResult;
        try {
            var commandId = tableKvsCommand.getCommandId();
            var respCommand = tableKvsCommand.serialize();
            executionResult = connectionSupplier.get().send(commandId, respCommand);
            ifErrorThrowException(executionResult);
        } catch (ConnectionException e) {
            throw new DatabaseExecutionException("Error when calling the createTable command", e);
        }
        return executionResult.asString();
    }

    @Override
    public String get(String tableName, String key) throws DatabaseExecutionException {
        var getKvsCommand = new GetKvsCommand(databaseName, tableName, key);
        RespObject executionResult;
        try {
            var commandId = getKvsCommand.getCommandId();
            var respCommand = getKvsCommand.serialize();
            executionResult = connectionSupplier.get().send(commandId, respCommand);
            ifErrorThrowException(executionResult);
        } catch (ConnectionException e) {
            throw new DatabaseExecutionException("Error when calling the get command", e);
        }
        return executionResult.asString();
    }

    @Override
    public String set(String tableName, String key, String value) throws DatabaseExecutionException {
        var setKvsCommand = new SetKvsCommand(databaseName, tableName, key, value);
        RespObject executionResult;
        try {
            var commandId = setKvsCommand.getCommandId();
            var respCommand = setKvsCommand.serialize();
            executionResult = connectionSupplier.get().send(commandId, respCommand);
            ifErrorThrowException(executionResult);
        } catch (ConnectionException e) {
            throw new DatabaseExecutionException("Error when calling the set command", e);
        }
        return executionResult.asString();
    }

    @Override
    public String delete(String tableName, String key) throws DatabaseExecutionException {
        var deleteKvsCommand = new DeleteKvsCommand(databaseName, tableName, key);
        RespObject executionResult;
        try {
            var commandId = deleteKvsCommand.getCommandId();
            var respCommand = deleteKvsCommand.serialize();
            executionResult = connectionSupplier.get().send(commandId, respCommand);
            ifErrorThrowException(executionResult);
        } catch (ConnectionException e) {
            throw new DatabaseExecutionException("Error when calling the delete command", e);
        }
        return executionResult.asString();
    }
}
