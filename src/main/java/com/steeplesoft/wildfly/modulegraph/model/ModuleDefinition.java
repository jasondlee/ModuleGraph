package com.steeplesoft.wildfly.modulegraph.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "module")
public class ModuleDefinition implements Comparable<ModuleDefinition> {
    public String name;
    public String version = "";
    public String mainClass;

    @JacksonXmlProperty //(localName = "exports")
    public List<Filter> exports = new ArrayList<>();
    //    @JacksonXmlProperty //(localName = "dependencies")
    public List<ModuleDependency> dependencies = new ArrayList<>();
    @JacksonXmlProperty(localName = "resources")
    public List<Artifact> resources = new ArrayList<>();
    @JacksonXmlProperty(localName = "properties")
    public List<Property> properties = new ArrayList<>();
    public List<Object> permissions = new ArrayList<>();
    public List<Object> provides = new ArrayList<>();

    public ModuleDefinition() {
    }

    public ModuleDefinition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleDefinition)) return false;
        ModuleDefinition moduleDefinition = (ModuleDefinition) o;
        return Objects.equals(name, moduleDefinition.name) &&
            Objects.equals(version, moduleDefinition.version) &&
            Objects.equals(mainClass, moduleDefinition.mainClass) &&
            Objects.equals(exports, moduleDefinition.exports) &&
            Objects.equals(dependencies, moduleDefinition.dependencies) &&
            Objects.equals(resources, moduleDefinition.resources) &&
            Objects.equals(properties, moduleDefinition.properties) &&
            Objects.equals(permissions, moduleDefinition.permissions) &&
            Objects.equals(provides, moduleDefinition.provides);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, mainClass, exports, dependencies, resources, properties, permissions, provides);
    }

    @Override
    public String toString() {
        return name;
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
