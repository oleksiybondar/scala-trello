INSERT INTO roles (id, name, description)
VALUES
  (1, 'admin', 'Full dashboard access including member management.'),
  (2, 'contributor', 'Can contribute to tickets and comments.'),
  (3, 'viewer', 'Read-only access to dashboard data.');

ALTER TABLE roles ALTER COLUMN id RESTART WITH 4;
