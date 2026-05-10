package com.skillgateway.mcp.tools;

import com.skillgateway.model.dto.SearchRequest;
import com.skillgateway.model.dto.SearchResult;
import com.skillgateway.service.SearchService;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class SearchToolHandler {

    @Inject
    SearchService searchService;

    @Tool(description = "Search skills by query with hybrid keyword and semantic matching")
    public List<SearchResult> searchSkills(
        @ToolArg(description = "Search query") String query,
        @ToolArg(description = "Category filter") String category,
        @ToolArg(description = "Comma-separated tags") String tags,
        @ToolArg(description = "Max results") int limit
    ) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query is required");
        }
        List<String> parsedTags = tags == null || tags.isBlank()
            ? List.of()
            : Arrays.stream(tags.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
        return searchService.search(new SearchRequest(query, category, parsedTags, limit));
    }
}
