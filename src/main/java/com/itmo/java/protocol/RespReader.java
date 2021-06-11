package com.itmo.java.protocol;

import com.itmo.java.protocol.model.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespReader implements AutoCloseable {

    private final BufferedReader reader;
    /**
     * Специальные символы окончания элемента
     */
    private static final byte CR = '\r';
    private static final byte LF = '\n';

    public RespReader(InputStream is) {

        this.reader = new BufferedReader(new InputStreamReader(new DataInputStream(new BufferedInputStream(is)), StandardCharsets.UTF_8));
    }

    /**
     * Есть ли следующий массив в стриме?
     */
    public boolean hasArray() throws IOException {
        var code = readCodeOfNextObjectAndReset();
        return code == RespArray.CODE;
    }

    private byte readCodeOfNextObjectAndReset() throws IOException {
        reader.mark(1);
        var oneByte = (byte) reader.read();
        reader.reset();
        return oneByte;
    }

    /**
     * Считывает из input stream следующий объект. Может прочитать любой объект, сам определит его тип на основе кода объекта.
     * Например, если первый элемент "-", то вернет ошибку. Если "$" - bulk строку
     *
     * @throws EOFException если stream пустой
     * @throws IOException  при ошибке чтения
     */
    public RespObject readObject() throws IOException {
        exceptionIfStreamEmpty();
        var code = readCodeOfNextObject();
        return readCorrectObject(code);
    }

    private byte[] readBeforeCRLF() throws IOException {
        var previousByte = (byte) reader.read();
        var currentByte = (byte) reader.read();
        byte byteForWrite;
        var buffer = new ByteArrayOutputStream();
        while (previousByte != CR || currentByte != LF) {
            byteForWrite = previousByte;
            previousByte = currentByte;
            currentByte = (byte) reader.read();
            if (byteForWrite == -1) {
                throw new IOException("Mistake. The buffer has run out. CLRF was not detected");
            }
            buffer.write(byteForWrite);
        }
        return buffer.toByteArray();
    }

    private void exceptionIfStreamEmpty() throws IOException {
        if (isInputStreamEmpty()) {
            throw new EOFException("The input stream is empty");
        }
    }

    private byte readCodeOfNextObject() throws IOException {
        try {
            return (byte) reader.read();
        } catch (IOException e) {
            throw new IOException("An error occurred while reading", e);
        }
    }

    private boolean isInputStreamEmpty() throws IOException {
        try {
            reader.mark(1);
            var oneByte = (byte) reader.read();
            reader.reset();
            return oneByte == -1;
        } catch (Exception e) {
            throw new IOException("An error occurred while checking the file for emptiness");
        }
    }

    private RespObject readCorrectObject(byte code) throws IOException {
        switch (code) {
            case RespArray.CODE:
                return readArrayWithCode();
            case RespBulkString.CODE:
                return readBulkStringWithCode();
            case RespCommandId.CODE:
                return readCommandIdWithCode();
            case RespError.CODE:
                return readErrorWithCode();
            default: throw new IOException("An incorrect object's RESP code was read");
        }
    }

    /**
     * Считывает объект ошибки
     *
     * @throws EOFException если stream пустой
     * @throws IOException  при ошибке чтения
     */
    public RespError readError() throws IOException {
        exceptionIfStreamEmpty();
        var code = (byte) reader.read();
        exceptionIfNotCorrectCode(code, RespError.CODE);
        return readErrorWithCode();
    }

    private RespError readErrorWithCode() throws IOException {
        var message = readBeforeCRLF();
        return new RespError(message);
    }

    private void exceptionIfNotCorrectCode(byte currentCode, byte correctCode) throws IOException {
        if (currentCode != correctCode) {
            throw new IOException("Read error. Expected object with code:" + correctCode + "received object with code:" + currentCode);
        }
    }

    /**
     * Читает bulk строку
     *
     * @throws EOFException если stream пустой
     * @throws IOException  при ошибке чтения
     */
    public RespBulkString readBulkString() throws IOException {
        exceptionIfStreamEmpty();
        var code = (byte) reader.read();
        exceptionIfNotCorrectCode(code, RespBulkString.CODE);
        return readBulkStringWithCode();
    }

    private RespBulkString readBulkStringWithCode() throws IOException {
        var messageLength = Integer.parseInt(reader.readLine());
        var message = readBeforeCRLF();
        if (message.length != messageLength) {
            throw new IOException("An error occurred while reading the Bulk String");
        }
        return new RespBulkString(message);
    }

    /**
     * Считывает массив RESP элементов
     *
     * @throws EOFException если stream пустой
     * @throws IOException  при ошибке чтения
     */
    public RespArray readArray() throws IOException {
        exceptionIfStreamEmpty();
        var code = (byte) reader.read();
        exceptionIfNotCorrectCode(code, RespArray.CODE);
        return readArrayWithCode();
    }

    private RespArray readArrayWithCode() throws IOException {
        var arrayLength = Integer.parseInt(reader.readLine());
        var respList = readArrayObjects(arrayLength);
        return RespArray.builder()
                .respObjects(respList)
                .respObjectStrings(parseStringsFromRespObjects(respList))
                .build();
    }
    private List<RespObject> readArrayObjects(int arrayLength) throws IOException {
        var respList = new ArrayList<RespObject>();
        for (var i = 0; i < arrayLength; i++) {
            respList.add(readCorrectObject(readCodeOfNextObject()));
        }
        return respList;
    }
    private List<String> parseStringsFromRespObjects(List<RespObject> objects) {
        var result = new ArrayList<String>();
        for (var respObject : objects) {
            result.add(respObject.asString());
        }
        return result;
    }
    /**
     * Считывает id команды
     *
     * @throws EOFException если stream пустой
     * @throws IOException  при ошибке чтения
     */
    public RespCommandId readCommandId() throws IOException {
        exceptionIfStreamEmpty();
        var code = (byte) reader.read();
        exceptionIfNotCorrectCode(code, RespCommandId.CODE);
        return readCommandIdWithCode();
    }

    private RespCommandId readCommandIdWithCode() throws IOException {
        var commandIdBytes = readBeforeCRLF();
        var commandId = getInt(commandIdBytes);
        return new RespCommandId(commandId);
    }

    private Integer getInt(byte[] bytes) {
        var byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getInt();
    }
    @Override
    public void close() throws IOException {
        reader.close();
    }
}
