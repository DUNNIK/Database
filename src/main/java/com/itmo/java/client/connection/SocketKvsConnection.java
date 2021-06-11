package com.itmo.java.client.connection;

import com.itmo.java.client.exception.ConnectionException;
import com.itmo.java.protocol.RespReader;
import com.itmo.java.protocol.RespWriter;
import com.itmo.java.protocol.model.RespArray;
import com.itmo.java.protocol.model.RespObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * С помощью {@link RespWriter} и {@link RespReader} читает/пишет в сокет
 */
public class SocketKvsConnection implements KvsConnection {
    private Socket clientSocket;

    public SocketKvsConnection(ConnectionConfig config) {
        try {
            clientSocket = new Socket(config.getHost(), config.getPort());
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    /**
     * Отправляет с помощью сокета команду и получает результат.
     * @param commandId id команды (номер)
     * @param command   команда
     * @throws ConnectionException если сокет закрыт или если произошла другая ошибка соединения
     */
    @Override
    public synchronized RespObject send(int commandId, RespArray command) throws ConnectionException {
        if (clientSocket == null || clientSocket.isClosed()) {
            throw new ConnectionException("An error occurred while connecting to the server");
        }
        try (var input = clientSocket.getInputStream(); var output = clientSocket.getOutputStream()) {
            var writer = new RespWriter(output);
            writer.write(command);
            var data = new byte[100_000];
            var readBytes = input.read(data);
            var reader = new RespReader(new ByteArrayInputStream(data, 0, readBytes));
            return reader.readObject();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new ConnectionException("An error occurred while connecting to the server", e);
        }
    }

    /**
     * Закрывает сокет (и другие использованные ресурсы)
     */
    @Override
    public void close() {
        try {
            if (clientSocket == null) {
                throw new ConnectionException("Error. Connection does not exist");
            }
            clientSocket.close();
        } catch (IOException | ConnectionException e) {
            e.printStackTrace();
        }
    }
}
