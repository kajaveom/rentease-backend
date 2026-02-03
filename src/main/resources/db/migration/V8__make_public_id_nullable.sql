-- Make public_id nullable in listing_images table
ALTER TABLE listing_images ALTER COLUMN public_id DROP NOT NULL;
