package com.skillgateway.repository;

public record ScoredSkill(
    Long id,
    String name,
    String description,
    String category,
    int downloadCount,
    double score
) {
    public static ScoredSkill fromRow(Object[] row) {
        return new ScoredSkill(
            ((Number) row[0]).longValue(),
            (String) row[1],
            (String) row[2],
            (String) row[3],
            ((Number) row[4]).intValue(),
            ((Number) row[5]).doubleValue()
        );
    }
}
