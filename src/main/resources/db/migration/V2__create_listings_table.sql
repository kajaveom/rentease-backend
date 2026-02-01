-- Listings table
CREATE TABLE listings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(20) NOT NULL,
    price_per_day INTEGER NOT NULL,
    deposit_amount INTEGER NOT NULL,
    condition VARCHAR(20) NOT NULL,
    brand VARCHAR(50),
    model VARCHAR(100),
    pickup_location VARCHAR(200) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Listing images table
CREATE TABLE listing_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    public_id VARCHAR(200) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0
);

-- Indexes for listings
CREATE INDEX idx_listings_owner ON listings(owner_id);
CREATE INDEX idx_listings_category ON listings(category);
CREATE INDEX idx_listings_available ON listings(available, active);
CREATE INDEX idx_listings_price ON listings(price_per_day);
CREATE INDEX idx_listings_created ON listings(created_at DESC);

-- Index for listing images
CREATE INDEX idx_listing_images_listing ON listing_images(listing_id);
