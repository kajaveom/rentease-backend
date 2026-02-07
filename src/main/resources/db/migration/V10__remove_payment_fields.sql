-- Remove payment-related fields from bookings table
ALTER TABLE bookings DROP COLUMN IF EXISTS deposit_amount;
ALTER TABLE bookings DROP COLUMN IF EXISTS service_fee;
ALTER TABLE bookings DROP COLUMN IF EXISTS paid_at;

-- Update notification type constraint to remove BOOKING_PAID
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS valid_notification_type;
ALTER TABLE notifications ADD CONSTRAINT valid_notification_type CHECK (type IN (
    'BOOKING_REQUESTED',
    'BOOKING_APPROVED',
    'BOOKING_REJECTED',
    'BOOKING_CANCELLED',
    'BOOKING_STARTED',
    'BOOKING_COMPLETED',
    'REVIEW_RECEIVED',
    'NEW_MESSAGE'
));

-- Delete any existing BOOKING_PAID notifications (optional cleanup)
DELETE FROM notifications WHERE type = 'BOOKING_PAID';
