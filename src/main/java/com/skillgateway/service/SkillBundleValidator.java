package com.skillgateway.service;

import com.skillgateway.model.dto.SkillBundleFileInfo;
import com.skillgateway.model.dto.SkillManifest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SkillBundleValidator {

    private static final int MAX_STORED_PATH_LENGTH = 512;
    private static final Set<String> FORBIDDEN_SEGMENTS = Set.of(".git", "node_modules", ".venv", "__pycache__");

    @ConfigProperty(name = "skill.bundle.max-bytes")
    long maxBytes;

    @ConfigProperty(name = "skill.bundle.max-file-bytes")
    long maxFileBytes;

    @ConfigProperty(name = "skill.bundle.max-files")
    int maxFiles;

    @Inject
    ManifestValidator manifestValidator;

    @Inject
    SkillBundleManifestParser manifestParser;

    public ValidatedSkillBundle validate(InputStream input) throws IOException {
        Map<String, byte[]> files = readZip(input);
        byte[] skillMd = files.get("SKILL.md");
        if (skillMd == null) {
            throw new ValidationException("bundle root must contain SKILL.md");
        }

        SkillManifest manifest = manifestParser.parse(new String(skillMd, java.nio.charset.StandardCharsets.UTF_8));
        manifestValidator.validateOrThrow(manifest);

        List<String> paths = files.keySet().stream().sorted().toList();
        List<SkillBundleFileInfo> manifestFiles = new ArrayList<>();
        Path canonicalZip = Files.createTempFile("skill-bundle-", ".zip");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(canonicalZip))) {
            for (String path : paths) {
                byte[] bytes = files.get(path);
                ZipEntry entry = new ZipEntry(path);
                entry.setTime(0);
                zip.putNextEntry(entry);
                zip.write(bytes);
                zip.closeEntry();
                manifestFiles.add(new SkillBundleFileInfo(path, sha256(bytes), bytes.length, mediaType(path), role(path)));
            }
        }
        return new ValidatedSkillBundle(manifest, manifestFiles, canonicalZip, sha256(canonicalZip), Files.size(canonicalZip));
    }

    private Map<String, byte[]> readZip(InputStream input) throws IOException {
        Map<String, byte[]> files = new HashMap<>();
        long totalBytes = 0;
        try (ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String path = normalizePath(entry.getName());
                if (files.containsKey(path)) {
                    throw new ValidationException("duplicate bundle path: " + path);
                }
                byte[] bytes = readEntry(zip, path);
                totalBytes += bytes.length;
                if (totalBytes > maxBytes) {
                    throw new ValidationException("bundle exceeds max bytes");
                }
                files.put(path, bytes);
                if (files.size() > maxFiles) {
                    throw new ValidationException("bundle exceeds max file count");
                }
            }
        }
        if (files.isEmpty()) {
            throw new ValidationException("bundle zip is empty");
        }
        long entrypoints = files.keySet().stream().filter(path -> path.equals("SKILL.md") || path.endsWith("/SKILL.md")).count();
        if (entrypoints != 1 || !files.containsKey("SKILL.md")) {
            throw new ValidationException("bundle must contain exactly one root SKILL.md");
        }
        return files;
    }

    private byte[] readEntry(ZipInputStream zip, String path) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long size = 0;
        int read;
        while ((read = zip.read(buffer)) != -1) {
            size += read;
            if (size > maxFileBytes) {
                throw new ValidationException("file exceeds max bytes: " + path);
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private String normalizePath(String rawPath) {
        String path = rawPath.replace('\\', '/').trim();
        if (path.isBlank() || path.startsWith("/") || path.matches("^[A-Za-z]:.*")) {
            throw new ValidationException("unsafe bundle path: " + rawPath);
        }
        List<String> parts = new ArrayList<>();
        for (String part : path.split("/")) {
            if (part.isBlank() || ".".equals(part) || "..".equals(part)) {
                throw new ValidationException("unsafe bundle path: " + rawPath);
            }
            String lower = part.toLowerCase();
            if (FORBIDDEN_SEGMENTS.contains(lower) || lower.equals(".env") || lower.startsWith(".env.")
                || lower.endsWith(".log") || lower.endsWith(".tmp")) {
                throw new ValidationException("forbidden bundle path: " + rawPath);
            }
            parts.add(part);
        }
        String normalized = String.join("/", parts);
        if (normalized.length() > MAX_STORED_PATH_LENGTH) {
            throw new ValidationException("bundle path exceeds max length: " + rawPath);
        }
        return normalized;
    }

    private String mediaType(String path) {
        if (path.endsWith(".md")) return "text/markdown";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".js")) return "text/javascript";
        if (path.endsWith(".ts")) return "text/typescript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private String role(String path) {
        return "SKILL.md".equals(path) ? "entrypoint" : "asset";
    }

    private String sha256(byte[] bytes) {
        return HexFormat.of().formatHex(digest().digest(bytes));
    }

    private String sha256(Path path) throws IOException {
        MessageDigest digest = digest();
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
