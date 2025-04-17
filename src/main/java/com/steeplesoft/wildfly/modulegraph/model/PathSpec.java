package com.steeplesoft.wildfly.modulegraph.model;

import java.util.Objects;

public class PathSpec {
    public String path;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathSpec pathSpec = (PathSpec) o;
        return Objects.equals(path, pathSpec.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "PathSpec{" +
            "path='" + path + '\'' +
            '}';
    }
}
