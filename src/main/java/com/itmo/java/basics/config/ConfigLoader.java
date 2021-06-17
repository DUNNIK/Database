package com.itmo.java.basics.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс, отвечающий за подгрузку данных из конфигурационного файла формата .properties
 */
public class ConfigLoader {
    private final Logger logger = Logger.getLogger("MyLogger");

    private final String name;

    /**
     * По умолчанию читает из server.properties
     */
    public ConfigLoader() {
        name = "server.properties";
    }

    /**
     * @param name Имя конфикурационного файла, откуда читать
     */
    public ConfigLoader(String name) {
        this.name = name;
    }

    /**
     * Считывает конфиг из указанного в конструкторе файла.
     * Если не удалось считать из заданного файла, или какого-то конкретно значения не оказалось,
     * то используют дефолтные значения из {@link DatabaseConfig} и {@link ServerConfig}
     * <br/>
     * Читаются: "kvs.workingPath", "kvs.host", "kvs.port" (но в конфигурационном файле допустимы и другие проперти)
     */
    public DatabaseServerConfig readConfig() {

        var databaseServerConfig = new DatabaseServerConfig(
                new ServerConfig(ServerConfig.DEFAULT_HOST, ServerConfig.DEFAULT_PORT),
                new DatabaseConfig(DatabaseConfig.DEFAULT_WORKING_PATH)
        );
        try {
            Properties properties = loadPropertiesFile();
            String workingPath = properties.getProperty("kvs.workingPath", DatabaseConfig.DEFAULT_WORKING_PATH);
            String host = properties.getProperty("kvs.host", ServerConfig.DEFAULT_HOST);
            var port = Integer.parseInt(properties.getProperty("kvs.port", String.valueOf(ServerConfig.DEFAULT_PORT)));
            var databaseConfig = new DatabaseConfig(workingPath);
            var serverConfig = new ServerConfig(host, port);
            databaseServerConfig = new DatabaseServerConfig(serverConfig, databaseConfig);
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return databaseServerConfig;
    }

    private Properties loadPropertiesFile() throws IOException {
        try (InputStream inputStream = openInputStream()) {
            var properties = new Properties();
            try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                properties.load(reader);
            }
            return properties;
        } catch (IOException e) {
            throw new IOException("An error occurred while reading the properties file: " + name);
        }
    }

    private InputStream openInputStream() {
        var inputStream = this.getClass().getClassLoader().getResourceAsStream(name);
        if (inputStream == null) {
            try {
                inputStream = new BufferedInputStream(new FileInputStream(name));
            } catch (FileNotFoundException e) {
                inputStream = this.getClass().getClassLoader().getResourceAsStream("server.properties");
            }
        }
        return inputStream;
    }
}
