-- 1. Define ENUM type for roles
CREATE TYPE user_role AS ENUM ('ADMIN', 'MEMBER', 'AI_AGENT');

-- 2. Create users table
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(80) NOT NULL UNIQUE,
    email           VARCHAR(255) UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(120),
    role            user_role NOT NULL DEFAULT 'MEMBER',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 3. Trigger to auto-update `updated_at`
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = now();
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- 4. Optional: initial admin seed (for bootstrap)
INSERT INTO users (username, email, password_hash, role, is_active)
VALUES ('admin', 'admin@example.com', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'ADMIN', TRUE)
ON CONFLICT DO NOTHING;
