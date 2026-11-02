-- Persistent state for active Core Quiz Gameplay sessions.

CREATE TABLE quiz_sessions (
    session_id                varchar(36)  NOT NULL,
    map_id                    bigint       NOT NULL,
    current_target_region_id  bigint       NOT NULL,
    correct_answers           int          NOT NULL,
    answered_targets          int          NOT NULL,
    correction_required       boolean      NOT NULL,
    status                    varchar(20)  NOT NULL,

    PRIMARY KEY (session_id),

    CONSTRAINT fk_quiz_sessions_map
        FOREIGN KEY (map_id)
        REFERENCES map_data (map_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_quiz_sessions_current_target
        FOREIGN KEY (map_id, current_target_region_id)
        REFERENCES regions (map_id, region_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;


CREATE TABLE quiz_session_targets (
    session_id         varchar(36)  NOT NULL,
    target_position    int          NOT NULL,    
    region_id          bigint       NOT NULL,
    completed          boolean      NOT NULL,

    PRIMARY KEY (session_id, target_position),

    UNIQUE KEY uq_quiz_session_target (
        session_id,
        region_id
    ),

    CONSTRAINT fk_quiz_session_targets_session
        FOREIGN KEY (session_id)
        REFERENCES quiz_sessions (session_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_quiz_session_targets_region
        FOREIGN KEY (region_id)
        REFERENCES regions (region_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;