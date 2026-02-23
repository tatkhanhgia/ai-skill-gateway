package com.skillgateway.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SemVerParserTest {

    @Test
    void parseValidVersion() {
        SemVer v = SemVerParser.parse("1.2.3-beta.1+build.42");
        assertEquals(1, v.major());
        assertEquals(2, v.minor());
        assertEquals(3, v.patch());
        assertEquals("beta.1", v.prerelease());
        assertEquals("build.42", v.build());
    }

    @Test
    void parseInvalidVersionThrows() {
        assertThrows(IllegalArgumentException.class, () -> SemVerParser.parse("not.a.version"));
    }
}
