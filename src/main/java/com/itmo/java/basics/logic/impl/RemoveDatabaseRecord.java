package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.logic.WritableDatabaseRecord;


public class RemoveDatabaseRecord implements WritableDatabaseRecord {

    private final byte[] _key;
    int _keySize;

    public RemoveDatabaseRecord(byte[] objectKey){
        _key = objectKey;
        _keySize = _key.length;
    }
    @Override
    public byte[] getKey() {
        return _key;
    }

    @Override
    public byte[] getValue() {
        return null;
    }

    @Override
    public long size() {
        int INT_BYTES = 4;
        return INT_BYTES + _keySize + INT_BYTES;
    }

    @Override
    public boolean isValuePresented() {
        return false;
    }

    @Override
    public int getKeySize() {
        return _keySize;
    }

    @Override
    public int getValueSize() {
        return -1;
    }
}
