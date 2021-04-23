package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.logic.WritableDatabaseRecord;


public class SetDatabaseRecord implements WritableDatabaseRecord {

    private final byte[] key;
    private final byte[] value;
    int keySize;
    int valueSize;

    public SetDatabaseRecord(byte[] objectKey, byte[] objectValue) {
        this.key = objectKey.clone();
        this.value = objectValue.clone();
        this.keySize = key.length;
        this.valueSize = value.length;
    }

    @Override
    public byte[] getKey() {
        return key.clone();
    }

    @Override
    public byte[] getValue() {
        return value.clone();
    }

    @Override
    public long size() {
        int INT_BYTES = 4;
        return INT_BYTES + keySize + INT_BYTES + valueSize;
    }

    @Override
    public boolean isValuePresented() {
        return value != null;
    }

    @Override
    public int getKeySize() {
        return keySize;
    }

    @Override
    public int getValueSize() {
        return valueSize;
    }
}
