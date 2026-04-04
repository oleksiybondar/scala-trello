INSERT INTO permissions (
  id,
  role_id,
  area,
  can_read,
  can_create,
  can_modify,
  can_delete,
  can_reassign
)
VALUES
  (1, 1, 'dashboard', TRUE, TRUE, TRUE, TRUE, TRUE),
  (2, 1, 'ticket', TRUE, TRUE, TRUE, TRUE, TRUE),
  (3, 1, 'comment', TRUE, TRUE, TRUE, TRUE, FALSE),
  (4, 2, 'dashboard', TRUE, FALSE, FALSE, FALSE, FALSE),
  (5, 2, 'ticket', TRUE, TRUE, TRUE, FALSE, TRUE),
  (6, 2, 'comment', TRUE, TRUE, TRUE, FALSE, FALSE),
  (7, 3, 'dashboard', TRUE, FALSE, FALSE, FALSE, FALSE),
  (8, 3, 'ticket', TRUE, FALSE, FALSE, FALSE, FALSE),
  (9, 3, 'comment', TRUE, FALSE, FALSE, FALSE, FALSE);

ALTER TABLE permissions ALTER COLUMN id RESTART WITH 10;
