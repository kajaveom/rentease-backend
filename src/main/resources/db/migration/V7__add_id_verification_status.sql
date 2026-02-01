-- Add ID verification status column to users table
ALTER TABLE users ADD COLUMN id_verification_status VARCHAR(20) DEFAULT 'NONE' NOT NULL;

-- Add submission timestamp
ALTER TABLE users ADD COLUMN id_verification_submitted_at TIMESTAMP;

-- Add rejection reason (for when admins reject)
ALTER TABLE users ADD COLUMN id_verification_rejection_reason TEXT;

-- Create index for admin queries on pending verifications
CREATE INDEX idx_users_id_verification_status ON users(id_verification_status);
