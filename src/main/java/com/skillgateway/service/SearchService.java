package com.skillgateway.service;

import com.skillgateway.model.dto.SearchRequest;
import com.skillgateway.model.dto.SearchResult;
import com.skillgateway.repository.ScoredSkill;
import com.skillgateway.repository.SkillRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SearchService {

    @Inject
    SkillRepository skillRepository;

    @Inject
    EmbeddingService embeddingService;

    @ConfigProperty(name = "search.weight.keyword")
    double keywordWeight;

    @ConfigProperty(name = "search.weight.semantic")
    double semanticWeight;

    @ConfigProperty(name = "search.weight.popularity")
    double popularityWeight;

    @ConfigProperty(name = "search.default-limit")
    int defaultLimit;

    @ConfigProperty(name = "search.max-limit")
    int maxLimit;

    public List<SearchResult> search(SearchRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("query is required");
        }

        int limit = request.limit() == null ? defaultLimit : Math.max(1, Math.min(request.limit(), maxLimit));

        List<ScoredSkill> keyword = skillRepository.keywordSearch(request.query(), 100);
        List<ScoredSkill> semantic = new ArrayList<>();
        try {
            float[] emb = embeddingService.embed(request.query());
            semantic = skillRepository.vectorSearch(embeddingService.asPgVectorLiteral(emb), 100);
        } catch (RuntimeException ignored) {
        }

        double keywordMin = keyword.stream().mapToDouble(ScoredSkill::score).min().orElse(0);
        double keywordMax = keyword.stream().mapToDouble(ScoredSkill::score).max().orElse(0);
        int popMin = merge(keyword, semantic).stream().mapToInt(ScoredSkill::downloadCount).min().orElse(0);
        int popMax = merge(keyword, semantic).stream().mapToInt(ScoredSkill::downloadCount).max().orElse(0);

        Map<Long, SearchScore> scoreById = new HashMap<>();
        for (ScoredSkill row : keyword) {
            scoreById.computeIfAbsent(row.id(), id -> SearchScore.from(row)).keyword = normalize(row.score(), keywordMin, keywordMax);
        }
        for (ScoredSkill row : semantic) {
            scoreById.computeIfAbsent(row.id(), id -> SearchScore.from(row)).semantic = row.score();
        }

        List<Long> skillIds = new ArrayList<>(scoreById.keySet());
        Map<Long, List<String>> tagsById = skillRepository.findTagsBySkillIds(skillIds);

        List<SearchResult> out = new ArrayList<>();
        for (SearchScore sc : scoreById.values()) {
            if (request.category() != null && !request.category().isBlank() && !request.category().equalsIgnoreCase(sc.category)) {
                continue;
            }
            List<String> skillTags = tagsById.getOrDefault(sc.id, List.of());
            if (request.tags() != null && !request.tags().isEmpty() && !skillTags.containsAll(request.tags())) {
                continue;
            }
            double popularity = normalize(sc.downloadCount, popMin, popMax);
            double combined = keywordWeight * sc.keyword + semanticWeight * sc.semantic + popularityWeight * popularity;
            out.add(new SearchResult(sc.id, sc.name, sc.category, sc.description, sc.downloadCount, combined, skillTags));
        }

        out.sort(Comparator.comparingDouble(SearchResult::score).reversed());
        return out.stream().limit(limit).toList();
    }

    private List<ScoredSkill> merge(List<ScoredSkill> left, List<ScoredSkill> right) {
        Map<Long, ScoredSkill> map = new HashMap<>();
        for (ScoredSkill item : left) map.put(item.id(), item);
        for (ScoredSkill item : right) map.putIfAbsent(item.id(), item);
        return new ArrayList<>(map.values());
    }

    private double normalize(double value, double min, double max) {
        if (Double.compare(max, min) == 0) {
            return 1;
        }
        return (value - min) / (max - min);
    }

    private static final class SearchScore {
        Long id;
        String name;
        String description;
        String category;
        int downloadCount;
        double keyword;
        double semantic;

        static SearchScore from(ScoredSkill row) {
            SearchScore score = new SearchScore();
            score.id = row.id();
            score.name = row.name();
            score.description = row.description();
            score.category = row.category();
            score.downloadCount = row.downloadCount();
            return score;
        }
    }
}
