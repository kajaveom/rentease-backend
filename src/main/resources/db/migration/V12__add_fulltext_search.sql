-- Add full-text search capability to listings table

-- Add a tsvector column for full-text search
ALTER TABLE listings ADD COLUMN search_vector tsvector;

-- Create a GIN index for fast full-text search
CREATE INDEX idx_listings_search ON listings USING GIN(search_vector);

-- Create a function to update the search vector
CREATE OR REPLACE FUNCTION listings_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.title, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.brand, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.model, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'C') ||
        setweight(to_tsvector('english', COALESCE(NEW.pickup_location, '')), 'D');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create a trigger to automatically update search_vector on INSERT or UPDATE
CREATE TRIGGER listings_search_vector_trigger
    BEFORE INSERT OR UPDATE ON listings
    FOR EACH ROW
    EXECUTE FUNCTION listings_search_vector_update();

-- Populate search_vector for existing rows
UPDATE listings SET search_vector =
    setweight(to_tsvector('english', COALESCE(title, '')), 'A') ||
    setweight(to_tsvector('english', COALESCE(brand, '')), 'B') ||
    setweight(to_tsvector('english', COALESCE(model, '')), 'B') ||
    setweight(to_tsvector('english', COALESCE(description, '')), 'C') ||
    setweight(to_tsvector('english', COALESCE(pickup_location, '')), 'D');
