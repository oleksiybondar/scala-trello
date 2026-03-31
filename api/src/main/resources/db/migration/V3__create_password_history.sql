CREATE TABLE password_history (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  password_hash TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX password_history_user_id_idx ON password_history(user_id);
CREATE INDEX password_history_created_at_idx ON password_history(created_at);
