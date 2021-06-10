package com.itmo.java.basics.connector;

import com.itmo.java.basics.DatabaseServer;
import com.itmo.java.basics.config.ConfigLoader;
import com.itmo.java.basics.config.DatabaseConfig;
import com.itmo.java.basics.config.ServerConfig;
import com.itmo.java.basics.console.impl.ExecutionEnvironmentImpl;
import com.itmo.java.basics.initialization.impl.DatabaseInitializer;
import com.itmo.java.basics.initialization.impl.DatabaseServerInitializer;
import com.itmo.java.basics.initialization.impl.SegmentInitializer;
import com.itmo.java.basics.initialization.impl.TableInitializer;
import com.itmo.java.basics.resp.CommandReader;
import com.itmo.java.protocol.RespReader;
import com.itmo.java.protocol.RespWriter;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Класс, который предоставляет доступ к серверу через сокеты
 */
public class JavaSocketServerConnector implements Closeable {

    /**
     * Экзекьютор для выполнения ClientTask
     */
    private final ExecutorService clientIOWorkers = Executors.newSingleThreadExecutor();
    private final DatabaseServer databaseServer;
    private final ServerSocket serverSocket;
    private final ExecutorService connectionAcceptorExecutor = Executors.newSingleThreadExecutor();

    /**
     * Стартует сервер. По аналогии с сокетом открывает коннекшн в конструкторе.
     */
    public JavaSocketServerConnector(DatabaseServer databaseServer, ServerConfig config) throws IOException {
        serverSocket = new ServerSocket(config.getPort());
        this.databaseServer = databaseServer;
    }
    /**
     * Начинает слушать заданный порт, начинает аксептить клиентские сокеты. На каждый из них начинает клиентскую таску
     */
    public void start() {
        connectionAcceptorExecutor.submit(() -> {
            try {
                var socket = serverSocket.accept();
                var clientTask = new ClientTask(socket, databaseServer);
                clientIOWorkers.submit(clientTask);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Закрывает все, что нужно ¯\_(ツ)_/¯
     */
    @Override
    public void close() {
        System.out.println("Stopping socket connector");
        try {
            serverSocket.close();
            clientIOWorkers.shutdown();
            connectionAcceptorExecutor.shutdown();
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception {
        var executionEnvironmentPath = Paths.get("C:/Users/NIKITOS/ExecutionEnvironment");
        var initializer =
                new DatabaseServerInitializer(
                        new DatabaseInitializer(
                                new TableInitializer(
                                        new SegmentInitializer())));

        var executionEnvironment = new ExecutionEnvironmentImpl(
                new DatabaseConfig(executionEnvironmentPath.toString()));

        var server = DatabaseServer.initialize(executionEnvironment, initializer);
        var loader = new ConfigLoader();
        var config = loader.readConfig();
        JavaSocketServerConnector connector = null;
        connector = new JavaSocketServerConnector(server, config.getServerConfig());
        connector.start();
    }

    /**
     * Runnable, описывающий исполнение клиентской команды.
     */
    static class ClientTask implements Runnable, Closeable {
        private final Socket client;
        private final DatabaseServer server;

        /**
         * @param client клиентский сокет
         * @param server сервер, на котором исполняется задача
         */
        public ClientTask(Socket client, DatabaseServer server) {
            this.client = client;
            this.server = server;
        }

        /**
         * Исполняет задачи из одного клиентского сокета, пока клиент не отсоединился или текущий поток не был прерван (interrupted).
         * Для кажной из задач:
         * 1. Читает из сокета команду с помощью {@link CommandReader}
         * 2. Исполняет ее на сервере
         * 3. Записывает результат в сокет с помощью {@link RespWriter}
         */
        @Override
        public void run() {
            try (var input = client.getInputStream(); var output = client.getOutputStream()) {
                var buffer = new byte[32 * 1024];
                int readBytes = input.read(buffer);
                var commandReader = new CommandReader(new RespReader(new ByteArrayInputStream(buffer, 0, readBytes)), server.getEnv());
                if (commandReader.hasNextCommand()) {
                    var command = commandReader.readCommand();
                    var databaseCommandResult = command.execute();
                    var respWriter = new RespWriter(output);
                    respWriter.write(databaseCommandResult.serialize());
                }
            } catch (IOException e) {
                System.out.println("An error occurred while reading/writing from the socket");
                e.printStackTrace();
            }
        }

        /**
         * Закрывает клиентский сокет
         */
        @Override
        public void close() {
            System.out.println("Stopping client socket");
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
