package com.itmo.java.basics.initialization.impl;

import com.itmo.java.basics.index.impl.SegmentIndex;
import com.itmo.java.basics.initialization.SegmentInitializationContext;
import lombok.Builder;

import java.io.File;
import java.nio.file.Path;

@Builder
public class SegmentInitializationContextImpl implements SegmentInitializationContext {

    private final String segmentName;
    private final Path segmentPath;
    private final int currentSize;
    private final SegmentIndex index;

    public SegmentInitializationContextImpl(String segmentName, Path segmentPath, int currentSize, SegmentIndex index) {
        this.segmentName = segmentName;
        this.segmentPath = segmentPath;
        this.currentSize = currentSize;
        this.index = index;
    }

    public SegmentInitializationContextImpl(String segmentName, Path tablePath, int currentSize) {
        this.segmentName = segmentName;
        segmentPath = createSegmentPathFromRootPath(tablePath);
        this.currentSize = currentSize;
        index = new SegmentIndex();
    }

    private Path createSegmentPathFromRootPath(Path segmentRoot) {
        return Path.of(segmentRoot + File.separator + segmentName);
    }

    @Override
    public String getSegmentName() {
        return segmentName;
    }

    @Override
    public Path getSegmentPath() {
        return segmentPath;
    }

    @Override
    public SegmentIndex getIndex() {
        return index;
    }

    @Override
    public long getCurrentSize() {
        return currentSize;
    }
}
