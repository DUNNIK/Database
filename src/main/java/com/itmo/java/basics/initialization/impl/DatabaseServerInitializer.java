package com.itmo.java.basics.initialization.impl;

import com.itmo.java.basics.console.ExecutionEnvironment;
import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.initialization.DatabaseInitializationContext;
import com.itmo.java.basics.initialization.InitializationContext;
import com.itmo.java.basics.initialization.Initializer;
import com.itmo.java.basics.logic.impl.DatabaseImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseServerInitializer implements Initializer {

    private final DatabaseInitializer databaseInitializer;

    public DatabaseServerInitializer(DatabaseInitializer databaseInitializer) {
        this.databaseInitializer = databaseInitializer;
    }

    /**
     * Если заданная в окружении директория не существует - создает ее
     * Добавляет информацию о существующих в директории базах, начинает их инициалиализацию
     *
     * @param context контекст, содержащий информацию об окружении
     * @throws DatabaseException если произошла ошибка при создании директории, ее обходе или ошибка инициализации бд
     */
    @Override
    public void perform(InitializationContext context) throws DatabaseException {
        makeEnvironmentDirIfNotExist(context);

        Path environmentPath;
        try {
            environmentPath = context.executionEnvironment().getWorkingPath();
        } catch (Exception e){
            throw new DatabaseException("Invalid execution environment.", e);
        }
        var databaseDirectories = findDatabasesDir(environmentPath);

        for (File databaseDirectory : databaseDirectories) {
            var databaseContext = createDatabaseContextFromDir(
                    databaseDirectory, environmentPath);

            var executionEnvironment = context.executionEnvironment();


            databaseInitializer.perform(
                    CreateInitializationContextWithDatabaseContext(
                            executionEnvironment,
                            databaseContext));
        }
    }


    private void addDatabaseToExecutionEnvironment
            (ExecutionEnvironment executionEnvironment,
             DatabaseInitializationContext databaseInitializationContext){
        executionEnvironment.addDatabase
                (DatabaseImpl.initializeFromContext(databaseInitializationContext));
    }
    private InitializationContext CreateInitializationContextWithDatabaseContext
            (ExecutionEnvironment executionEnvironment,
             DatabaseInitializationContext databaseInitializationContext){
        return InitializationContextImpl.builder()
                .executionEnvironment(executionEnvironment)
                .currentDatabaseContext(databaseInitializationContext)
                .build();
    }
    private DatabaseInitializationContext createDatabaseContextFromDir(File directory, Path environmentPath){
        return new DatabaseInitializationContextImpl
                (directory.getName(), environmentPath);
    }
    private File[] findDatabasesDir(Path environmentPath){
        return new File(environmentPath.toString()).listFiles(File::isDirectory);
    }
    private void makeEnvironmentDirIfNotExist(InitializationContext context) throws DatabaseException {
        Path environmentPath;
        try {
            environmentPath = context.executionEnvironment().getWorkingPath();
        } catch (Exception e){
            throw new DatabaseException("Invalid execution environment.", e);
        }
        if (!Files.exists(environmentPath)){
            makeEnvironmentDir(environmentPath);
        }
    }
    private void makeEnvironmentDir(Path environmentPath) throws DatabaseException {
        try {
            Files.createDirectory(environmentPath);
        } catch (IOException e) {
            throw new DatabaseException("IO: Directory creation error.", e);
        }
    }
}
