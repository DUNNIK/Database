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
            if (config.getHost() == null) {
                throw new IOException("Host is empty");
            }
            clientSocket = new Socket(config.getHost(), config.getPort());
        } catch (IOException e) {
            e.printStackTrace();
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
        try (var input = clientSocket.getInputStream(); var output = clientSocket.getOutputStream()) {
            command.write(output);
            output.flush();
            var data = new byte[32 * 1024];
            var readBytes = input.read(data);
            var reader = new RespReader(new ByteArrayInputStream(data, 0, readBytes));
            return reader.readObject();
        } catch (IOException e) {
            throw new ConnectionException("An error occurred while connecting to the server", e);
        }
    }

    /**
     * Закрывает сокет (и другие использованные ресурсы)
     */
    @Override
    public void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
