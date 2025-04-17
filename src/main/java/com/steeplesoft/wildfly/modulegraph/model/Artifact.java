package com.steeplesoft.wildfly.modulegraph.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Artifact {
    public String name;
    public String path;
    public List<Filter> filter = new ArrayList<>();
    public List<Condition> conditions = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return Objects.equals(name, artifact.name) &&
            Objects.equals(path, artifact.path) &&
            Objects.equals(filter, artifact.filter) &&
            Objects.equals(conditions, artifact.conditions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path, filter, conditions);
    }

    @Override
    public String toString() {
        return "Artifact{" +
            "name='" + name + '\'' +
            "path='" + path + '\'' +
            ", filter=" + filter +
            ", conditions=" + conditions +
            '}';
    }
}
