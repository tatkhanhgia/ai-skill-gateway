package com.skillgateway.model.dto;

import java.util.List;

public record VersionResolution(
    String skillName,
    String constraint,
    String selectedVersion,
    List<String> candidates
) {}
