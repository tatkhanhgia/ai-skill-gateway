package com.skillgateway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.skillgateway.model.Skill;
import com.skillgateway.model.SkillVersion;
import com.skillgateway.model.dto.SkillManifest;
import com.skillgateway.repository.SkillRepository;
import com.skillgateway.repository.SkillVersionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SkillServiceTest {

    @Test
    void publishPreservesExistingEmbeddingWhenProviderFails() {
        Skill existing = new Skill();
        existing.id = 7L;
        existing.name = "skill-logs";
        existing.category = "ops";
        existing.description = "old";
        existing.embedding = "[0.1,0.2]";

        SkillService service = new SkillService();
        service.skillRepository = new FakeSkillRepository(existing);
        service.skillVersionRepository = new FakeSkillVersionRepository();
        service.manifestValidator = new ManifestValidator();
        service.embeddingService = new FailingEmbeddingService();

        service.publish(new SkillManifest(
            "skill-logs",
            "1.1.0",
            "Log analysis",
            "utility",
            List.of("logs"),
            null,
            null,
            null,
            null
        ));

        assertEquals("[0.1,0.2]", existing.embedding);
    }

    private static final class FailingEmbeddingService extends EmbeddingService {
        @Override
        public float[] embed(String text) {
            throw new IllegalStateException("provider down");
        }
    }

    private static final class FakeSkillRepository extends SkillRepository {
        private final Skill existing;

        private FakeSkillRepository(Skill existing) {
            this.existing = existing;
        }

        @Override
        public Optional<Skill> findByName(String name) {
            return Optional.of(existing);
        }
    }

    private static final class FakeSkillVersionRepository extends SkillVersionRepository {
        @Override
        public Optional<SkillVersion> findBySkillAndVersion(Long skillId, String version) {
            return Optional.empty();
        }

        @Override
        public void clearLatestBySkillId(Long skillId) {
        }

        @Override
        public void persist(SkillVersion entity) {
            entity.id = 11L;
        }

        @Override
        public void flush() {
        }
    }
}
