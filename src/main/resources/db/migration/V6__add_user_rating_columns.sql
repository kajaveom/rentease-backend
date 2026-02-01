-- Add rating columns to users table
ALTER TABLE users ADD COLUMN average_rating DECIMAL(3,2) DEFAULT NULL;
ALTER TABLE users ADD COLUMN total_reviews INTEGER DEFAULT 0;

-- Create index for sorting by rating
CREATE INDEX idx_users_rating ON users(average_rating DESC NULLS LAST);
