DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'skills'
          AND column_name = 'embedding'
          AND data_type = 'text'
    ) THEN
        ALTER TABLE skills
            ALTER COLUMN embedding TYPE vector(768)
            USING CASE
                WHEN embedding IS NULL OR embedding = '' THEN NULL
                ELSE embedding::vector
            END;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_skills_embedding ON skills USING hnsw(embedding vector_cosine_ops);
