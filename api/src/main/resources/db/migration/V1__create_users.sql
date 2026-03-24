CREATE TABLE users (
    id UUID PRIMARY KEY,

    username VARCHAR(200),
    email VARCHAR(320),

    password_hash TEXT,

    first_name VARCHAR(200) NOT NULL,
    last_name VARCHAR(200) NOT NULL,

    avatar_url TEXT,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT username_or_email_required
       CHECK (username IS NOT NULL OR email IS NOT NULL)
);

CREATE UNIQUE INDEX users_username_unique
    ON users (username)
    WHERE username IS NOT NULL;

CREATE UNIQUE INDEX users_email_unique
    ON users (email)
    WHERE email IS NOT NULL;