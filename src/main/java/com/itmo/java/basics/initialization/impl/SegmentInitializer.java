package com.itmo.java.basics.initialization.impl;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.index.impl.SegmentIndex;
import com.itmo.java.basics.index.impl.SegmentOffsetInfoImpl;
import com.itmo.java.basics.initialization.InitializationContext;
import com.itmo.java.basics.initialization.Initializer;
import com.itmo.java.basics.initialization.SegmentInitializationContext;
import com.itmo.java.basics.logic.DatabaseRecord;
import com.itmo.java.basics.logic.impl.SegmentImpl;
import com.itmo.java.basics.logic.io.DatabaseInputStream;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;


public class SegmentInitializer implements Initializer {

    private SegmentInitializationContext segmentInitializationContext;
    /**
     * Добавляет в контекст информацию об инициализируемом сегменте.
     * Составляет индекс сегмента
     * Обновляет инфу в индексе таблицы
     *
     * @param context контекст с информацией об инициализируемой бд и об окружении
     * @throws DatabaseException если в контексте лежит неправильный путь к сегменту, невозможно прочитать содержимое. Ошибка в содержании
     */
    @Override
    public void perform(InitializationContext context) throws DatabaseException {
        segmentInitializationContext = context.currentSegmentContext();
        var segmentIndex = new SegmentIndex();
        try (var inputStream = new DatabaseInputStream(createInputStreamForDatabase())) {
            while (isNotFileEnd((int) segmentInitializationContext.getCurrentSize())) {
                var databaseRecord = readDatabaseRecord(inputStream);

                if (databaseRecord.isPresent()){
                    addInfoInSegmentIndex(segmentIndex, databaseRecord.get());
                    updateSegmentContextInformation(currentSize(databaseRecord.get().size()), segmentIndex);
                    updateTableIndexInformation(context, databaseRecord.get());
                }
            }

        } catch (Exception e) {
            throw new DatabaseException("Error when closing the segment file", e);
        }
    }

    private int currentSize(long recordSize){
        return (int) (recordSize + segmentInitializationContext.getCurrentSize());
    }
    private void updateSegmentContextInformation(int currentSize, SegmentIndex index){
        segmentInitializationContext = SegmentInitializationContextImpl.builder()
                .segmentName(segmentInitializationContext.getSegmentName())
                .segmentPath(segmentInitializationContext.getSegmentPath())
                .currentSize(currentSize)
                .index(index)
                .build();
    }
    private void addInfoInSegmentIndex(SegmentIndex segmentIndex, DatabaseRecord databaseRecord) {
        var objectKey = new String(databaseRecord.getKey(), StandardCharsets.UTF_8);
        var offset = segmentInitializationContext.getCurrentSize();
        segmentIndex.onIndexedEntityUpdated(objectKey, new SegmentOffsetInfoImpl(offset));
    }

    private void updateTableIndexInformation(InitializationContext context, DatabaseRecord databaseRecord){
        var objectKey = new String(databaseRecord.getKey(), StandardCharsets.UTF_8);
        var segment = SegmentImpl.initializeFromContext(segmentInitializationContext);
        context.currentTableContext().getTableIndex().onIndexedEntityUpdated(objectKey, segment);
        context.currentTableContext().updateCurrentSegment(segment);
    }
    private Optional<DatabaseRecord> readDatabaseRecord(DatabaseInputStream inputStream) throws DatabaseException {
        Optional<DatabaseRecord> unit;
        try {
            unit = inputStream.readDbUnit();
        } catch (IOException e) {
            throw new DatabaseException("Error reading the record", e);
        }
        return unit;
    }

    private DataInputStream createInputStreamForDatabase() throws DatabaseException {
        FileInputStream fileInputStream;
        var segmentPath = segmentInitializationContext.getSegmentPath();
        try {
            fileInputStream = new FileInputStream(segmentPath.toString());
        } catch (FileNotFoundException e) {
            throw new DatabaseException("Error reading the Database", e);
        }

        return new DataInputStream(fileInputStream);
    }
    private boolean isNotFileEnd(int currentSize) throws DatabaseException {
        try {
            var fileSize = Files.size(segmentInitializationContext.getSegmentPath());
            return currentSize < fileSize;
        } catch (IOException e) {
            throw new DatabaseException("File size detection error", e);
        }
    }
}
