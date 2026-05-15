package com.steeplesoft.modulegraph.model;

import java.util.List;

public record ModuleDefinition(
    String name,
    String version,
    String mainClass,
    String alias,
    List<Filter> exports,
    List<ModuleDependency> dependencies,
    List<Artifact> resources,
    List<Property> properties,
    List<Object> permissions,
    List<Object> provides) implements Comparable<ModuleDefinition> {

    public ModuleDefinition {
        if (version == null) version = "";
        if (exports == null) exports = List.of();
        if (dependencies == null) dependencies = List.of();
        if (resources == null) resources = List.of();
        if (properties == null) properties = List.of();
        if (permissions == null) permissions = List.of();
        if (provides == null) provides = List.of();
    }

    public ModuleDefinition(String name) {
        this(name, "", null, null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    @Override
    public int compareTo(ModuleDefinition other) {
        int nameComparison = name == null ? (other.name == null ? 0 : -1) : name.compareTo(other.name);
        if (nameComparison != 0) {
            return nameComparison;
        }
        return version == null ? (other.version == null ? 0 : -1) : version.compareTo(other.version);
    }
}
