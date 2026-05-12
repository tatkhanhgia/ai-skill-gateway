ALTER TABLE skill_versions
    ADD COLUMN IF NOT EXISTS package_format VARCHAR(32),
    ADD COLUMN IF NOT EXISTS entrypoint_path VARCHAR(255),
    ADD COLUMN IF NOT EXISTS bundle_sha256 VARCHAR(64),
    ADD COLUMN IF NOT EXISTS bundle_size BIGINT,
    ADD COLUMN IF NOT EXISTS file_count INTEGER,
    ADD COLUMN IF NOT EXISTS artifact_uri VARCHAR(1024);

CREATE TABLE IF NOT EXISTS skill_version_files (
    id BIGSERIAL PRIMARY KEY,
    version_id BIGINT NOT NULL REFERENCES skill_versions(id) ON DELETE CASCADE,
    path VARCHAR(512) NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    size_bytes BIGINT NOT NULL,
    media_type VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    UNIQUE(version_id, path)
);

CREATE INDEX IF NOT EXISTS idx_skill_versions_bundle_sha256
    ON skill_versions(bundle_sha256);

CREATE INDEX IF NOT EXISTS idx_skill_version_files_version_path
    ON skill_version_files(version_id, path);
