package com.itmo.java.basics.initialization.impl;

import com.itmo.java.basics.index.impl.TableIndex;
import com.itmo.java.basics.initialization.TableInitializationContext;
import com.itmo.java.basics.logic.Segment;

import java.io.File;
import java.nio.file.Path;

public class TableInitializationContextImpl implements TableInitializationContext {

    private final String tableName;
    private final Path tablePath;
    private final TableIndex tableIndex;
    private Segment currentSegment;

    public TableInitializationContextImpl(String tableName, Path databasePath, TableIndex tableIndex) {
        this.tableName = tableName;
        tablePath = createTablePathFromRootPath(databasePath);
        this.tableIndex = createNewIfNull(tableIndex);
    }

    private TableIndex createNewIfNull(TableIndex tableIndex){
        if (tableIndex==null) return new TableIndex();
        return tableIndex;
    }
    private Path createTablePathFromRootPath(Path tableRoot) {
        return Path.of(tableRoot + File.separator + tableName);
    }
    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public Path getTablePath() {
        return tablePath;
    }

    @Override
    public TableIndex getTableIndex() {
        return tableIndex;
    }

    @Override
    public Segment getCurrentSegment() {
        return currentSegment;
    }

    @Override
    public void updateCurrentSegment(Segment segment) {
        currentSegment = segment;
    }
}
