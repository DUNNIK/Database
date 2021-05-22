package com.itmo.java.basics.console.impl;

import com.itmo.java.basics.console.DatabaseCommand;
import com.itmo.java.basics.console.DatabaseCommandArgPositions;
import com.itmo.java.basics.console.DatabaseCommandResult;
import com.itmo.java.basics.console.ExecutionEnvironment;
import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.protocol.model.RespObject;

import java.util.List;

/**
 * Команда для создания удаления значения по ключу
 */
public class DeleteKeyCommand implements DatabaseCommand {
    private final ExecutionEnvironment environment;
    private final List<RespObject> commandArgs;
    private String dbName;
    private String tableName;
    private String key;
    public static final int COMMAND_SIZE = 5;
    /**
     * Создает команду.
     * <br/>
     * Обратите внимание, что в конструкторе нет логики проверки валидности данных. Не проверяется, можно ли исполнить команду. Только формальные признаки (например, количество переданных значений или ненуловость объектов
     *
     * @param env         env
     * @param commandArgs аргументы для создания (порядок - {@link DatabaseCommandArgPositions}.
     *                    Id команды, имя команды, имя бд, таблицы, ключ
     * @throws IllegalArgumentException если передано неправильное количество аргументов
     */
    public DeleteKeyCommand(ExecutionEnvironment env, List<RespObject> commandArgs) {
        if (isNotValidArgumentsCount(commandArgs)) {
            throw new IllegalArgumentException("When creating the database, an incorrect number of arguments was passed.\n" +
                    "Your number of arguments:\n" + commandArgs.size() +
                    "The required number of arguments:" + COMMAND_SIZE);
        }
        environment = env;
        this.commandArgs = commandArgs;
    }

    /**
     * Удаляет значение по ключу
     *
     * @return {@link DatabaseCommandResult#success(byte[])} с удаленным значением. Например, "previous"
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
            var deleteOptionalValue = database.read(tableName, key);
            if (deleteOptionalValue.isEmpty()) {
                return DatabaseCommandResult.error("There is no value to delete");
            }
            var deleteValue = deleteOptionalValue.get();
            database.delete(tableName, key);
            return DatabaseCommandResult.success(deleteValue);
        } catch (DatabaseException e) {
            return DatabaseCommandResult.error(e);
        }
    }

    private void parseCommandArgs() throws DatabaseException {
        try {
            dbName = commandArgs.get(DatabaseCommandArgPositions.DATABASE_NAME.getPositionIndex()).asString();
            tableName = commandArgs.get(DatabaseCommandArgPositions.TABLE_NAME.getPositionIndex()).asString();
            key = commandArgs.get(DatabaseCommandArgPositions.KEY.getPositionIndex()).asString();
        } catch (Exception e) {
            throw new DatabaseException("An error occurred while parsing the command", e);
        }
    }

    private boolean isNotValidArgumentsCount(List<RespObject> commandArgs) {
        return commandArgs.size() != COMMAND_SIZE;
    }
}
