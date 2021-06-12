package com.itmo.java.protocol;

import com.itmo.java.protocol.model.RespObject;

import java.io.IOException;
import java.io.OutputStream;

public class RespWriter implements AutoCloseable {
    private final OutputStream outputStream;

    public RespWriter(OutputStream os) {
        this.outputStream = os;
    }

    /**
     * Записывает в output stream объект
     */
    public void write(RespObject object) throws IOException {
        if (object != null) {
            object.write(outputStream);
        }
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
