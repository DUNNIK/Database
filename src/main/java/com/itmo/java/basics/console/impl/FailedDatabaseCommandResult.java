package com.itmo.java.basics.console.impl;

import com.itmo.java.basics.console.DatabaseCommandResult;
import com.itmo.java.protocol.model.RespError;
import com.itmo.java.protocol.model.RespObject;

import java.nio.charset.StandardCharsets;

/**
 * Зафейленная команда
 */
public class FailedDatabaseCommandResult implements DatabaseCommandResult {
    private final String payload;

    public FailedDatabaseCommandResult(String payload) {
        this.payload = payload;
    }

    /**
     * Сообщение об ошибке
     */
    @Override
    public String getPayLoad() {
        //TODO implement
        return null;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    /**
     * Сериализуется в {@link RespError}
     */
    @Override
    public RespObject serialize() {
        return new RespError(convertPayloadToByte());
    }
    private byte[] convertPayloadToByte(){
        return payload.getBytes(StandardCharsets.UTF_8);
    }
}
