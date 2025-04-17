package com.steeplesoft.wildfly.modulegraph.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModuleDependency {
    public String name;
    public boolean export = false;
    public boolean optional = false;
    public String services = "none";

    public List<Property> properties = new ArrayList<>();
    public List<Filter> imports = new ArrayList<>();
    public List<Filter> exports = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleDependency that = (ModuleDependency) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(export, that.export) &&
            Objects.equals(optional, that.optional) &&
            Objects.equals(properties, that.properties) &&
            Objects.equals(imports, that.imports) &&
            Objects.equals(exports, that.exports) &&
            Objects.equals(services, that.services);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, export, optional, properties, imports, exports, services);
    }

    @Override
    public String toString() {
        return "ModuleDependency{" +
            "name='" + name + '\'' +
            ", export=" + export +
            ", optional=" + optional +
            ", properties=" + properties +
            ", imports=" + imports +
            ", exports=" + exports +
            ", services='" + services + '\'' +
            '}';
    }
}
