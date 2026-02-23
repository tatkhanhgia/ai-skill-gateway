package com.skillgateway.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SemVerParser {

    private static final Pattern SEMVER = Pattern.compile(
        "^(\\d+)\\.(\\d+)\\.(\\d+)(?:-([-0-9A-Za-z.]+))?(?:\\+([0-9A-Za-z.]+))?$"
    );

    private SemVerParser() {
    }

    public static SemVer parse(String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version is required");
        }
        Matcher m = SEMVER.matcher(version);
        if (!m.matches()) {
            throw new IllegalArgumentException("invalid semver: " + version);
        }
        return new SemVer(
            Integer.parseInt(m.group(1)),
            Integer.parseInt(m.group(2)),
            Integer.parseInt(m.group(3)),
            m.group(4),
            m.group(5)
        );
    }
}
