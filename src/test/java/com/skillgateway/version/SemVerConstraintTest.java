package com.skillgateway.version;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SemVerConstraintTest {

    @Test
    void caretConstraintForMajorOne() {
        SemVerConstraint c = SemVerConstraint.parse("^1.2.3");
        assertTrue(c.matches(SemVerParser.parse("1.5.0")));
        assertFalse(c.matches(SemVerParser.parse("2.0.0")));
    }

    @Test
    void caretConstraintForMajorZero() {
        SemVerConstraint c = SemVerConstraint.parse("^0.2.3");
        assertTrue(c.matches(SemVerParser.parse("0.2.9")));
        assertFalse(c.matches(SemVerParser.parse("0.3.0")));
    }

    @Test
    void tildeConstraintMatchesPatchOnly() {
        SemVerConstraint c = SemVerConstraint.parse("~1.2.3");
        assertTrue(c.matches(SemVerParser.parse("1.2.9")));
        assertFalse(c.matches(SemVerParser.parse("1.3.0")));
    }

    @Test
    void rangeConstraintParsesAndMatches() {
        SemVerConstraint c = SemVerConstraint.parse(">=1.2.3 <2.0.0");
        assertTrue(c.matches(SemVerParser.parse("1.9.9")));
        assertFalse(c.matches(SemVerParser.parse("2.0.0")));
    }
}
