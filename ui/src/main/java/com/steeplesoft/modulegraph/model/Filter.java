package com.steeplesoft.modulegraph.model;

import java.util.List;

public record Filter(
    List<PathSpec> include,
    List<PathSpec> exclude,
    List<PathSet> includeSet,
    List<PathSet> excludeSet) {

    public Filter {
        if (include == null) include = List.of();
        if (exclude == null) exclude = List.of();
        if (includeSet == null) includeSet = List.of();
        if (excludeSet == null) excludeSet = List.of();
    }
}
