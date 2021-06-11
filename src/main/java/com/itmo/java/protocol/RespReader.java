package com.itmo.java.protocol;

import com.itmo.java.protocol.model.*;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RespReader implements AutoCloseable {

    private final DataInputStream inputStream;
    private int offset;
    /**
     * Специальные символы окончания элемента
     */
    private static final byte CR = '\r';
    private static final byte LF = '\n';

    public RespReader(InputStream is) {
        this.inputStream = new DataInputStream(new BufferedInputStream(is));
        offset = 0;
    }

    /**
     * Есть ли следующий массив в стриме?
     */
    public boolean hasArray() throws IOException {
        var code = readCodeOfNextObjectAndReset();
        return code == RespArray.CODE;
    }

    private byte readCodeOfNextObjectAndReset() throws IOException {
        inputStream.mark(1);
        var oneByte = (byte) inputStream.read();
        inputStream.reset();
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
        var previousByte = (byte) inputStream.read();
        var currentByte = (byte) inputStream.read();
        byte byteForWrite;
        var buffer = new ByteArrayOutputStream();
        while (previousByte != CR || currentByte != LF) {
            byteForWrite = previousByte;
            previousByte = currentByte;
            currentByte = (byte) inputStream.read();
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
        offset++;
        return (byte) inputStream.read();
    }

    private boolean isInputStreamEmpty() throws IOException {
        inputStream.mark(1);
        var oneByte = (byte) inputStream.read();
        inputStream.reset();
        return oneByte == -1;
    }

    private boolean isNotInputStreamEmpty() throws IOException {
        inputStream.mark(1);
        var oneByte = (byte) inputStream.read();
        inputStream.reset();
        return oneByte != -1;
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
        var code = (byte) inputStream.read();
        exceptionIfNotCorrectCode(code, RespError.CODE);
        offset++;
        return readErrorWithCode();
    }

    private RespError readErrorWithCode() throws IOException {
        var message = readBeforeCRLF();
        offset += message.length;
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
        var code = (byte) inputStream.read();
        exceptionIfNotCorrectCode(code, RespBulkString.CODE);
        offset++;
        return readBulkStringWithCode();
    }

    private RespBulkString readBulkStringWithCode() throws IOException {
        var messageLengthString = readBeforeCRLF();
        offset += messageLengthString.length;
        var messageLength = getInt(messageLengthString);
        var message = readBeforeCRLF();
        if (message.length != messageLength) {
            throw new IOException("An error occurred while reading the Bulk String");
        }
        return new RespBulkString(message);
    }

    private void checkCRLF() throws IOException {
        var cr = (byte) inputStream.read();
        offset++;
        if (cr != CR) {
            throw new IOException("Read error. Expected byte:" + CR + "received byte:" + cr);
        }
        var lf = (byte) inputStream.read();
        offset++;
        if (lf != LF) {
            throw new IOException("Read error. Expected byte:" + CR + "received byte:" + cr);
        }
    }

    /**
     * Считывает массив RESP элементов
     *
     * @throws EOFException если stream пустой
     * @throws IOException  при ошибке чтения
     */
    public RespArray readArray() throws IOException {
        exceptionIfStreamEmpty();
        var code = (byte) inputStream.read();
        exceptionIfNotCorrectCode(code, RespArray.CODE);
        offset++;
        return readArrayWithCode();
    }

    private RespArray readArrayWithCode() throws IOException {
        var stringLength = readBeforeCRLF();
        offset += stringLength.length;
        var arrayLength = getInt(stringLength);
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
        var code = (byte) inputStream.read();
        exceptionIfNotCorrectCode(code, RespCommandId.CODE);
        offset++;
        return readCommandIdWithCode();
    }

    private RespCommandId readCommandIdWithCode() throws IOException {
        var commandIdBytes = readBeforeCRLF();
        offset += commandIdBytes.length;
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
