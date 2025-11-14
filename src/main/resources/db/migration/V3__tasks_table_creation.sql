-- ===============================
-- TMS Task Table Initialization
-- ===============================

-- 1. Drop table if exists (for rebuild)
DROP TABLE IF EXISTS task CASCADE;

-- 2. Create task table
CREATE TABLE task (
    id          BIGSERIAL PRIMARY KEY,
    owner_id    BIGINT NOT NULL,

    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(32) NOT NULL,
    priority    VARCHAR(32),

    due_date    TIMESTAMP WITH TIME ZONE,

    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_task_owner
        FOREIGN KEY (owner_id)
        REFERENCES app_user(id)
);

-- ===============================
-- 3. Trigger: auto-update updated_at
-- ===============================

-- Create trigger function
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Bind trigger to table
CREATE TRIGGER task_set_updated_at
BEFORE UPDATE ON task
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ===============================
-- SQL END
-- ===============================
