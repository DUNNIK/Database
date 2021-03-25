package com.itmo.java.basics;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.logic.Database;
import com.itmo.java.basics.logic.impl.DatabaseImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        try {
            Database database = DatabaseImpl.create("TestPath", Path.of("C:\\Users\\NIKITOS"));
            database.createTableIfNotExists("Table1");
            database.getName();
            database.write("Table1", "Segment1", "Hi".getBytes(StandardCharsets.UTF_8));
            var readTest = database.read("Table1", "Segment1");
            readTest.ifPresent(bytes -> System.out.print(new String(bytes)));
            database.delete("Table1", "Segment1");

        } catch (DatabaseException | IOException e) {
            System.out.print(e.getMessage());
        }
    }
}
