package com.itmo.java.basics.initialization.impl;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.initialization.*;
import com.itmo.java.basics.logic.impl.TableImpl;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
            CheckContextFields(context);
            var tablePath = context.currentTableContext().getTablePath();
            var segmentFiles = findSegmentFiles(tablePath);
            segmentFiles = cleanSegmentFilesArray(segmentFiles, context);
            sortFileArrayByTime(segmentFiles, context);
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

    private void CheckContextFields(InitializationContext context) throws DatabaseException {
        if (context.executionEnvironment() == null || context.currentDbContext() == null){
            throw new DatabaseException("Such a context cannot exist");
        }
    }
    private void sortFileArrayByTime(File[] files, InitializationContext context){
        Arrays.sort(files, (firstFile, secondFile) -> {
            var regexForSegmentName = createRegexForSegmentName(context);
            var pattern = Pattern.compile(regexForSegmentName);
            var matcher1 = pattern.matcher(firstFile.getName());
            Long firstFileTime = Long.parseLong(matcher1.replaceFirst(""));
            var matcher2 = pattern.matcher(secondFile.getName());
            Long secondFileTime = Long.parseLong(matcher2.replaceFirst(""));
            return firstFileTime.compareTo(secondFileTime);
        });
    }
    private File[] cleanSegmentFilesArray(File[] files, InitializationContext context){
        for (File segmentFile : files) {
            if (isNotSegmentNameCorrect(segmentFile.getName(), context)) {
                List<File> list = new ArrayList<>(Arrays.asList(files));
                list.remove(segmentFile);
                files = list.toArray(new File[list.size()]);
            }
        }
        return files;
    }

    private boolean isNotSegmentNameCorrect(String fileName, InitializationContext context){
        var regexForSegmentName = createRegexForSegmentName(context);
        var pattern = Pattern.compile(regexForSegmentName);
        var matcher = pattern.matcher(fileName);
        return !matcher.find();
    }
    private String createRegexForSegmentName(InitializationContext context){
        return "^"+context.currentTableContext().getTableName()+"_";
    }
    private void addTableToDatabaseContext
            (DatabaseInitializationContext databaseInitializationContext,
             TableInitializationContext tableInitializationContext){
        var table = TableImpl.initializeFromContext(tableInitializationContext);
        databaseInitializationContext.addTable
                (table);
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
