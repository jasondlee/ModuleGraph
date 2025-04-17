package com.steeplesoft.wildfly.modulegraph.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Filter {
    public List<PathSpec> include = new ArrayList<>();
    public List<PathSpec> exclude = new ArrayList<>();
    public List<PathSet> includeSet = new ArrayList<>();
    public List<PathSet> excludeSet = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Filter filter = (Filter) o;
        return Objects.equals(include, filter.include) &&
            Objects.equals(exclude, filter.exclude) &&
            Objects.equals(includeSet, filter.includeSet) &&
            Objects.equals(excludeSet, filter.excludeSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(include, exclude, includeSet, excludeSet);
    }

    @Override
    public String toString() {
        return "Filter{" +
            "include=" + include +
            ", exclude=" + exclude +
            ", includeSet=" + includeSet +
            ", excludeSet=" + excludeSet +
            '}';
    }
}
