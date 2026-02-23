CREATE INDEX IF NOT EXISTS idx_skills_search_vector ON skills USING GIN(search_vector);
CREATE INDEX IF NOT EXISTS idx_skills_embedding ON skills USING hnsw(embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_skills_category ON skills(category);
CREATE INDEX IF NOT EXISTS idx_skill_versions_lookup ON skill_versions(skill_id, is_latest);
CREATE INDEX IF NOT EXISTS idx_skill_tags_tag ON skill_tags(tag_name);
