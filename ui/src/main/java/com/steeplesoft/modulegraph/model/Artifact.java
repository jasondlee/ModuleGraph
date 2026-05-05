package com.steeplesoft.modulegraph.model;

import java.util.List;

public record Artifact(
    String name,
    String path,
    List<Filter> filter,
    List<Condition> conditions) {

    public Artifact {
        if (filter == null) filter = List.of();
        if (conditions == null) conditions = List.of();
    }
}
