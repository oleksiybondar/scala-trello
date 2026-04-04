CREATE TABLE dashboard_members (
  dashboard_id UUID NOT NULL REFERENCES dashboards(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (dashboard_id, user_id)
);

CREATE INDEX dashboard_members_user_id_idx ON dashboard_members(user_id);
CREATE INDEX dashboard_members_role_id_idx ON dashboard_members(role_id);
