package com.itmo.java.basics;

import com.itmo.java.basics.console.*;
import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.initialization.impl.DatabaseServerInitializer;
import com.itmo.java.basics.initialization.impl.InitializationContextImpl;
import com.itmo.java.protocol.model.RespArray;
import lombok.Builder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Builder
public class DatabaseServer {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ExecutionEnvironment environment;
    private final DatabaseServerInitializer initializer;

    /**
     * Конструктор
     *
     * @param env         env для инициализации. Далее работа происходит с заполненным объектом
     * @param initializer готовый чейн инициализации
     * @throws DatabaseException если произошла ошибка инициализации
     */
    public static DatabaseServer initialize(ExecutionEnvironment env, DatabaseServerInitializer initializer) throws DatabaseException {
        var initializationContext = InitializationContextImpl.builder()
                .executionEnvironment(env)
                .build();
        initializer.perform(initializationContext);
        return DatabaseServer.builder()
                .environment(env)
                .initializer(initializer)
                .build();
    }

    public CompletableFuture<DatabaseCommandResult> executeNextCommand(RespArray message) {
        return CompletableFuture.supplyAsync(() -> {
            var commandName = message.getObjects().get(DatabaseCommandArgPositions.COMMAND_NAME.getPositionIndex());
            var databaseCommands = DatabaseCommands.valueOf(commandName.asString());
            var command = databaseCommands.getCommand(environment, message.getObjects());
            return command.execute();
        }, executorService);
    }

    public CompletableFuture<DatabaseCommandResult> executeNextCommand(DatabaseCommand command) {
        return CompletableFuture.supplyAsync(command::execute);
    }

    public ExecutionEnvironment getEnv() {
        //TODO implement
        return null;
    }
}