package com.skillgateway.api;

import com.skillgateway.model.SkillVersion;
import com.skillgateway.model.dto.SkillBundleFileInfo;
import com.skillgateway.model.dto.SkillBundlePublishResponse;
import com.skillgateway.service.SkillArtifactStorage;
import com.skillgateway.service.SkillBundleService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/api/v1/skills")
@Produces(MediaType.APPLICATION_JSON)
public class SkillBundleResource {

    @Inject
    SkillBundleService bundleService;

    @Inject
    SkillArtifactStorage artifactStorage;

    @ConfigProperty(name = "skill.bundle.max-bytes")
    long maxBundleBytes;

    @POST
    @Path("/publish-bundle")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public SkillBundlePublishResponse publishBundle(@RestForm("bundle") FileUpload bundle) throws IOException {
        if (bundle == null || bundle.uploadedFile() == null) {
            throw new IllegalArgumentException("bundle file is required");
        }
        if (bundle.size() > maxBundleBytes) {
            throw new IllegalArgumentException("bundle upload exceeds max bytes");
        }
        try (InputStream input = java.nio.file.Files.newInputStream(bundle.uploadedFile())) {
            return bundleService.publish(input);
        }
    }

    @GET
    @Path("/{name}/versions/{version}/files")
    public List<SkillBundleFileInfo> files(@PathParam("name") String name, @PathParam("version") String version) {
        return bundleService.listFiles(name, version);
    }

    @GET
    @Path("/{name}/versions/{version}/bundle")
    @Produces("application/zip")
    public Response download(@PathParam("name") String name, @PathParam("version") String version) throws IOException {
        SkillVersion bundle = bundleService.findBundleVersion(name, version);
        InputStream input = artifactStorage.open(bundle.artifactUri);
        String filename = name + "-" + version + ".zip";
        return Response.ok(input, "application/zip")
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .header("X-Bundle-Sha256", bundle.bundleSha256)
            .header("X-Bundle-Size", bundle.bundleSize)
            .build();
    }
}
