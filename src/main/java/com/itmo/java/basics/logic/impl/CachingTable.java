package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.logic.DatabaseCache;
import com.itmo.java.basics.logic.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Optional;

@Builder
@AllArgsConstructor
public class CachingTable implements Table {
    private final DatabaseCache cache = new DatabaseCacheImpl();
    private final Table table;

    @Override
    public String getName() {
        return table.getName();
    }

    @Override
    public void write(String objectKey, byte[] objectValue) throws DatabaseException {
        table.write(objectKey, objectValue);
        cache.set(objectKey, objectValue);
    }

    @Override
    public Optional<byte[]> read(String objectKey) throws DatabaseException {
        if (cache.get(objectKey) == null){
            var objectValue = table.read(objectKey);
            objectValue.ifPresent(bytes -> cache.set(objectKey, bytes));
            return objectValue;
        }
        return Optional.of(cache.get(objectKey));
    }

    @Override
    public void delete(String objectKey) throws DatabaseException {
        table.delete(objectKey);
        cache.delete(objectKey);
    }
}
