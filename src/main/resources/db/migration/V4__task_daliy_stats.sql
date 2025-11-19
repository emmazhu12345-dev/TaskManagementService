CREATE TABLE task_daily_stats (
    stat_date               date PRIMARY KEY,
    created_count           bigint NOT NULL DEFAULT 0,
    completed_count         bigint NOT NULL DEFAULT 0,
    removed_total_count     bigint NOT NULL DEFAULT 0,
    removed_deleted_count   bigint NOT NULL DEFAULT 0,
    removed_canceled_count  bigint NOT NULL DEFAULT 0,
    created_at              timestamptz NOT NULL DEFAULT now(),
    updated_at              timestamptz NOT NULL DEFAULT now()
);

-- Optional: trigger to keep updated_at in sync
CREATE OR REPLACE FUNCTION set_task_daily_stats_updated_at()
RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_task_daily_stats_updated_at
BEFORE UPDATE ON task_daily_stats
FOR EACH ROW
EXECUTE FUNCTION set_task_daily_stats_updated_at();
