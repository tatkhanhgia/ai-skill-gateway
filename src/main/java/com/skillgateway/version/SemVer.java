package com.skillgateway.version;

import java.util.Comparator;

public record SemVer(
    int major,
    int minor,
    int patch,
    String prerelease,
    String build
) implements Comparable<SemVer> {

    public boolean isPrerelease() {
        return prerelease != null && !prerelease.isBlank();
    }

    @Override
    public int compareTo(SemVer other) {
        int byCore = Comparator.comparingInt(SemVer::major)
            .thenComparingInt(SemVer::minor)
            .thenComparingInt(SemVer::patch)
            .compare(this, other);
        if (byCore != 0) {
            return byCore;
        }
        if (!this.isPrerelease() && other.isPrerelease()) {
            return 1;
        }
        if (this.isPrerelease() && !other.isPrerelease()) {
            return -1;
        }
        if (!this.isPrerelease()) {
            return 0;
        }
        return comparePreRelease(this.prerelease, other.prerelease);
    }

    private static int comparePreRelease(String a, String b) {
        String[] aa = a.split("\\.");
        String[] bb = b.split("\\.");
        int max = Math.max(aa.length, bb.length);
        for (int i = 0; i < max; i++) {
            if (i >= aa.length) return -1;
            if (i >= bb.length) return 1;
            String x = aa[i];
            String y = bb[i];
            boolean xn = x.chars().allMatch(Character::isDigit);
            boolean yn = y.chars().allMatch(Character::isDigit);
            int cmp;
            if (xn && yn) {
                cmp = Integer.compare(Integer.parseInt(x), Integer.parseInt(y));
            } else if (xn) {
                cmp = -1;
            } else if (yn) {
                cmp = 1;
            } else {
                cmp = x.compareTo(y);
            }
            if (cmp != 0) return cmp;
        }
        return 0;
    }

    @Override
    public String toString() {
        String core = major + "." + minor + "." + patch;
        String pre = isPrerelease() ? "-" + prerelease : "";
        String b = (build != null && !build.isBlank()) ? "+" + build : "";
        return core + pre + b;
    }
}
