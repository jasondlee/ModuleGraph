package com.steeplesoft.modulegraph.model;

import java.util.List;

public record Artifact(
    String name,
    List<Filter> filter,
    List<Condition> conditions) {

    public Artifact {
        if (filter == null) filter = List.of();
        if (conditions == null) conditions = List.of();
    }
}
