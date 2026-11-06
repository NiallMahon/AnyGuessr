-- Adds profile location to users, and the starter account (user 1 on a new database): cillian@email.com / password.

ALTER TABLE users ADD COLUMN location varchar(100) NULL;

INSERT INTO users (username, email, password_hash, location, created_at)
VALUES ('CillianShine', 'cillian@email.com',
        '$2a$10$QCsj8TtbSXBL2Zhzz1sfheFlCEc9/sXxvFRr0Ei0StMuoqz3aN8ga',
        'Galway, Ireland', CURRENT_TIMESTAMP);
