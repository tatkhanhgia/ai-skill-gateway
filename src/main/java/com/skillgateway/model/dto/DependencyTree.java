package com.skillgateway.model.dto;

import java.util.List;

public record DependencyTree(
    String name,
    String version,
    List<DependencyTree> dependencies
) {}
