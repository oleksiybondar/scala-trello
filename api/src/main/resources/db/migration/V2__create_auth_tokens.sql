CREATE TABLE auth_tokens (
  token VARCHAR PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_type VARCHAR NOT NULL,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX auth_tokens_user_id_idx ON auth_tokens(user_id);
CREATE INDEX auth_tokens_token_type_idx ON auth_tokens(token_type);
