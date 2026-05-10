package com.skillgateway.mcp.tools;

import com.skillgateway.model.dto.VersionInfo;
import com.skillgateway.model.dto.VersionResolution;
import com.skillgateway.service.VersionService;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class VersionToolHandler {

    @Inject
    VersionService versionService;

    @Tool(description = "Resolve best version matching a constraint")
    public VersionResolution resolveVersion(
        @ToolArg(description = "Skill name") String name,
        @ToolArg(description = "Version constraint") String constraint
    ) {
        return versionService.resolve(name, constraint);
    }

    @Tool(description = "List all versions of a skill")
    public List<VersionInfo> listVersions(
        @ToolArg(description = "Skill name") String name
    ) {
        return versionService.listVersions(name);
    }
}
