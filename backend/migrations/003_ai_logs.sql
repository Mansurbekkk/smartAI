-- ============================================================
-- 003_ai_logs.sql
-- Smart Scholar AI — AI interaction + attention analytics
-- ============================================================

-- ------------------------------------------------------------
-- AI Interaction Logs
-- GDPR: logs purged after 90 days via pg_cron / lifecycle job
-- ------------------------------------------------------------
CREATE TABLE ai_logs (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_id       UUID,
    interaction_type VARCHAR(20) NOT NULL CHECK (
                         interaction_type IN ('scan', 'chat', 'quiz', 'attention')),
    subject          VARCHAR(100),
    content_hash     TEXT,   -- SHA-256 of chat content (not raw text — privacy)
    error_analysis   JSONB,  -- {gap_type, concept, roadmap_action}
    attention_score  FLOAT CHECK (attention_score BETWEEN 0.0 AND 1.0),
    duration_seconds INT,
    created_at       TIMESTAMPTZ DEFAULT NOW()
);

-- Auto-cleanup: rows older than 90 days (run via cron or pg_cron)
-- DELETE FROM ai_logs WHERE created_at < NOW() - INTERVAL '90 days';

CREATE INDEX idx_ai_logs_user ON ai_logs(user_id);
CREATE INDEX idx_ai_logs_created ON ai_logs(created_at DESC);

-- ------------------------------------------------------------
-- Homework Scans (references to S3 objects)
-- Actual image stored in S3 with 30-day TTL lifecycle policy
-- ------------------------------------------------------------
CREATE TABLE scans (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    s3_key      TEXT NOT NULL,
    raw_text    TEXT,
    latex       TEXT,
    analysis    JSONB,   -- subject, errors[], concepts[]
    roadmap     JSONB,   -- generated steps
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- ------------------------------------------------------------
-- Parent Dashboard View (aggregate, no raw content)
-- ------------------------------------------------------------
CREATE VIEW parent_dashboard AS
SELECT
    u.id                                   AS student_id,
    u.display_name,
    u.age,
    u.grade,
    u.irt_theta                            AS global_ability,
    COALESCE(s.current_streak, 0)          AS current_streak,
    COALESCE(s.longest_streak, 0)          AS longest_streak,
    COUNT(DISTINCT ua.achievement_id)      AS badges_earned,
    COALESCE(xp.total_xp, 0)              AS total_xp,
    ROUND(AVG(al.attention_score)::NUMERIC, 2) AS avg_focus_score,
    COALESCE(SUM(al.duration_seconds) / 3600.0, 0) AS total_study_hours,
    MAX(al.created_at)                     AS last_active
FROM users u
LEFT JOIN streaks         s  ON u.id = s.user_id
LEFT JOIN user_achievements ua ON u.id = ua.user_id
LEFT JOIN user_xp         xp ON u.id = xp.user_id
LEFT JOIN ai_logs         al ON u.id = al.user_id
GROUP BY u.id, u.display_name, u.age, u.grade, u.irt_theta,
         s.current_streak, s.longest_streak, xp.total_xp;
