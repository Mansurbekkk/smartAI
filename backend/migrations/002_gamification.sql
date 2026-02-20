-- ============================================================
-- 002_gamification.sql
-- Smart Scholar AI — Achievements, XP, Badges
-- ============================================================

-- ------------------------------------------------------------
-- Achievement Definitions
-- ------------------------------------------------------------
CREATE TABLE achievements (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(80) UNIQUE NOT NULL,
    title       VARCHAR(120) NOT NULL,
    description TEXT,
    badge_url   TEXT,
    xp_reward   INT DEFAULT 50,
    condition_type VARCHAR(30),  -- streak | mastery | scan_count | ...
    condition_value JSONB DEFAULT '{}'
);

-- Seed default achievements
INSERT INTO achievements (code, title, description, xp_reward, condition_type, condition_value)
VALUES
    ('first_scan',   '🔍 Birinchi Skan!',     'Birinchi uy vazifangizni skaner qildingiz.',  100, 'scan_count',  '{"min": 1}'),
    ('streak_3',     '🔥 3-kunlik Seriya!',   'Ketma-ket 3 kun o''qidingiz.',                150, 'streak',      '{"days": 3}'),
    ('streak_7',     '🏆 Haftalik Chempion!', 'Ketma-ket 7 kun o''qidingiz.',                300, 'streak',      '{"days": 7}'),
    ('mastery_50',   '⭐ Yarim Ustoz!',        'Biror mavzuda 50% mahorat qozondingiz.',      200, 'mastery',     '{"score": 0.5}'),
    ('mastery_100',  '🎓 To''liq Ustoz!',     'Biror mavzuda 100% mahorat qozondingiz.',     500, 'mastery',     '{"score": 1.0}'),
    ('quiz_10',      '📝 Quiz Masteri!',       '10 ta viktorinani to''latdingiz.',             100, 'quiz_count',  '{"min": 10}'),
    ('socratic_50',  '💬 Qiziquvchan!',        'AI bilan 50 ta suhbat qildingiz.',             250, 'chat_count',  '{"min": 50}');

-- ------------------------------------------------------------
-- User Achievements (unlocked)
-- ------------------------------------------------------------
CREATE TABLE user_achievements (
    user_id        UUID REFERENCES users(id) ON DELETE CASCADE,
    achievement_id INT  REFERENCES achievements(id),
    unlocked_at    TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (user_id, achievement_id)
);

-- ------------------------------------------------------------
-- XP Ledger (all XP earning events)
-- ------------------------------------------------------------
CREATE TABLE xp_ledger (
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    xp_delta   INT NOT NULL,
    reason     VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- XP total view
CREATE VIEW user_xp AS
SELECT user_id, COALESCE(SUM(xp_delta), 0) AS total_xp
FROM xp_ledger
GROUP BY user_id;

-- Leaderboard view (no PII, safe for display)
CREATE VIEW leaderboard AS
SELECT
    u.id,
    u.display_name,
    u.grade,
    xp.total_xp,
    s.current_streak,
    ROW_NUMBER() OVER (ORDER BY xp.total_xp DESC) AS rank
FROM users u
JOIN user_xp xp ON u.id = xp.user_id
LEFT JOIN streaks s ON u.id = s.user_id
WHERE u.is_active = TRUE;
