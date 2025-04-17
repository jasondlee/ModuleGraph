package com.steeplesoft.wildfly.modulegraph.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dependency {
    public List<ModuleDependency> module = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(module, that.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module);
    }

    @Override
    public String toString() {
        return "Dependency{" +
            "module=" + module +
            '}';
    }


}
