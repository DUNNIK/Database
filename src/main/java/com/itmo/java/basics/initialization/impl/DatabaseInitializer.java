package com.itmo.java.basics.initialization.impl;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.index.impl.TableIndex;
import com.itmo.java.basics.initialization.DatabaseInitializationContext;
import com.itmo.java.basics.initialization.InitializationContext;
import com.itmo.java.basics.initialization.Initializer;
import com.itmo.java.basics.initialization.TableInitializationContext;
import com.itmo.java.basics.logic.impl.TableImpl;

import java.io.File;
import java.nio.file.Path;

public class DatabaseInitializer implements Initializer {

    private final TableInitializer tableInitializer;

    public DatabaseInitializer(TableInitializer tableInitializer) {
        this.tableInitializer = tableInitializer;
    }

    /**
     * Добавляет в контекст информацию об инициализируемой бд.
     * Запускает инициализацию всех таблиц это базы
     *
     * @param context контекст с информацией об инициализируемой бд и об окружении
     * @throws DatabaseException если в контексте лежит неправильный путь к базе, невозможно прочитать содержимого папки,
     *  или если возникла ошибка дочерних инициализаторов
     */
    @Override
    public void perform(InitializationContext context) throws DatabaseException {

        var databasePath = context.currentDbContext().getDatabasePath();
        var tableDirectories = findTableDirs(databasePath);

        for (File tableDirectory : tableDirectories) {
            var tableContext
                    = createTableContextFromDir(tableDirectory, databasePath);

            tableInitializer.perform(
                    createInitializationContextWithTableContext(
                            context,
                            tableContext));

            var databaseContext = context.currentDbContext();
            addTableToDatabaseContext(databaseContext, tableContext);
        }

    }

    private void addTableToDatabaseContext
            (DatabaseInitializationContext databaseInitializationContext,
             TableInitializationContext tableInitializationContext){

        databaseInitializationContext.addTable
                (TableImpl.initializeFromContext(tableInitializationContext));
    }
    private InitializationContext createInitializationContextWithTableContext
            (InitializationContext context,
             TableInitializationContext tableInitializationContext){

        return InitializationContextImpl.builder()
                .executionEnvironment(context.executionEnvironment())
                .currentDatabaseContext(context.currentDbContext())
                .currentTableContext(tableInitializationContext)
                .build();
    }
    private TableInitializationContext createTableContextFromDir(File directory, Path databasePath){
        return new TableInitializationContextImpl
                (directory.getName(),
                databasePath,
                        new TableIndex()
                );
    }
    private File[] findTableDirs(Path databasePath){
        return new File(databasePath.toString()).listFiles(File::isDirectory);
    }

}
