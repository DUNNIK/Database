package com.itmo.java.client.connection;

import com.itmo.java.client.exception.ConnectionException;
import com.itmo.java.protocol.RespReader;
import com.itmo.java.protocol.RespWriter;
import com.itmo.java.protocol.model.RespArray;
import com.itmo.java.protocol.model.RespObject;

import java.io.IOException;
import java.net.Socket;

/**
 * С помощью {@link RespWriter} и {@link RespReader} читает/пишет в сокет
 */
public class SocketKvsConnection implements KvsConnection {
    private Socket clientSocket;
    private RespReader reader;
    private RespWriter writer;

    public SocketKvsConnection(ConnectionConfig config) {
        try {
            clientSocket = new Socket(config.getHost(), config.getPort());
            this.writer = new RespWriter(clientSocket.getOutputStream());
            this.reader = new RespReader(clientSocket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Exception. Unable to connect to the server on port " + config.getPort() + " and host:" + config.getHost(), e);
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
        try {
            writer.write(command);
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
            reader.close();
            writer.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
