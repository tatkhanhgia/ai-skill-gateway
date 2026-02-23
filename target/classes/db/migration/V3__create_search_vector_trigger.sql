CREATE OR REPLACE FUNCTION update_search_vector() RETURNS trigger AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'B');
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_skills_search_vector ON skills;
CREATE TRIGGER trg_skills_search_vector
    BEFORE INSERT OR UPDATE ON skills
    FOR EACH ROW EXECUTE FUNCTION update_search_vector();
