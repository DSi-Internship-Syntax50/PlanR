-- Add is_active column to ALL tables that use @SQLRestriction("is_active = true")
-- Must run BEFORE JPA schema update (spring.jpa.defer-datasource-initialization=true)
ALTER TABLE users       ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE courses     ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE departments ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE rooms       ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;
