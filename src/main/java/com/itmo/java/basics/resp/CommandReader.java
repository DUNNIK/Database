package com.itmo.java.basics.resp;

import com.itmo.java.basics.console.DatabaseCommand;
import com.itmo.java.basics.console.DatabaseCommandArgPositions;
import com.itmo.java.basics.console.DatabaseCommands;
import com.itmo.java.basics.console.ExecutionEnvironment;
import com.itmo.java.protocol.RespReader;
import com.itmo.java.protocol.model.RespObject;

import java.io.IOException;

public class CommandReader implements AutoCloseable {
    private final RespReader reader;
    private final ExecutionEnvironment environment;

    public CommandReader(RespReader reader, ExecutionEnvironment env) {
        this.reader = reader;
        this.environment = env;
    }

    /**
     * Есть ли следующая команда в ридере?
     */
    public boolean hasNextCommand() throws IOException {
        return reader.hasArray();
    }

    /**
     * Считывает комманду с помощью ридера и возвращает ее
     *
     * @throws IllegalArgumentException если нет имени команды и id
     */
    public DatabaseCommand readCommand() throws IOException {
        var commandArray = reader.readArray();
        RespObject commandName;
        try {
            commandName = commandArray.getObjects().get(DatabaseCommandArgPositions.COMMAND_NAME.getPositionIndex());
        } catch (Exception e) {
            throw new IllegalArgumentException("An error occurred. No command name or command id", e);
        }
        var databaseCommands = DatabaseCommands.valueOf(commandName.asString());
        return databaseCommands.getCommand(environment, commandArray.getObjects());
    }

    @Override
    public void close() throws Exception {
        reader.close();
    }
}
