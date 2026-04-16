-- Add is_active column if it doesn't exist (required by User entity soft-delete)
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;
