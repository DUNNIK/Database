package com.itmo.java.basics.logic.impl;

import com.itmo.java.basics.logic.WritableDatabaseRecord;


public class SetDatabaseRecord implements WritableDatabaseRecord {

    private final byte[] _key;
    private final byte[] _value;
    int _keySize = 0;
    int _valueSize = 0;

    public SetDatabaseRecord(byte[] objectKey, byte[] objectValue) {
        _key = objectKey;
        _value = objectValue;
        _keySize = _key.length;
        if (_value != null) _valueSize = _value.length;
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
        int INT_BYTES = 4;
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
