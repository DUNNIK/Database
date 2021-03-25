package com.itmo.java.basics;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.logic.Database;
import com.itmo.java.basics.logic.impl.DatabaseImpl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Path dbPath = Paths.get("C:\\Users\\NIKITOS");

        try {
            Database database = DatabaseImpl.create("FindMePlease", dbPath);
            database.createTableIfNotExists("table1");
            database.createTableIfNotExists("table2");
            database.write("table1", "1", "фв".getBytes(StandardCharsets.UTF_8));
            database.write("table1", "2", "1_валью".getBytes(StandardCharsets.UTF_8));
            database.write("table1", "3", "1_валью2d3".getBytes(StandardCharsets.UTF_8));
            database.write("table2", "Hel__123lo", "25ю.фыафы1241лдаара132134512".getBytes(StandardCharsets.UTF_8));
            database.write("table2", "Hel__123lo", "2132134512".getBytes(StandardCharsets.UTF_8));
            database.write("table2", "Hel1lo", "2a1s2ффодпрфрююфа.ю.ю._d3".getBytes(StandardCharsets.UTF_8));


            var a1 = database.read("table1", "1");
            var b1 = a1.get();
            var c1 = new String(b1, StandardCharsets.UTF_8);
            System.out.println(c1);

            a1 = database.read("table1", "2");
            b1 = a1.get();
            c1 = new String(b1, StandardCharsets.UTF_8);
            System.out.println(c1);

            database.delete("table1", "1");

            a1 = database.read("table1", "1");
            if (a1.isPresent()) {
                b1 = a1.get();
                c1 = new String(b1, StandardCharsets.UTF_8);
                System.out.println(c1);
            } else {
                System.out.println("Bad");
            }

            database.write("table1", "1", "132134512".getBytes(StandardCharsets.UTF_8));

            a1 = database.read("table1", "1");
            if (a1.isPresent()) {
                b1 = a1.get();
                c1 = new String(b1, StandardCharsets.UTF_8);
                System.out.println(c1);
            }

            a1 = database.read("table1", "3");
            if (a1.isPresent()) {
                b1 = a1.get();
                c1 = new String(b1, StandardCharsets.UTF_8);
                System.out.println(c1);
            }


            var a = database.read("table1", "2");
            if (a.isPresent()) {
                var b = a.get();
                var c = new String(b, StandardCharsets.UTF_8);
                System.out.println(c);
            }

        } catch (DatabaseException e) {
            System.out.println(e.getMessage());
        }
    }
}
