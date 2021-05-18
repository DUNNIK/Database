package com.itmo.java.protocol.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Массив RESP объектов
 */
public class RespArray implements RespObject {

    /**
     * Код объекта
     */
    public static final byte CODE = '*';

    private final List<RespObject> respObjects;

    public RespArray(RespObject... objects) {
        respObjects = Arrays.asList(objects);
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
     * @return результаты метода {@link RespObject#asString()} для всех хранимых объектов, разделенные пробелом
     */
    @Override
    public String asString() {
        return convertToString();
    }

    @Override
    public void write(OutputStream os) throws IOException {
        var respArrayStartOutput = createOutputStreamBytes();
        writeBytesInOutputStream(respArrayStartOutput, os);
        writeArrayObjectsToOutputStream(os);
    }

    private void writeBytesInOutputStream(ByteArrayOutputStream respOutput, OutputStream os) throws IOException {
        try {
            os.write(respOutput.toByteArray());
        } catch (IOException e){
            throw new IOException("An error occurred while writing RespArray with that objects: " + convertToString(), e);
        }
    }
    private void writeArrayObjectsToOutputStream(OutputStream os) throws IOException {
        for (RespObject currentRespObject : respObjects) {
            try {
                currentRespObject.write(os);
            } catch (IOException e) {
                throw new IOException("An error occurred while writing RespArray objects", e);
            }
        }
    }
    private ByteArrayOutputStream createOutputStreamBytes() throws IOException {
        var bytes = new ByteArrayOutputStream();
        try {
            bytes.write(CODE);
            bytes.write(Integer.toString(whatIsTheArraySize()).getBytes(StandardCharsets.UTF_8));
            bytes.write(CRLF);
        } catch (IOException e) {
            throw new IOException("Error creating a byte record RESP RespArray with that objects: " + convertToString(), e);
        }
        return bytes;
    }

    private int whatIsTheArraySize() {
        int count = 0;
        for (var respObject:respObjects) {
            if (respObject instanceof RespCommandId) continue;
            count += 1;
        }
        return count;
    }

    public List<RespObject> getObjects() {
        return respObjects;
    }

    private String convertToString(){
        var stringBuilder = new StringBuilder();
        for (RespObject currentRespObject : respObjects) {
            stringBuilder.append(currentRespObject.asString());
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
}
