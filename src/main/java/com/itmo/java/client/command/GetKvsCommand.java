package com.itmo.java.client.command;

import com.itmo.java.protocol.model.RespArray;
import com.itmo.java.protocol.model.RespBulkString;
import com.itmo.java.protocol.model.RespCommandId;

import java.nio.charset.StandardCharsets;

public class GetKvsCommand implements KvsCommand {

    private static final String COMMAND_NAME = "GET_KEY";
    private final int id;
    private final String databaseName;
    private final String tableName;
    private final String key;
    public GetKvsCommand(String databaseName, String tableName, String key) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.key = key;
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
        return new RespArray(commandIdResp, commandNameResp, databaseNameResp, tableNameResp, keyResp);
    }

    @Override
    public int getCommandId() {
        return id;
    }
}
