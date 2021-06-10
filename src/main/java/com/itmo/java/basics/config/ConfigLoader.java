package com.itmo.java.basics.config;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Класс, отвечающий за подгрузку данных из конфигурационного файла формата .properties
 */
public class ConfigLoader {

    private final String fileName;
    private static final String WORKING_PATH_REGEX = "\\S+\\.workingPath=";
    private static final String HOST_REGEX = "\\S+\\.host=";
    private static final String PORT_REGEX = "\\S+\\.port=";

    /**
     * По умолчанию читает из server.properties
     */
    public ConfigLoader() {
        fileName = "server.properties";
    }

    /**
     * @param name Имя конфикурационного файла, откуда читать
     */
    public ConfigLoader(String name) {
        fileName = name;
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
            var allLines = readAllFile();
            var workingPath = searchWithRegex(allLines, WORKING_PATH_REGEX);
            workingPath = ifNullThenDefault(workingPath, DatabaseConfig.DEFAULT_WORKING_PATH);
            var host = searchWithRegex(allLines, HOST_REGEX);
            host = ifNullThenDefault(host, ServerConfig.DEFAULT_HOST);
            var portStr = searchWithRegex(allLines, PORT_REGEX);
            var port = ifNullThenDefault(portStr);
            var databaseConfig = new DatabaseConfig(workingPath);
            var serverConfig = new ServerConfig(host, port);
            databaseServerConfig = new DatabaseServerConfig(serverConfig, databaseConfig);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return databaseServerConfig;
    }

    private String ifNullThenDefault(String value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private Integer ifNullThenDefault(String value) {
        if (value == null) {
            return ServerConfig.DEFAULT_PORT;
        }
        return Integer.parseInt(value);
    }

    private String searchWithRegex(List<String> allLines, String regex) {
        String value = null;
        for (var line : allLines) {
            var expressions = line.split(" ");
            for (String expression : expressions) {
                var receivedValue = getValueFromExpression(expression.trim(), regex);
                if (isStringFit(receivedValue)) {
                    value = receivedValue.trim();
                }
            }
        }
        return value;
    }

    private boolean isStringFit(String str) {
        return str != null && !str.trim().isEmpty();
    }
    private String getValueFromExpression(String expression, String regex) {
        var pattern = Pattern.compile(regex);
        var matcher1 = pattern.matcher(expression);
        if (matcher1.find()) {
            return matcher1.replaceFirst("");
        }
        return null;
    }

    private List<String> readAllFile() throws IOException {
        var inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            return new ArrayList<>();
        }
        var bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));
        var line = bufferedReader.readLine();
        var allLines = new ArrayList<String>();
        while (line != null) {
            allLines.add(line.trim());
            line = bufferedReader.readLine();
        }
        return allLines;
    }
}
