package com.steeplesoft.wildfly.modulegraph.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PathSet {
    public List<PathSpec> path = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathSet pathSet = (PathSet) o;
        return Objects.equals(path, pathSet.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "PathSet{" +
            "path=" + path +
            '}';
    }
}
