package com.steeplesoft.modulegraph.model;

import java.util.List;

public record PathSet(
    List<PathSpec> path) {

    public PathSet {
        if (path == null) path = List.of();
    }
}
