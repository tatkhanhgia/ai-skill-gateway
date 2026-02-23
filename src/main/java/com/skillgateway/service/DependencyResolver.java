package com.skillgateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillgateway.model.SkillVersion;
import com.skillgateway.model.dto.DependencyTree;
import com.skillgateway.model.dto.VersionResolution;
import com.skillgateway.repository.SkillVersionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class DependencyResolver {

    @Inject
    SkillVersionRepository skillVersionRepository;

    @Inject
    VersionService versionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DependencyTree resolve(String skillName, String version) {
        return resolveRecursive(skillName, version, new HashSet<>(), 0);
    }

    private DependencyTree resolveRecursive(String name, String version, Set<String> visited, int depth) {
        if (depth > 10) {
            throw new IllegalArgumentException("dependency graph too deep");
        }

        String key = name + "@" + version;
        if (visited.contains(key)) {
            throw new CircularDependencyException("circular dependency detected at " + key);
        }

        visited.add(key);
        SkillVersion root = skillVersionRepository.findByNameAndVersion(name, version)
            .orElseThrow(() -> new NotFoundException("version not found: " + key));

        List<String> requires = parseRequires(root.requires);
        List<DependencyTree> children = new ArrayList<>();
        for (String req : requires) {
            if (!req.startsWith("skill:")) {
                continue;
            }
            ParsedDependency dep = parseDependency(req);
            VersionResolution resolved = versionService.resolve(dep.name, dep.constraint);
            children.add(resolveRecursive(dep.name, resolved.selectedVersion(), new HashSet<>(visited), depth + 1));
        }

        return new DependencyTree(name, version, children);
    }

    private List<String> parseRequires(String json) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private ParsedDependency parseDependency(String raw) {
        String body = raw.substring("skill:".length());
        String[] parts = body.split("@", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("invalid dependency format: " + raw);
        }
        return new ParsedDependency(parts[0], parts[1]);
    }

    private record ParsedDependency(String name, String constraint) {
    }
}
