package com.itmo.java.protocol.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Строка
 */
public class RespBulkString implements RespObject {
    /**
     * Код объекта
     */
    public static final byte CODE = '$';

    public static final int NULL_STRING_SIZE = -1;
    private final byte[] data;
    public RespBulkString(byte[] data) {
        this.data = data;
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

    /**
     * Строковое представление
     *
     * @return строку, если данные есть. Если нет - null
     */
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
            respOutput.writeTo(os);
        } catch (IOException e){
            throw new IOException("An error occurred while writing RespBulkString with data: " + convertToString(), e);
        }
    }
    private ByteArrayOutputStream createOutputStreamBytes() throws IOException {
        var bytes = new ByteArrayOutputStream();
        try {
            bytes.write(CODE);
            bytes.write(data.length);
            bytes.write(CRLF);
            bytes.write(data);
            bytes.write(CRLF);
        } catch (IOException e) {
            throw new IOException("Error creating a byte record RESP RespBulkString with data: " + convertToString(), e);
        }
        return bytes;
    }
    private String convertToString(){
        return Arrays.toString(data);
    }

}
