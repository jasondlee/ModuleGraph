package com.steeplesoft.wildfly.modulegraph.model;

import java.util.Objects;

public class Property {
    public String name;
    public String value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property = (Property) o;
        return Objects.equals(name, property.name) &&
            Objects.equals(value, property.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return "Property{" +
            "name='" + name + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
