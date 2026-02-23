CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    category VARCHAR(100) NOT NULL,
    description TEXT,
    author VARCHAR(255),
    repository_url VARCHAR(512),
    download_count INTEGER NOT NULL DEFAULT 0,
    search_vector tsvector,
    embedding vector(768),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS skill_versions (
    id BIGSERIAL PRIMARY KEY,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    version_semver VARCHAR(50) NOT NULL,
    is_prerelease BOOLEAN NOT NULL DEFAULT FALSE,
    is_latest BOOLEAN NOT NULL DEFAULT FALSE,
    is_yanked BOOLEAN NOT NULL DEFAULT FALSE,
    requires JSONB NOT NULL DEFAULT '[]',
    release_notes TEXT,
    yank_reason VARCHAR(512),
    published_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(skill_id, version_semver)
);

CREATE TABLE IF NOT EXISTS skill_tags (
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    tag_name VARCHAR(100) NOT NULL,
    PRIMARY KEY(skill_id, tag_name)
);
