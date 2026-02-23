package com.skillgateway.version;

public record SemVerConstraint(
    ConstraintOperator operator,
    SemVer base,
    SemVer rangeMin,
    SemVer rangeMax,
    boolean includePrerelease
) {

    public static SemVerConstraint parse(String constraint) {
        if (constraint == null || constraint.isBlank() || "latest".equalsIgnoreCase(constraint.trim())) {
            return new SemVerConstraint(ConstraintOperator.LATEST, null, null, null, false);
        }
        String c = constraint.trim();
        if (c.startsWith("^")) {
            SemVer base = SemVerParser.parse(c.substring(1));
            return new SemVerConstraint(ConstraintOperator.CARET, base, null, null, base.isPrerelease());
        }
        if (c.startsWith("~")) {
            SemVer base = SemVerParser.parse(c.substring(1));
            return new SemVerConstraint(ConstraintOperator.TILDE, base, null, null, base.isPrerelease());
        }
        if (c.startsWith(">=") && c.contains("<")) {
            String[] parts = c.split("\\s+");
            if (parts.length != 2 || !parts[0].startsWith(">=") || !parts[1].startsWith("<")) {
                throw new IllegalArgumentException("invalid range constraint: " + c);
            }
            SemVer min = SemVerParser.parse(parts[0].substring(2));
            SemVer max = SemVerParser.parse(parts[1].substring(1));
            return new SemVerConstraint(ConstraintOperator.RANGE, null, min, max, min.isPrerelease());
        }
        SemVer exact = SemVerParser.parse(c);
        return new SemVerConstraint(ConstraintOperator.EXACT, exact, null, null, exact.isPrerelease());
    }

    public boolean matches(SemVer version) {
        if (!includePrerelease && version.isPrerelease()) {
            return false;
        }
        return switch (operator) {
            case LATEST -> !version.isPrerelease();
            case CARET -> {
                SemVer upper;
                if (base.major() > 0) {
                    upper = new SemVer(base.major() + 1, 0, 0, null, null);
                } else if (base.minor() > 0) {
                    upper = new SemVer(0, base.minor() + 1, 0, null, null);
                } else {
                    upper = new SemVer(0, 0, base.patch() + 1, null, null);
                }
                yield version.compareTo(base) >= 0 && version.compareTo(upper) < 0;
            }
            case TILDE -> {
                SemVer upper = new SemVer(base.major(), base.minor() + 1, 0, null, null);
                yield version.compareTo(base) >= 0 && version.compareTo(upper) < 0;
            }
            case EXACT -> version.compareTo(base) == 0;
            case RANGE -> version.compareTo(rangeMin) >= 0 && version.compareTo(rangeMax) < 0;
        };
    }
}
