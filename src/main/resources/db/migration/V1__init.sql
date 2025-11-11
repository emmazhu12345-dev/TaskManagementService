-- V1: initial schema for Task Management Service (notes + users)
-- This script is idempotent due to IF NOT EXISTS clauses.
-- Flyway will scan classpath:db/migration by default
-- all files must start with V{version}__{description}.sql

CREATE TABLE IF NOT EXISTS app_user (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  email    VARCHAR(200) NOT NULL UNIQUE,
  password_hash VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS note (
  id BIGSERIAL PRIMARY KEY,
  title     VARCHAR(200) NOT NULL,
  content   TEXT,
  owner_id  BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_note_owner ON note(owner_id);
