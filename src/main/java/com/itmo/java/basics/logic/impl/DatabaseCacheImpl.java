package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.logic.DatabaseCache;

import java.util.LinkedHashMap;
import java.util.Map;


public class DatabaseCacheImpl implements DatabaseCache {

    private static final int CAPACITY = 5_000;
    private final LinkedHashMap<String, byte[]> cacheMap
            = new LinkedHashMap<>(CAPACITY, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
            return size() > CAPACITY;
        }
    };

    @Override
    public byte[] get(String key) {
        return cacheMap.getOrDefault(key, null);
    }

    @Override
    public void set(String key, byte[] value) {
        cacheMap.put(key, value);
    }

    @Override
    public void delete(String key) {
        cacheMap.remove(key);
    }
}
