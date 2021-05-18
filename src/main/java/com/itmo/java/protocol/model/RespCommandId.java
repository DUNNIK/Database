package com.itmo.java.protocol.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        var respOutput = createOutputStreamBytes();
        writeBytesInOutputStream(respOutput, os);
    }

    private void writeBytesInOutputStream(ByteArrayOutputStream respOutput, OutputStream os) throws IOException {
        try {
            os.write(respOutput.toByteArray());
            //respOutput.writeTo(os);
        } catch (IOException e){
            throw new IOException("An error occurred while writing RespCommandId with commandId: " + commandId, e);
        }
    }

    private ByteArrayOutputStream createOutputStreamBytes() throws IOException {
        var bytes = new ByteArrayOutputStream();
        try {
            bytes.write(CODE);
            bytes.write(Integer.toString(commandId).getBytes(StandardCharsets.UTF_8));
            bytes.write(CRLF);
        } catch (IOException e) {
            throw new IOException("Error creating a byte record RESP RespCommandId with commandId:" + commandId, e);
        }
        return bytes;
    }
    private String convertToString(){
        return String.valueOf(commandId);
    }
}
