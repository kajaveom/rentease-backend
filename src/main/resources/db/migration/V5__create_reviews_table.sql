-- Create reviews table
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id),
    listing_id UUID NOT NULL REFERENCES listings(id),
    reviewer_id UUID NOT NULL REFERENCES users(id),
    reviewee_id UUID NOT NULL REFERENCES users(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment VARCHAR(1000),
    owner_response VARCHAR(500),
    owner_response_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_review_per_booking UNIQUE (booking_id, reviewer_id)
);

-- Create indexes
CREATE INDEX idx_reviews_booking ON reviews(booking_id);
CREATE INDEX idx_reviews_listing ON reviews(listing_id);
CREATE INDEX idx_reviews_reviewer ON reviews(reviewer_id);
CREATE INDEX idx_reviews_reviewee ON reviews(reviewee_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_created ON reviews(created_at DESC);

-- Function to update user's average rating
CREATE OR REPLACE FUNCTION update_user_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE users
    SET average_rating = (
        SELECT AVG(rating)::DECIMAL(3,2)
        FROM reviews
        WHERE reviewee_id = NEW.reviewee_id
    ),
    total_reviews = (
        SELECT COUNT(*)
        FROM reviews
        WHERE reviewee_id = NEW.reviewee_id
    ),
    updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.reviewee_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to auto-update user rating on new review
CREATE TRIGGER on_new_review
    AFTER INSERT OR UPDATE ON reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_user_rating();

-- Trigger for delete
CREATE OR REPLACE FUNCTION update_user_rating_on_delete()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE users
    SET average_rating = (
        SELECT COALESCE(AVG(rating)::DECIMAL(3,2), 0)
        FROM reviews
        WHERE reviewee_id = OLD.reviewee_id
    ),
    total_reviews = (
        SELECT COUNT(*)
        FROM reviews
        WHERE reviewee_id = OLD.reviewee_id
    ),
    updated_at = CURRENT_TIMESTAMP
    WHERE id = OLD.reviewee_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER on_delete_review
    AFTER DELETE ON reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_user_rating_on_delete();
