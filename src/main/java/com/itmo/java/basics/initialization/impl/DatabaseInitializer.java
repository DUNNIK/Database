package com.itmo.java.basics.initialization.impl;

import com.itmo.java.basics.console.ExecutionEnvironment;
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
        var tableDirectories = findTableDir(databasePath);

        for (File tableDirectory : tableDirectories) {
            var tableContext
                    = CreateTableContextFromDir(tableDirectory);

            tableInitializer.perform(
                    CreateInitializationContextWithTableContext(
                            context,
                            tableContext));

            var databaseContext = context.currentDbContext();
            AddTableToDatabaseContext(databaseContext, tableContext);
        }

    }

    private void AddTableToDatabaseContext
            (DatabaseInitializationContext databaseInitializationContext,
             TableInitializationContext tableInitializationContext){

        databaseInitializationContext.addTable
                (TableImpl.initializeFromContext(tableInitializationContext));
    }
    private InitializationContext CreateInitializationContextWithTableContext
            (InitializationContext context,
             TableInitializationContext tableInitializationContext){

        return InitializationContextImpl.builder()
                .executionEnvironment(context.executionEnvironment())
                .currentDatabaseContext(context.currentDbContext())
                .currentTableContext(tableInitializationContext)
                .build();
    }
    private TableInitializationContext CreateTableContextFromDir(File directory){
        return new TableInitializationContextImpl
                (directory.getName(),
                directory.toPath(),
                        new TableIndex()
                );
    }
    private File[] findTableDir(Path environmentPath){
        return new File(environmentPath.toString()).listFiles(File::isDirectory);
    }

}
