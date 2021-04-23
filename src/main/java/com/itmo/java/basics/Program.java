package com.itmo.java.basics;

import com.itmo.java.basics.config.DatabaseConfig;
import com.itmo.java.basics.console.impl.ExecutionEnvironmentImpl;
import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.initialization.Initializer;
import com.itmo.java.basics.initialization.impl.*;
import com.itmo.java.basics.logic.impl.DatabaseImpl;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Program {
    public static void main(String[] args) {
        Path executionEnvironmentPath = Paths.get("C:/Users/NIKITOS/ExecutionEnvironment");

        try {
            Initializer initializer =
                    new DatabaseServerInitializer(
                            new DatabaseInitializer(
                                    new TableInitializer(
                                            new SegmentInitializer())));

            var executionEnvironment = new ExecutionEnvironmentImpl(
                    new DatabaseConfig(executionEnvironmentPath.toString()));

            initializer.perform(InitializationContextImpl.builder()
                    .executionEnvironment(executionEnvironment)
                    .build());



            initializer.perform(InitializationContextImpl.builder()
                    .executionEnvironment(executionEnvironment)
                    .build());

        } catch (DatabaseException e) {
            System.out.println(e.getMessage());
        }
    }


}
