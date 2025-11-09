-- Add profile, role, status, and audit columns to app_user
ALTER TABLE app_user
  ADD COLUMN IF NOT EXISTS first_name   VARCHAR(100),
  ADD COLUMN IF NOT EXISTS last_name    VARCHAR(100),
  ADD COLUMN IF NOT EXISTS role         VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
  ADD COLUMN IF NOT EXISTS is_active    BOOLEAN     NOT NULL DEFAULT TRUE,
  ADD COLUMN IF NOT EXISTS created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS updated_at   TIMESTAMP   NOT NULL DEFAULT NOW();

-- Optional trigger to auto-update updated_at (PostgreSQL)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_proc WHERE proname = 'app_user_set_updated_at'
  ) THEN
    CREATE OR REPLACE FUNCTION app_user_set_updated_at()
    RETURNS TRIGGER AS $f$
    BEGIN
      NEW.updated_at = NOW();
      RETURN NEW;
    END
    $f$ LANGUAGE plpgsql;

    CREATE TRIGGER trg_app_user_updated_at
      BEFORE UPDATE ON app_user
      FOR EACH ROW
      EXECUTE FUNCTION app_user_set_updated_at();
  END IF;
END $$;
