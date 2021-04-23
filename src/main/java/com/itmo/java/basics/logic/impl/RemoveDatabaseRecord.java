package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.logic.WritableDatabaseRecord;


public class RemoveDatabaseRecord implements WritableDatabaseRecord {

    private final byte[] key;
    int keySize;

    public RemoveDatabaseRecord(byte[] objectKey) {
        this.key = objectKey.clone();
        this.keySize = key.length;
    }

    @Override
    public byte[] getKey() {
        return key.clone();
    }

    @Override
    public byte[] getValue() {
        return null;
    }

    @Override
    public long size() {
        int INT_BYTES = 4;
        return INT_BYTES + keySize + INT_BYTES;
    }

    @Override
    public boolean isValuePresented() {
        return false;
    }

    @Override
    public int getKeySize() {
        return keySize;
    }

    @Override
    public int getValueSize() {
        return -1;
    }
}
