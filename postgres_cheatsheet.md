# 🐘 PostgreSQL Command Cheatsheet

A quick reference for common PostgreSQL commands — useful for local or Docker-based development.

---

## 🔌 Connect & Basics

```bash
# Connect to a PostgreSQL database
psql -h localhost -p 5432 -U tms_user -d tms_db

# Show current connection info
\conninfo

# List all databases
\l

# Switch database
\c tms_db

## Schemas & Tables
-- List all schemas
\dn

-- List all tables in current schema
\dt

-- List all tables in public schema
\dt public.*

-- Describe table structure (columns, constraints, indexes)
\d app_user

-- Detailed table info (includes size, storage info)
\d+ app_user

-- List all indexes
\di

-- Show indexes related to a specific table
\di app_user_*

-- Show foreign key relationships
\d+ task

## Querying & Data Manipulation
-- Show first 20 rows
SELECT * FROM task LIMIT 20;

-- Count total records
SELECT COUNT(*) FROM task;

-- Filter and sort results
SELECT id, title, owner_id FROM task
WHERE status = 'OPEN'
ORDER BY created_at DESC
LIMIT 50;

-- Transaction example
BEGIN;
UPDATE task SET status='DONE' WHERE id=123;
COMMIT;   -- or ROLLBACK;

-- Analyze query performance
EXPLAIN ANALYZE
SELECT * FROM task WHERE owner_id = 42;

## 💾 Backup & Restore
# Dump database (schema + data)
pg_dump -h localhost -U tms_user -d tms_db -F c -f tms_db.dump

# Restore database from dump
pg_restore -h localhost -U tms_user -d tms_db -c tms_db.dump
