package com.steeplesoft.modulegraph.model;

import java.util.List;

public record Dependency(
    List<ModuleDependency> module) {

    public Dependency {
        if (module == null) module = List.of();
    }
}
