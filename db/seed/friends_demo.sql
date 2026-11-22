-- Demo friends with finished games and leaderboard rows on the shared database's maps, for testing comparisons. Remove with: DELETE FROM users WHERE username IN ('AoifeK', 'ciaranb_92', 'maevegeo');

INSERT INTO users (username, location, created_at)
VALUES ('AoifeK', 'Cork, Ireland', NOW()),
       ('ciaranb_92', 'Dublin, Ireland', NOW()),
       ('maevegeo', 'Limerick, Ireland', NOW());

INSERT INTO user_games (session_id, user_id, map_id, started_at, finished_at, duration, correct_guesses, incorrect_guesses)
SELECT CONCAT('demo-', SUBSTRING(UUID(), 6)), u.user_id, g.map_id,
       NOW() - INTERVAL g.days_ago DAY - INTERVAL g.duration SECOND, NOW() - INTERVAL g.days_ago DAY,
       g.duration, g.correct, g.incorrect
FROM (SELECT 'AoifeK' AS username, 1 AS map_id, 3 AS correct, 0 AS incorrect, 7 AS duration, 1 AS days_ago
      UNION ALL SELECT 'AoifeK', 6, 8, 0, 25, 3
      UNION ALL SELECT 'ciaranb_92', 1, 2, 1, 15, 2
      UNION ALL SELECT 'ciaranb_92', 5, 3, 0, 13, 4
      UNION ALL SELECT 'ciaranb_92', 6, 5, 3, 48, 1
      UNION ALL SELECT 'maevegeo', 4, 1, 1, 9, 2
      UNION ALL SELECT 'maevegeo', 2, 1, 0, 5, 5) g
JOIN users u ON u.username = g.username;

INSERT INTO leaderboard (map_id, user_id, game_id, accuracy, duration, achieved_at)
SELECT g.map_id, g.user_id, g.game_id, ROUND(g.correct_guesses * 100 / (g.correct_guesses + g.incorrect_guesses), 2),
       g.duration, g.finished_at
FROM user_games g JOIN users u ON u.user_id = g.user_id
WHERE u.username IN ('AoifeK', 'ciaranb_92', 'maevegeo');
