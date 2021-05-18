package com.itmo.java.protocol.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Сообщение об ошибке в RESP протоколе
 */
public class RespError implements RespObject {

    /**
     * Код объекта
     */
    public static final byte CODE = '-';

    private final byte[] message;

    public RespError(byte[] message) {
        this.message = message;
    }

    /**
     * Ошибка ли это? Ответ - да
     *
     * @return true
     */
    @Override
    public boolean isError() {
        return true;
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
            throw new IOException("An error occurred while writing RespError with message: " + convertToString(), e);
        }
    }

    private ByteArrayOutputStream createOutputStreamBytes() throws IOException {
        var bytes = new ByteArrayOutputStream();
        try {
            bytes.write(CODE);
            bytes.write(message);
            bytes.write(CRLF);
        } catch (IOException e) {
            throw new IOException("Error creating a byte record RESP RespError with message: " + convertToString(), e);
        }
        return bytes;
    }
    private String convertToString(){
        return new String(message, StandardCharsets.UTF_8);
    }
}
