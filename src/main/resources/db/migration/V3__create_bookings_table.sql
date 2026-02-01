-- Create bookings table
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID NOT NULL REFERENCES listings(id),
    renter_id UUID NOT NULL REFERENCES users(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INTEGER NOT NULL,
    daily_rate INTEGER NOT NULL,
    total_price INTEGER NOT NULL,
    deposit_amount INTEGER NOT NULL DEFAULT 0,
    service_fee INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    renter_message VARCHAR(500),
    owner_response VARCHAR(500),
    cancellation_reason VARCHAR(500),
    cancelled_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    paid_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    CONSTRAINT valid_dates CHECK (end_date >= start_date),
    CONSTRAINT valid_status CHECK (status IN ('REQUESTED', 'APPROVED', 'PAID', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'REJECTED'))
);

-- Create indexes for common queries
CREATE INDEX idx_bookings_listing_id ON bookings(listing_id);
CREATE INDEX idx_bookings_renter_id ON bookings(renter_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_dates ON bookings(start_date, end_date);

-- Create a function to check for overlapping bookings
CREATE OR REPLACE FUNCTION check_booking_overlap()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM bookings
        WHERE listing_id = NEW.listing_id
        AND id != COALESCE(NEW.id, '00000000-0000-0000-0000-000000000000'::uuid)
        AND status IN ('APPROVED', 'PAID', 'ACTIVE')
        AND (NEW.start_date, NEW.end_date) OVERLAPS (start_date, end_date)
    ) THEN
        RAISE EXCEPTION 'Booking dates overlap with an existing booking';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to prevent overlapping bookings
CREATE TRIGGER prevent_booking_overlap
    BEFORE INSERT OR UPDATE ON bookings
    FOR EACH ROW
    WHEN (NEW.status IN ('APPROVED', 'PAID', 'ACTIVE'))
    EXECUTE FUNCTION check_booking_overlap();
