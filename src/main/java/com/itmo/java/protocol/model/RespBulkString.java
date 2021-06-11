package com.itmo.java.protocol.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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

    public static final RespBulkString NULL_STRING = new RespBulkString(null);

    public RespBulkString(byte[] data) {
        this.data = checkData(data);
    }

    private byte[] checkData(byte[] data) {
        if (data == null) {
            return new byte[0];
        }
        return data.clone();
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
            os.write(respOutput.toByteArray());
        } catch (IOException e) {
            throw new IOException("An error occurred while writing RespBulkString with data: " + convertToString(), e);
        }
    }

    private ByteArrayOutputStream createOutputStreamBytes() throws IOException {
        var bytes = new ByteArrayOutputStream();
        try {
            if (isDataNull()) {
                createNullResp(bytes);
            } else {
                createNotNullResp(bytes);
            }
        } catch (IOException e) {
            throw new IOException("Error creating a byte record RESP RespBulkString with data: " + convertToString(), e);
        }
        return bytes;
    }

    private boolean isDataNull() {
        return data == null || data.length == 0;
    }

    private void createNotNullResp(ByteArrayOutputStream bytes) throws IOException {
        bytes.write(CODE);
        writeInt(bytes, data.length);
        //bytes.write(Integer.toString(data.length).getBytes(StandardCharsets.UTF_8));
        bytes.write(CRLF);
        bytes.write(data);
        bytes.write(CRLF);
    }

    private void createNullResp(ByteArrayOutputStream bytes) throws IOException {
        bytes.write(CODE);
        bytes.write(Integer.toString(NULL_STRING_SIZE).getBytes(StandardCharsets.UTF_8));
        bytes.write(CRLF);
    }

    private void writeInt(OutputStream os, int intValue) throws IOException {
        var byteInt = ByteBuffer.allocate(4);
        byteInt.putInt(intValue);
        os.write(byteInt.array());
    }

    private String convertToString() {
        return new String(data, StandardCharsets.UTF_8);
    }

}
