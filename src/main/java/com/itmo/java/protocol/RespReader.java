package com.itmo.java.protocol;

import com.itmo.java.protocol.model.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RespReader implements AutoCloseable {

    private final InputStream inputStream;
    private final DataInputStream dataInputStream;
    private final BufferedReader reader;

    /**
     * Специальные символы окончания элемента
     */
    private static final byte CR = '\r';
    private static final byte LF = '\n';

    public RespReader(InputStream is) {
        this.inputStream = is;
        this.dataInputStream = new DataInputStream(new BufferedInputStream(is));
        reader = new BufferedReader(new InputStreamReader(dataInputStream));
    }

    /**
     * Есть ли следующий массив в стриме?
     */
    public boolean hasArray() throws IOException {
        var code = checkCodeOfNextObject();
        return code == RespArray.CODE;
    }

    /**
     * Считывает из input stream следующий объект. Может прочитать любой объект, сам определит его тип на основе кода объекта.
     * Например, если первый элемент "-", то вернет ошибку. Если "$" - bulk строку
     *
     * @throws EOFException если stream пустой
     * @throws IOException  при ошибке чтения
     */
    public RespObject readObject() throws IOException {
        if (isInputStreamEmpty()) {
            throw new EOFException("The input stream is empty");
        }
        var code = checkCodeOfNextObject();
        return readCorrectObject(code);
    }

    private void exceptionIfStreamEmpty() throws IOException {
        if (isInputStreamEmpty()) {
            throw new EOFException("The input stream is empty");
        }
    }

    private byte checkCodeOfNextObject() throws IOException {
        dataInputStream.mark(1);
        var code = dataInputStream.readByte();
        dataInputStream.reset();
        return code;
    }

    private boolean isInputStreamEmpty() throws IOException {
        dataInputStream.mark(1);
        var oneByte = dataInputStream.readByte();
        dataInputStream.reset();
        return oneByte == -1;
    }

    private RespObject readCorrectObject(byte code) throws IOException {
        switch (code) {
            case RespArray.CODE:
                return readArray();
            case RespBulkString.CODE:
                return readBulkString();
            case RespCommandId.CODE:
                return readCommandId();
            case RespError.CODE:
                return readError();
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
        var code = dataInputStream.readByte();
        exceptionIfNotCorrectCode(code, RespError.CODE);
        var message = reader.readLine();
        return new RespError(message.getBytes());
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
        var code = dataInputStream.readByte();
        exceptionIfNotCorrectCode(code, RespBulkString.CODE);
        var messageLength = Integer.parseInt(reader.readLine());
        var message = reader.readLine();
        if (message.length() != messageLength ) {
            throw new IOException("The length of the read string:" + message.length() + "should have been:" + messageLength);
        }
        return new RespBulkString(message.getBytes());
    }

    /**
     * Считывает массив RESP элементов
     *
     * @throws EOFException если stream пустой
     * @throws IOException  при ошибке чтения
     */
    public RespArray readArray() throws IOException {
        exceptionIfStreamEmpty();
        var code = dataInputStream.readByte();
        exceptionIfNotCorrectCode(code, RespArray.CODE);
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
            respList.add(readCorrectObject(checkCodeOfNextObject()));
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
        var code = dataInputStream.readByte();
        exceptionIfNotCorrectCode(code, RespCommandId.CODE);
        var commandId = getInt(reader.readLine().getBytes());
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
