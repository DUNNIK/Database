package com.itmo.java.client.command;

import com.itmo.java.protocol.model.RespArray;
import com.itmo.java.protocol.model.RespBulkString;
import com.itmo.java.protocol.model.RespCommandId;

import java.nio.charset.StandardCharsets;

public class SetKvsCommand implements KvsCommand {

    private static final String COMMAND_NAME = "SET_KEY";
    private final int id;
    private final String databaseName;
    private final String tableName;
    private final String key;
    private final String value;

    public SetKvsCommand(String databaseName, String tableName, String key, String value) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.key = key;
        this.value = value;
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
        var tableNameResp = new RespBulkString(tableName.getBytes(StandardCharsets.UTF_8));
        var keyResp = new RespBulkString(key.getBytes(StandardCharsets.UTF_8));
        var valueResp = new RespBulkString(value.getBytes(StandardCharsets.UTF_8));
        return new RespArray(commandIdResp, commandNameResp, databaseNameResp, tableNameResp, keyResp, valueResp);
    }

    @Override
    public int getCommandId() {
        return id;
    }
}
