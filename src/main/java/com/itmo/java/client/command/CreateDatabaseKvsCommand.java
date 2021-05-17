package com.itmo.java.client.command;

import com.itmo.java.protocol.model.RespArray;
import com.itmo.java.protocol.model.RespBulkString;
import com.itmo.java.protocol.model.RespCommandId;

import java.nio.charset.StandardCharsets;

/**
 * Команда для создания бд
 */
public class CreateDatabaseKvsCommand implements KvsCommand {
    private static final String COMMAND_NAME = "CREATE_DATABASE";

    private final int id;
    private final String databaseName;
    /**
     * Создает объект
     *
     * @param databaseName имя базы данных
     */
    public CreateDatabaseKvsCommand(String databaseName) {
        this.databaseName = databaseName;
        id = idGen.getAndIncrement();
    }

    /**
     * Возвращает RESP объект. {@link RespArray} с {@link RespCommandId}, именем команды, аргументами в виде {@link RespBulkString}
     *
     * @return объект
     */
    @Override
    public RespArray serialize() {
        var commandIdResp = new RespCommandId(id);
        var commandNameResp = new RespBulkString(COMMAND_NAME.getBytes(StandardCharsets.UTF_8));
        var databaseNameResp = new RespBulkString(databaseName.getBytes(StandardCharsets.UTF_8));
        return new RespArray(commandIdResp, commandNameResp, databaseNameResp);
    }

    @Override
    public int getCommandId() {
        return id;
    }
}
