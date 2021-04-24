package com.itmo.java.basics.initialization.impl;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.initialization.*;
import com.itmo.java.basics.logic.impl.TableImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TableInitializer implements Initializer {

    private final SegmentInitializer segmentInitializer;

    public TableInitializer(SegmentInitializer segmentInitializer) {
        this.segmentInitializer = segmentInitializer;
    }

    /**
     * Добавляет в контекст информацию об инициализируемой таблице.
     * Запускает инициализацию всех сегментов в порядке их создания (из имени)
     *
     * @param context контекст с информацией об инициализируемой бд, окружении, таблицы
     * @throws DatabaseException если в контексте лежит неправильный путь к таблице, невозможно прочитать содержимого папки,
     *  или если возникла ошибка ошибка дочерних инициализаторов
     */
    @Override
    public void perform(InitializationContext context) throws DatabaseException {

        try {
            Path tablePath = context.currentTableContext().getTablePath();

            var segmentFiles = findSegmentFiles(tablePath);

            for (File segmentFile : segmentFiles) {
                var segmentContext
                        = createSegmentContextFromFile(segmentFile, tablePath);

                segmentInitializer.perform(
                        createInitializationContextWithSegmentContext(
                                context,
                                segmentContext));
            }
            var databaseContext = context.currentDbContext();
            var tableContext = context.currentTableContext();
            addTableToDatabaseContext(databaseContext, tableContext);
        } catch (Exception e) {
            throw new DatabaseException("Error in TableInitializer", e);
        }
    }

    private void addTableToDatabaseContext
            (DatabaseInitializationContext databaseInitializationContext,
             TableInitializationContext tableInitializationContext){

        databaseInitializationContext.addTable
                (TableImpl.initializeFromContext(tableInitializationContext));
    }
    private InitializationContext createInitializationContextWithSegmentContext
            (InitializationContext context, SegmentInitializationContext segmentInitializationContext){

        return InitializationContextImpl.builder()
                .executionEnvironment(context.executionEnvironment())
                .currentDatabaseContext(context.currentDbContext())
                .currentTableContext(context.currentTableContext())
                .currentSegmentContext(segmentInitializationContext)
                .build();
    }

    private SegmentInitializationContext createSegmentContextFromFile(File segmentFile, Path tablePath){
        return new SegmentInitializationContextImpl(
                segmentFile.getName(),
                tablePath,
                0
        );
    }
    private File[] findSegmentFiles(Path tablePath){
        return new File(tablePath.toString()).listFiles(File::isFile);
    }
}
