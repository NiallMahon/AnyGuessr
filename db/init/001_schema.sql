-- anyguessr schema (prototype). Written for MySQL 8.0 / InnoDB.

-- ---------------------------------------------------------------- identity

CREATE TABLE users (
                       user_id        bigint        NOT NULL AUTO_INCREMENT,
                       username       varchar(50)   NOT NULL,
                       email          varchar(255)  NULL,
                       password_hash  varchar(255)  NULL,
                       verified_at    timestamp     NULL,
                       created_at     timestamp     NOT NULL,
                       deleted_at     timestamp     NULL,
                       PRIMARY KEY (user_id),
                       UNIQUE KEY uq_users_username (username),
                       UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE user_trophies (
                               user_id      bigint       NOT NULL,
                               trophy_type  varchar(50)  NOT NULL,
                               awarded_at   timestamp    NOT NULL,
                               PRIMARY KEY (user_id, trophy_type),
                               CONSTRAINT fk_user_trophies_user FOREIGN KEY (user_id)
                                   REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE external_login (
                                provider          varchar(20)   NOT NULL,
                                provider_user_id  varchar(255)  NOT NULL,
                                user_id           bigint        NOT NULL,
                                PRIMARY KEY (provider, provider_user_id),
                                CONSTRAINT fk_external_login_user FOREIGN KEY (user_id)
                                    REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE auth_tokens (
                             token_hash  varchar(64)  NOT NULL,
                             user_id     bigint       NOT NULL,
                             purpose     varchar(10)  NOT NULL,
                             expires_at  timestamp    NOT NULL,
                             PRIMARY KEY (token_hash),
                             CONSTRAINT fk_auth_tokens_user FOREIGN KEY (user_id)
                                 REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------------ social

CREATE TABLE friends (
                         user_id_a     bigint       NOT NULL,
                         user_id_b     bigint       NOT NULL,
                         requested_by  bigint       NOT NULL,
                         status        varchar(10)  NOT NULL,
                         created_at    timestamp    NOT NULL,
                         PRIMARY KEY (user_id_a, user_id_b),
                         CONSTRAINT fk_friends_user_a FOREIGN KEY (user_id_a)
                             REFERENCES users (user_id) ON DELETE CASCADE,
                         CONSTRAINT fk_friends_user_b FOREIGN KEY (user_id_b)
                             REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE blocked_users (
                               blocker_id  bigint     NOT NULL,
                               blocked_id  bigint     NOT NULL,
                               created_at  timestamp  NOT NULL,
                               PRIMARY KEY (blocker_id, blocked_id),
                               CONSTRAINT fk_blocked_users_blocker FOREIGN KEY (blocker_id)
                                   REFERENCES users (user_id) ON DELETE CASCADE,
                               CONSTRAINT fk_blocked_users_blocked FOREIGN KEY (blocked_id)
                                   REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE conversations (
                               conversation_id  bigint     NOT NULL AUTO_INCREMENT,
                               user_a           bigint     NOT NULL,
                               user_b           bigint     NOT NULL,
                               created_at       timestamp  NOT NULL,
                               PRIMARY KEY (conversation_id),
                               CONSTRAINT fk_conversations_user_a FOREIGN KEY (user_a)
                                   REFERENCES users (user_id) ON DELETE CASCADE,
                               CONSTRAINT fk_conversations_user_b FOREIGN KEY (user_b)
                                   REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE messages (
                          message_id       bigint     NOT NULL AUTO_INCREMENT,
                          conversation_id  bigint     NOT NULL,
                          sender_id        bigint     NOT NULL,
                          content          text       NOT NULL,
                          sent_at          timestamp  NOT NULL,
                          PRIMARY KEY (message_id),
                          CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id)
                              REFERENCES conversations (conversation_id) ON DELETE CASCADE,
                          CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id)
                              REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------- map

CREATE TABLE map_data (
                          map_id      bigint        NOT NULL AUTO_INCREMENT,
                          owner_id    bigint        NOT NULL,
                          map_name    varchar(100)  NOT NULL,
                          image_ref   varchar(500)  NOT NULL,
                          status      varchar(10)   NOT NULL,
                          version     int           NOT NULL,
                          created_at  timestamp     NOT NULL,
                          deleted_at  timestamp     NULL,
                          PRIMARY KEY (map_id),
                          CONSTRAINT fk_map_data_owner FOREIGN KEY (owner_id)
                              REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE regions (
                         region_id  bigint        NOT NULL AUTO_INCREMENT,
                         map_id     bigint        NOT NULL,
                         name       varchar(100)  NOT NULL,
                         polygon    json          NOT NULL,
                         PRIMARY KEY (region_id),
                         UNIQUE KEY uq_regions_map_region (map_id, region_id),
                         CONSTRAINT fk_regions_map FOREIGN KEY (map_id)
                             REFERENCES map_data (map_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------ game history

CREATE TABLE user_games (
                            game_id            bigint     NOT NULL AUTO_INCREMENT,
                            user_id            bigint     NOT NULL,
                            map_id             bigint     NOT NULL,
                            started_at         timestamp  NOT NULL,
                            finished_at        timestamp  NULL,
                            duration           int        NULL,
                            correct_guesses    int        NOT NULL,
                            incorrect_guesses  int        NOT NULL,
                            PRIMARY KEY (game_id),
                            UNIQUE KEY uq_user_games_game_map (game_id, map_id),
                            CONSTRAINT fk_user_games_user FOREIGN KEY (user_id)
                                REFERENCES users (user_id) ON DELETE CASCADE,
                            CONSTRAINT fk_user_games_map FOREIGN KEY (map_id)
                                REFERENCES map_data (map_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE guesses (
                         game_id             bigint     NOT NULL,
                         attempt_number      int        NOT NULL,
                         map_id              bigint     NOT NULL,
                         target_region_id    bigint     NOT NULL,
                         selected_region_id  bigint     NULL,
                         clicked_lat         double     NOT NULL,
                         clicked_lng         double     NOT NULL,
                         guessed_at          timestamp  NOT NULL,
                         PRIMARY KEY (game_id, attempt_number),
                         CONSTRAINT fk_guesses_game FOREIGN KEY (game_id, map_id)
                             REFERENCES user_games (game_id, map_id) ON DELETE CASCADE,
                         CONSTRAINT fk_guesses_target FOREIGN KEY (map_id, target_region_id)
                             REFERENCES regions (map_id, region_id) ON DELETE CASCADE,
                         CONSTRAINT fk_guesses_selected FOREIGN KEY (map_id, selected_region_id)
                             REFERENCES regions (map_id, region_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE leaderboard (
                             map_id       bigint        NOT NULL,
                             user_id      bigint        NOT NULL,
                             game_id      bigint        NOT NULL,
                             accuracy     decimal(5,2)  NOT NULL,
                             duration     int           NOT NULL,
                             achieved_at  timestamp     NOT NULL,
                             PRIMARY KEY (map_id, user_id),
                             UNIQUE KEY uq_leaderboard_game (game_id),
                             CONSTRAINT fk_leaderboard_map FOREIGN KEY (map_id)
                                 REFERENCES map_data (map_id) ON DELETE RESTRICT,
                             CONSTRAINT fk_leaderboard_user FOREIGN KEY (user_id)
                                 REFERENCES users (user_id) ON DELETE CASCADE,
                             CONSTRAINT fk_leaderboard_game FOREIGN KEY (game_id)
                                 REFERENCES user_games (game_id) ON DELETE CASCADE
) ENGINE=InnoDB;