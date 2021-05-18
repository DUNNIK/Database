package com.itmo.java.protocol.model;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Id
 */
public class RespCommandId implements RespObject {

    /**
     * Код объекта
     */
    public static final byte CODE = '!';

    private final int commandId;

    public RespCommandId(int commandId) {
        this.commandId = commandId;
    }

    /**
     * Ошибка ли это? Ответ - нет
     *
     * @return false
     */
    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public String asString() {
        return convertToString();
    }

    @Override
    public void write(OutputStream os) throws IOException {
        os.write(CODE);
        writeCommandId(os);
        os.write(CRLF);
    }
    private void writeCommandId(OutputStream os) throws IOException {
        var byteInt = ByteBuffer.allocate(4);
        byteInt.putInt(commandId);
        os.write(byteInt.array());
    }
    private String convertToString(){
        return String.valueOf(commandId);
    }
}
