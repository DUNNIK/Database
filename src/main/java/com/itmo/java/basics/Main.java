package com.itmo.java.basics;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.logic.Database;
import com.itmo.java.basics.logic.impl.DatabaseImpl;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        try {
            Database database = DatabaseImpl.create("TestPath", Path.of("C:\\Users\\NIKITOS"));
        } catch (DatabaseException | IOException e) {
            e.printStackTrace();
        }
    }
}
