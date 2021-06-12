package com.itmo.java.protocol;

import com.itmo.java.protocol.model.*;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RespReader implements AutoCloseable {

    private final InputStream inputStream;
    private boolean firstByteWasRead = false;
    private byte currentByteFromHasArray;
    /**
     * Специальные символы окончания элемента
     */
    private static final byte CR = '\r';
    private static final byte LF = '\n';

    public RespReader(InputStream is) {
        this.inputStream = is;
    }

    /**
     * Есть ли следующий массив в стриме?
     */
    public boolean hasArray() throws IOException {
        currentByteFromHasArray = (byte) inputStream.read();
        firstByteWasRead = true;
        return currentByteFromHasArray == RespArray.CODE;
    }

    /**
     * Считывает из input stream следующий объект. Может прочитать любой объект, сам определит его тип на основе кода объекта.
     * Например, если первый элемент "-", то вернет ошибку. Если "$" - bulk строку
     *
     * @throws EOFException если stream пустой
     * @throws IOException  при ошибке чтения
     */
    public RespObject readObject() throws IOException {
        if (firstByteWasRead) {
            firstByteWasRead = false;
            return readCorrectObject(currentByteFromHasArray);
        }
        var code = (byte) inputStream.read();
        exceptionIfStreamEmpty(code);
        return readCorrectObject(code);
    }

    private byte[] readBeforeCRLF() throws IOException {
        var previousByte = (byte) inputStream.read();
        var currentByte = (byte) inputStream.read();
        byte byteForWrite;
        var buffer = new ByteArrayOutputStream();
        while (previousByte != CR || currentByte != LF) {
            byteForWrite = previousByte;
            previousByte = currentByte;
            currentByte = (byte) inputStream.read();
            if (previousByte == -1 && currentByte == -1) {
                throw new IOException("Mistake. The buffer has run out. CLRF was not detected");
            }
            buffer.write(byteForWrite);
        }
        return buffer.toByteArray();
    }

    private void exceptionIfStreamEmpty(byte readByte) throws IOException {
        if (isInputStreamEmpty(readByte)) {
            throw new EOFException("The input stream is empty");
        }
    }

    private boolean isInputStreamEmpty(byte oneByte) {
        return oneByte == -1;
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
            default:
                throw new IOException("An incorrect object's RESP code was read");
        }
    }

    /**
     * Считывает объект ошибки
     *
     * @throws EOFException если stream пустой
     * @throws IOException  при ошибке чтения
     */
    public RespError readError() throws IOException {
        if (firstByteWasRead) {
            if (isNotCorrectCode(currentByteFromHasArray, RespError.CODE)) {
                throw new IOException("Exception. Incorrectly read code");
            }
            firstByteWasRead = false;
            return readErrorWithCode();
        }
        var code = (byte) inputStream.read();
        exceptionIfStreamEmpty(code);
        exceptionIfNotCorrectCode(code, RespError.CODE);
        return readErrorWithCode();
    }

    private RespError readErrorWithCode() throws IOException {
        var message = readBeforeCRLF();
        return new RespError(message);
    }

    private void exceptionIfNotCorrectCode(byte currentCode, byte correctCode) throws IOException {
        if (isNotCorrectCode(currentCode, correctCode)) {
            throw new IOException("Read error. Expected object with code:" + correctCode + "received object with code:" + currentCode);
        }
    }

    private boolean isNotCorrectCode(byte currentCode, byte correctCode) {
        return currentCode != correctCode;
    }

    /**
     * Читает bulk строку
     *
     * @throws EOFException если stream пустой
     * @throws IOException  при ошибке чтения
     */
    public RespBulkString readBulkString() throws IOException {
        if (firstByteWasRead) {
            if (isNotCorrectCode(currentByteFromHasArray, RespBulkString.CODE)) {
                throw new IOException("Exception. Incorrectly read code");
            }
            firstByteWasRead = false;
            return readBulkStringWithCode();
        }
        var code = (byte) inputStream.read();
        exceptionIfStreamEmpty(code);
        exceptionIfNotCorrectCode(code, RespBulkString.CODE);
        return readBulkStringWithCode();
    }

    private RespBulkString readBulkStringWithCode() throws IOException {
        var messageLength = readLength();
        if (messageLength == -1) {
            return RespBulkString.NULL_STRING;
        }
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
        if (firstByteWasRead) {
            if (isNotCorrectCode(currentByteFromHasArray, RespArray.CODE)) {
                throw new IOException("Exception. Incorrectly read code");
            }
            firstByteWasRead = false;
            return readArrayWithCode();
        }
        var code = (byte) inputStream.read();
        exceptionIfStreamEmpty(code);
        exceptionIfNotCorrectCode(code, RespArray.CODE);
        return readArrayWithCode();
    }

    private RespArray readArrayWithCode() throws IOException {
        var arrayLength = readLength();
        var respList = readArrayObjects(arrayLength);
        return RespArray.builder()
                .respObjects(respList)
                .respObjectStrings(parseStringsFromRespObjects(respList))
                .build();
    }

    private int readLength() throws IOException {
        var byteNumber = readBeforeCRLF();
        return Integer.parseInt(new String(byteNumber));
    }

    private List<RespObject> readArrayObjects(int arrayLength) throws IOException {
        var respList = new ArrayList<RespObject>();
        for (var i = 0; i < arrayLength; i++) {
            respList.add(readCorrectObject((byte) inputStream.read()));
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
        if (firstByteWasRead) {
            if (isNotCorrectCode(currentByteFromHasArray, RespCommandId.CODE)) {
                throw new IOException("Exception. Incorrectly read code");
            }
            firstByteWasRead = false;
            return readCommandIdWithCode();
        }
        var code = (byte) inputStream.read();
        exceptionIfStreamEmpty(code);
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
        inputStream.close();
    }
}
