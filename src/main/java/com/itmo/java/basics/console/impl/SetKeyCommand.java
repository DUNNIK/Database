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
 * Команда для создания записи значения
 */
public class SetKeyCommand implements DatabaseCommand {
    private final ExecutionEnvironment environment;
    private final List<RespObject> commandArgs;
    private String dbName;
    private String tableName;
    private String key;
    private String value;
    public static final int COMMAND_SIZE = 6;

    /**
     * Создает команду.
     * <br/>
     * Обратите внимание, что в конструкторе нет логики проверки валидности данных. Не проверяется, можно ли исполнить команду. Только формальные признаки (например, количество переданных значений или ненуловость объектов
     *
     * @param env         env
     * @param commandArgs аргументы для создания (порядок - {@link DatabaseCommandArgPositions}.
     *                    Id команды, имя команды, имя бд, таблицы, ключ, значение
     * @throws IllegalArgumentException если передано неправильное количество аргументов
     */
    public SetKeyCommand(ExecutionEnvironment env, List<RespObject> commandArgs) {
        if (isNotValidArgumentsCount(commandArgs)) {
            throw new IllegalArgumentException("When creating the database, an incorrect number of arguments was passed.\n" +
                    "Your number of arguments:\n" + commandArgs.size() +
                    "The required number of arguments:" + COMMAND_SIZE);
        }
        environment = env;
        this.commandArgs = commandArgs;
    }

    /**
     * Записывает значение
     *
     * @return {@link DatabaseCommandResult#success(byte[])} c предыдущим значением. Например, "previous" или null, если такого не было
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
            var readOptionalValue = database.read(tableName, key);
            byte[] previousValue = null;
            if (readOptionalValue.isPresent()) {
                previousValue = readOptionalValue.get();
            }
            database.write(tableName, key, value.getBytes(StandardCharsets.UTF_8));
            return DatabaseCommandResult.success(previousValue);
        } catch (DatabaseException e) {
            return DatabaseCommandResult.error(e);
        }
    }

    private void parseCommandArgs() throws DatabaseException {
        try {
            dbName = commandArgs.get(DatabaseCommandArgPositions.DATABASE_NAME.getPositionIndex()).asString();
            tableName = commandArgs.get(DatabaseCommandArgPositions.TABLE_NAME.getPositionIndex()).asString();
            key = commandArgs.get(DatabaseCommandArgPositions.KEY.getPositionIndex()).asString();
            value = commandArgs.get(DatabaseCommandArgPositions.VALUE.getPositionIndex()).asString();
        } catch (Exception e) {
            throw new DatabaseException("An error occurred while parsing the command", e);
        }
    }

    private boolean isNotValidArgumentsCount(List<RespObject> commandArgs) {
        return commandArgs.size() != COMMAND_SIZE;
    }
}
