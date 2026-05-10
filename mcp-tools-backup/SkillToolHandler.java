package com.skillgateway.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillgateway.model.dto.PublishResponse;
import com.skillgateway.model.dto.SkillDetail;
import com.skillgateway.model.dto.SkillManifest;
import com.skillgateway.model.dto.SkillSummary;
import com.skillgateway.service.NotFoundException;
import com.skillgateway.service.SkillService;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class SkillToolHandler {

    @Inject
    SkillService skillService;

    @Inject
    ObjectMapper objectMapper;

    @Tool(description = "Publish a new skill or version to the registry")
    public PublishResponse publishSkill(
        @ToolArg(description = "Skill manifest JSON") String manifestJson
    ) throws Exception {
        SkillManifest manifest = objectMapper.readValue(manifestJson, SkillManifest.class);
        return skillService.publish(manifest);
    }

    @Tool(description = "Get skill details by name")
    public SkillDetail getSkill(
        @ToolArg(description = "Skill name") String name
    ) {
        return skillService.findByName(name)
            .orElseThrow(() -> new NotFoundException("skill not found: " + name));
    }

    @Tool(description = "List skills with optional filters")
    public List<SkillSummary> listSkills(
        @ToolArg(description = "Category filter") String category,
        @ToolArg(description = "Comma-separated tags") String tags,
        @ToolArg(description = "Page number") int page,
        @ToolArg(description = "Page size") int size
    ) {
        List<String> parsedTags = tags == null || tags.isBlank()
            ? List.of()
            : Arrays.stream(tags.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
        return skillService.list(category, parsedTags, page, size);
    }

    @Tool(description = "Yank a version of a skill")
    public String yankVersion(
        @ToolArg(description = "Skill name") String name,
        @ToolArg(description = "Version") String version,
        @ToolArg(description = "Reason") String reason
    ) {
        skillService.yankVersion(name, version, reason == null ? "unspecified" : reason);
        return "ok";
    }
}
