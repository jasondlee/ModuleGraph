package com.steeplesoft.modulegraph.model;

import java.util.List;

public record ModuleDependency(
    String name,
    boolean export,
    boolean optional,
    String services,
    List<Property> properties,
    List<Filter> imports,
    List<Filter> exports) {

    public ModuleDependency {
        if (services == null) services = "none";
        if (properties == null) properties = List.of();
        if (imports == null) imports = List.of();
        if (exports == null) exports = List.of();
    }
}
