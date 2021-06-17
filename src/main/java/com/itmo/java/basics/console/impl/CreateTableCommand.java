package com.itmo.java.basics.console.impl;

import com.itmo.java.basics.console.DatabaseCommand;
import com.itmo.java.basics.console.DatabaseCommandArgPositions;
import com.itmo.java.basics.console.DatabaseCommandResult;
import com.itmo.java.basics.console.ExecutionEnvironment;
import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.protocol.model.RespObject;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Команда для создания базы таблицы
 */
public class CreateTableCommand implements DatabaseCommand {
    private final ExecutionEnvironment environment;
    private final List<RespObject> commandArgs;
    private String dbName;
    private String tableName;
    public static final int COMMAND_SIZE = 4;
    /**
     * Создает команду
     * <br/>
     * Обратите внимание, что в конструкторе нет логики проверки валидности данных. Не проверяется, можно ли исполнить команду. Только формальные признаки (например, количество переданных значений или ненуловость объектов
     *
     * @param env         env
     * @param commandArgs аргументы для создания (порядок - {@link DatabaseCommandArgPositions}.
     *                    Id команды, имя команды, имя бд, имя таблицы
     * @throws IllegalArgumentException если передано неправильное количество аргументов
     */
    public CreateTableCommand(ExecutionEnvironment env, List<RespObject> commandArgs) {
        if (isNotValidArgumentsCount(commandArgs)) {
            throw new IllegalArgumentException("When creating the database, an incorrect number of arguments was passed.\n" +
                    "Your number of arguments:\n" + commandArgs.size() +
                    "The required number of arguments:" + COMMAND_SIZE);
        }
        environment = env;
        this.commandArgs = commandArgs;
    }

    /**
     * Создает таблицу в нужной бд
     *
     * @return {@link DatabaseCommandResult#success(byte[])} с сообщением о том, что заданная таблица была создана. Например, "Table table1 in database db1 created"
     */
    @Override
    public DatabaseCommandResult execute() {
        try {
            parseCommandArgs();
            var databaseOptional = environment.getDatabase(dbName);
            if (databaseOptional.isEmpty()) {
                return DatabaseCommandResult.error("This database is not present in the environment");
            }
            var database = databaseOptional.get();
            database.createTableIfNotExists(tableName);
        } catch (DatabaseException e) {
            return DatabaseCommandResult.error(e);
        }
        var message = "Table " + tableName + " in Database " + dbName + " was created";
        return DatabaseCommandResult.success(message.getBytes(StandardCharsets.UTF_8));
    }

    private void parseCommandArgs() throws DatabaseException {
        try {
            dbName = commandArgs.get(DatabaseCommandArgPositions.DATABASE_NAME.getPositionIndex()).asString();
            tableName = commandArgs.get(DatabaseCommandArgPositions.TABLE_NAME.getPositionIndex()).asString();
        } catch (Exception e) {
            throw new DatabaseException("An error occurred while parsing the command", e);
        }
    }

    private boolean isNotValidArgumentsCount(List<RespObject> commandArgs) {
        return commandArgs.size() != COMMAND_SIZE;
    }
}
