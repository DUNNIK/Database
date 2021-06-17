package com.itmo.java.basics.connector;

import com.itmo.java.basics.DatabaseServer;
import com.itmo.java.basics.config.ConfigLoader;
import com.itmo.java.basics.config.DatabaseConfig;
import com.itmo.java.basics.config.ServerConfig;
import com.itmo.java.basics.console.DatabaseCommand;
import com.itmo.java.basics.console.DatabaseCommandResult;
import com.itmo.java.basics.console.DatabaseCommands;
import com.itmo.java.basics.console.impl.ExecutionEnvironmentImpl;
import com.itmo.java.basics.initialization.impl.DatabaseInitializer;
import com.itmo.java.basics.initialization.impl.DatabaseServerInitializer;
import com.itmo.java.basics.initialization.impl.SegmentInitializer;
import com.itmo.java.basics.initialization.impl.TableInitializer;
import com.itmo.java.basics.resp.CommandReader;
import com.itmo.java.protocol.RespReader;
import com.itmo.java.protocol.RespWriter;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс, который предоставляет доступ к серверу через сокеты
 */
public class JavaSocketServerConnector implements Closeable {

    static Logger logger = Logger.getLogger("MyLogger");

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
                while (!serverSocket.isClosed() && threadIsNotInterrupted()) {
                    Socket socket = serverSocket.accept();
                    var clientTask = new ClientTask(socket, databaseServer);
                    clientIOWorkers.submit(clientTask);
                }
            } catch (IOException e) {
                logger.log(Level.INFO,"An error occurred while accepting client sockets");
                e.printStackTrace();
            }
        });
    }

    private boolean threadIsNotInterrupted() {
        return !Thread.currentThread().isInterrupted();
    }

    /**
     * Закрывает все, что нужно ¯\_(ツ)_/¯
     */
    @Override
    public void close() {
        logger.log(Level.INFO,"Stopping socket connector");
        try {
            clientIOWorkers.shutdownNow();
            connectionAcceptorExecutor.shutdownNow();
            serverSocket.close();
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
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
        JavaSocketServerConnector connector;
        connector = new JavaSocketServerConnector(server, config.getServerConfig());
        connector.start();
    }

    /**
     * Runnable, описывающий исполнение клиентской команды.
     */
    static class ClientTask implements Runnable, Closeable {
        private final Socket client;
        private final DatabaseServer server;
        private CommandReader reader;
        private RespWriter writer;

        /**
         * @param client клиентский сокет
         * @param server сервер, на котором исполняется задача
         */
        public ClientTask(Socket client, DatabaseServer server) {
            this.client = client;
            this.server = server;
            try {
                this.writer = new RespWriter(client.getOutputStream());
                this.reader = new CommandReader(new RespReader(client.getInputStream()), server.getEnv());
            } catch (IOException e) {
                logger.log(Level.INFO,"Error while opening read/write streams");
                e.printStackTrace();
            }
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
            while (!client.isClosed() && threadIsNotInterrupted()) {
                try {
                    if (reader.hasNextCommand()) {
                        DatabaseCommand command = reader.readCommand();
                        CompletableFuture<DatabaseCommandResult> databaseCommandResult = server.executeNextCommand(command);
                        writer.write(databaseCommandResult.get().serialize());
                    }
                } catch (IOException | InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    logger.log(Level.INFO, "An error occurred while reading/writing from the socket");
                    e.printStackTrace();
                    close();
                    break;
                }
            }

        }

        private boolean threadIsNotInterrupted() {
            return !Thread.currentThread().isInterrupted();
        }

        /**
         * Закрывает клиентский сокет
         */
        @Override
        public void close() {
            logger.log(Level.INFO,"Stopping client socket");
            try {
                reader.close();
                writer.close();
                client.close();
            } catch (Exception e) {
                logger.log(Level.INFO, e.getMessage());
            }
        }
    }
}
