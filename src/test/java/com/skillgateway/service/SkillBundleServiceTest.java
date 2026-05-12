package com.skillgateway.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SkillBundleServiceTest {

    @Test
    void publishRollsBackWhenStorageThrowsIOException() throws NoSuchMethodException {
        Method publish = SkillBundleService.class.getMethod("publish", java.io.InputStream.class);
        Transactional transactional = publish.getAnnotation(Transactional.class);

        assertTrue(
            Arrays.asList(transactional.rollbackOn()).contains(IOException.class),
            "bundle metadata must roll back when artifact storage fails with IOException"
        );
    }
}
