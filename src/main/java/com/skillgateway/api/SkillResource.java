package com.skillgateway.api;

import com.skillgateway.model.dto.PublishResponse;
import com.skillgateway.model.dto.SearchRequest;
import com.skillgateway.model.dto.SearchResult;
import com.skillgateway.model.dto.SkillDetail;
import com.skillgateway.model.dto.SkillManifest;
import com.skillgateway.model.dto.SkillSummary;
import com.skillgateway.model.dto.VersionInfo;
import com.skillgateway.model.dto.VersionResolution;
import com.skillgateway.service.DependencyResolver;
import com.skillgateway.service.NotFoundException;
import com.skillgateway.service.SearchService;
import com.skillgateway.service.SkillService;
import com.skillgateway.service.VersionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

@Path("/api/v1/skills")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SkillResource {

    @Inject
    SkillService skillService;

    @Inject
    SearchService searchService;

    @Inject
    VersionService versionService;

    @Inject
    DependencyResolver dependencyResolver;

    @POST
    @Path("/publish")
    public PublishResponse publish(SkillManifest manifest) {
        return skillService.publish(manifest);
    }

    @GET
    @Path("/{name}")
    public SkillDetail get(@PathParam("name") String name) {
        return skillService.findByName(name)
            .orElseThrow(() -> new NotFoundException("skill not found: " + name));
    }

    @GET
    public List<SkillSummary> list(
        @QueryParam("category") String category,
        @QueryParam("tags") String tags,
        @QueryParam("page") Integer page,
        @QueryParam("size") Integer size
    ) {
        List<String> parsedTags = tags == null || tags.isBlank()
            ? List.of()
            : Arrays.stream(tags.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
        return skillService.list(category, parsedTags, page == null ? 0 : page, size == null ? 20 : size);
    }

    @POST
    @Path("/{name}/versions/{version}/yank")
    public Response yank(
        @PathParam("name") String name,
        @PathParam("version") String version,
        @QueryParam("reason") String reason
    ) {
        skillService.yankVersion(name, version, reason == null ? "unspecified" : reason);
        return Response.ok().build();
    }

    @GET
    @Path("/search")
    public List<SearchResult> search(
        @QueryParam("query") String query,
        @QueryParam("category") String category,
        @QueryParam("tags") String tags,
        @QueryParam("limit") Integer limit
    ) {
        List<String> parsedTags = tags == null || tags.isBlank()
            ? List.of()
            : Arrays.stream(tags.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
        return searchService.search(new SearchRequest(query, category, parsedTags, limit));
    }

    @GET
    @Path("/{name}/versions")
    public List<VersionInfo> versions(@PathParam("name") String name) {
        return versionService.listVersions(name);
    }

    @GET
    @Path("/{name}/resolve")
    public VersionResolution resolve(
        @PathParam("name") String name,
        @QueryParam("constraint") String constraint
    ) {
        return versionService.resolve(name, constraint == null ? "latest" : constraint);
    }

    @GET
    @Path("/{name}/dependencies/{version}")
    public Object dependencies(
        @PathParam("name") String name,
        @PathParam("version") String version
    ) {
        return dependencyResolver.resolve(name, version);
    }
}
