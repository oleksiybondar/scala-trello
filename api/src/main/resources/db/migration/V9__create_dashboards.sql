CREATE TABLE dashboards (
  id UUID PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  description TEXT,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  owner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_by_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ NOT NULL,
  modified_at TIMESTAMPTZ NOT NULL,
  last_modified_by_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX dashboards_owner_user_id_idx ON dashboards(owner_user_id);
CREATE INDEX dashboards_created_by_user_id_idx ON dashboards(created_by_user_id);
CREATE INDEX dashboards_last_modified_by_user_id_idx ON dashboards(last_modified_by_user_id);
CREATE INDEX dashboards_active_idx ON dashboards(active);
