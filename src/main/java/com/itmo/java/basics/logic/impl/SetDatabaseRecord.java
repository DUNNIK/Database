package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.logic.WritableDatabaseRecord;

import java.nio.charset.StandardCharsets;

public class SetDatabaseRecord implements WritableDatabaseRecord {

    private final int INT_BYTES = 4;
    private byte[] _key;
    private byte[] _value;
    int _keySize;
    int _valueSize;

    public SetDatabaseRecord(byte[] objectKey, byte[] objectValue) {
        _key = objectKey;
        _value = objectValue;
        _keySize = _key.length;
        _valueSize = _value.length;
    }

    @Override
    public byte[] getKey() {
        return _key;
    }

    @Override
    public byte[] getValue() {
        return _value;
    }

    @Override
    public long size() {
        return INT_BYTES + _keySize + INT_BYTES + _valueSize;
    }

    @Override
    public boolean isValuePresented() {
        return _value != null;
    }

    @Override
    public int getKeySize() {
        return _keySize;
    }

    @Override
    public int getValueSize() {
        return _valueSize;
    }
}
