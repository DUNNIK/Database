package com.itmo.java.basics.initialization.impl;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.index.impl.TableIndex;
import com.itmo.java.basics.initialization.InitializationContext;
import com.itmo.java.basics.initialization.Initializer;
import com.itmo.java.basics.initialization.SegmentInitializationContext;
import com.itmo.java.basics.initialization.TableInitializationContext;

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
        var tablePath = context.currentTableContext().getTablePath();
        var segmentFiles = findSegmentFiles(context.currentTableContext().getTablePath());

        for (File segmentFile : segmentFiles) {
            var segmentContext
                    = createSegmentContextFromFile(segmentFile);

            segmentInitializer.perform(
                    createInitializationContextWithSegmentContext(
                            context,
                            segmentContext));

        }
    }

    private InitializationContext createInitializationContextWithSegmentContext
            (InitializationContext context,
            SegmentInitializationContext segmentInitializationContext){

        return InitializationContextImpl.builder()
                .executionEnvironment(context.executionEnvironment())
                .currentDatabaseContext(context.currentDbContext())
                .currentTableContext(context.currentTableContext())
                .currentSegmentContext(segmentInitializationContext)
                .build();
    }

    private SegmentInitializationContext createSegmentContextFromFile(File segmentFile){
        long size = 0;
        try {
            size = Files.size(segmentFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SegmentInitializationContextImpl(
                segmentFile.getName(),
                segmentFile.toPath(),
                (int)size
        );
    }
    private File[] findSegmentFiles(Path tablePath){
        return new File(tablePath.toString()).listFiles(File::isFile);
    }
}
