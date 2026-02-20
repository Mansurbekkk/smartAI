-- ============================================================
-- 001_initial_schema.sql
-- Smart Scholar AI — Core Tables
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ------------------------------------------------------------
-- Parents (must exist before students for FK)
-- ------------------------------------------------------------
CREATE TABLE parents (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255) UNIQUE NOT NULL,
    phone       VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    consent_verified_at TIMESTAMPTZ,  -- COPPA: parental consent
    notification_prefs  JSONB DEFAULT '{"sms": false, "email": true}',
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- ------------------------------------------------------------
-- Users (Students)
-- ------------------------------------------------------------
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name VARCHAR(100) NOT NULL,
    age         INT NOT NULL CHECK (age BETWEEN 7 AND 17),
    ui_mode     VARCHAR(10) DEFAULT 'kids' CHECK (ui_mode IN ('kids', 'teens')),
    grade       INT CHECK (grade BETWEEN 1 AND 11),
    parent_id   UUID REFERENCES parents(id) ON DELETE SET NULL,
    irt_theta   FLOAT DEFAULT 0.0 CHECK (irt_theta BETWEEN -4.0 AND 4.0),
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW()
);

-- Trigger: auto-update updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ------------------------------------------------------------
-- Subjects & Curriculum
-- ------------------------------------------------------------
CREATE TABLE subjects (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    grade_level     INT NOT NULL,
    curriculum_tree JSONB DEFAULT '{}',  -- Knowledge Graph JSON
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- ------------------------------------------------------------
-- Progress (per concept mastery)
-- ------------------------------------------------------------
CREATE TABLE progress (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject_id   INT  NOT NULL REFERENCES subjects(id),
    concept_id   VARCHAR(150) NOT NULL,
    mastery_score FLOAT DEFAULT 0.0 CHECK (mastery_score BETWEEN 0.0 AND 1.0),
    irt_theta    FLOAT DEFAULT 0.0,
    attempt_count INT DEFAULT 0,
    last_studied TIMESTAMPTZ,
    UNIQUE(user_id, concept_id)
);

CREATE INDEX idx_progress_user_id ON progress(user_id);
CREATE INDEX idx_progress_concept  ON progress(user_id, concept_id);

-- ------------------------------------------------------------
-- Streaks
-- ------------------------------------------------------------
CREATE TABLE streaks (
    user_id         UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    current_streak  INT DEFAULT 0,
    longest_streak  INT DEFAULT 0,
    last_active_date DATE
);
