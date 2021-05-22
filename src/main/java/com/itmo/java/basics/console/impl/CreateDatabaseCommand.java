package com.itmo.java.basics.console.impl;

import com.itmo.java.basics.console.DatabaseCommand;
import com.itmo.java.basics.console.DatabaseCommandArgPositions;
import com.itmo.java.basics.console.DatabaseCommandResult;
import com.itmo.java.basics.console.ExecutionEnvironment;
import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.logic.DatabaseFactory;
import com.itmo.java.protocol.model.RespObject;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Команда для создания базы данных
 */
public class CreateDatabaseCommand implements DatabaseCommand {
    private final ExecutionEnvironment environment;
    private final DatabaseFactory factory;
    private final List<RespObject> commandArgs;
    private String dbName;
    public static final int COMMAND_SIZE = 3;
    /**
     * Создает команду.
     * <br/>
     * Обратите внимание, что в конструкторе нет логики проверки валидности данных. Не проверяется, можно ли исполнить команду. Только формальные признаки (например, количество переданных значений или ненуловость объектов
     *
     * @param env         env
     * @param factory     функция создания базы данных (пример: DatabaseImpl::create)
     * @param commandArgs аргументы для создания (порядок - {@link DatabaseCommandArgPositions}.
     *                    Id команды, имя команды, имя создаваемой бд
     * @throws IllegalArgumentException если передано неправильное количество аргументов
     */
    public CreateDatabaseCommand(ExecutionEnvironment env, DatabaseFactory factory, List<RespObject> commandArgs) {
        if (isNotValidArgumentsCount(commandArgs)) {
            throw new IllegalArgumentException("When creating the database, an incorrect number of arguments was passed.\n" +
                    "Your number of arguments:\n" + commandArgs.size() +
                    "The required number of arguments:" + COMMAND_SIZE);
        }
        environment = env;
        this.factory = factory;
        this.commandArgs = commandArgs;
    }

    /**
     * Создает бд в нужном env
     *
     * @return {@link DatabaseCommandResult#success(byte[])} с сообщением о том, что заданная база была создана. Например, "Database db1 created"
     */
    @Override
    public DatabaseCommandResult execute() {
        try {
            parseCommandArgs();
            var database = factory.createNonExistent(dbName, environment.getWorkingPath());
            environment.addDatabase(database);
        } catch (DatabaseException e) {
            return DatabaseCommandResult.error(e);
        }
        var message = "Database" + dbName + "was created";
        return DatabaseCommandResult.success(message.getBytes(StandardCharsets.UTF_8));
    }

    private void parseCommandArgs() throws DatabaseException {
        if (isDataNotValid()) {
            throw new DatabaseException("The DatabaseCommand has invalid arguments");
        }
        try {
            dbName = commandArgs.get(DatabaseCommandArgPositions.DATABASE_NAME.getPositionIndex()).asString();
        } catch (Exception e) {
            throw new DatabaseException("An error occurred while parsing the command", e);
        }
    }

    private boolean isNotValidArgumentsCount(List<RespObject> commandArgs) {
        return commandArgs.size() != COMMAND_SIZE;
    }

    private boolean isDataNotValid() {
        return environment == null || factory == null;
    }
}
