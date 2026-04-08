INSERT INTO roles (name, description)
SELECT 'admin', 'Full dashboard access including member management.'
WHERE NOT EXISTS (
  SELECT 1 FROM roles WHERE name = 'admin'
);

INSERT INTO roles (name, description)
SELECT 'contributor', 'Can contribute to tickets and comments.'
WHERE NOT EXISTS (
  SELECT 1 FROM roles WHERE name = 'contributor'
);

INSERT INTO roles (name, description)
SELECT 'viewer', 'Read-only access to dashboard data.'
WHERE NOT EXISTS (
  SELECT 1 FROM roles WHERE name = 'viewer'
);

INSERT INTO permissions (role_id, area, can_read, can_create, can_modify, can_delete, can_reassign)
SELECT r.id, 'dashboard', TRUE, TRUE, TRUE, TRUE, TRUE
FROM roles r
WHERE r.name = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.role_id = r.id AND p.area = 'dashboard'
  );

INSERT INTO permissions (role_id, area, can_read, can_create, can_modify, can_delete, can_reassign)
SELECT r.id, 'ticket', TRUE, TRUE, TRUE, TRUE, TRUE
FROM roles r
WHERE r.name = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.role_id = r.id AND p.area = 'ticket'
  );

INSERT INTO permissions (role_id, area, can_read, can_create, can_modify, can_delete, can_reassign)
SELECT r.id, 'comment', TRUE, TRUE, TRUE, TRUE, FALSE
FROM roles r
WHERE r.name = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.role_id = r.id AND p.area = 'comment'
  );

INSERT INTO permissions (role_id, area, can_read, can_create, can_modify, can_delete, can_reassign)
SELECT r.id, 'dashboard', TRUE, FALSE, FALSE, FALSE, FALSE
FROM roles r
WHERE r.name = 'contributor'
  AND NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.role_id = r.id AND p.area = 'dashboard'
  );

INSERT INTO permissions (role_id, area, can_read, can_create, can_modify, can_delete, can_reassign)
SELECT r.id, 'ticket', TRUE, TRUE, TRUE, FALSE, TRUE
FROM roles r
WHERE r.name = 'contributor'
  AND NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.role_id = r.id AND p.area = 'ticket'
  );

INSERT INTO permissions (role_id, area, can_read, can_create, can_modify, can_delete, can_reassign)
SELECT r.id, 'comment', TRUE, TRUE, TRUE, FALSE, FALSE
FROM roles r
WHERE r.name = 'contributor'
  AND NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.role_id = r.id AND p.area = 'comment'
  );

INSERT INTO permissions (role_id, area, can_read, can_create, can_modify, can_delete, can_reassign)
SELECT r.id, 'dashboard', TRUE, FALSE, FALSE, FALSE, FALSE
FROM roles r
WHERE r.name = 'viewer'
  AND NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.role_id = r.id AND p.area = 'dashboard'
  );

INSERT INTO permissions (role_id, area, can_read, can_create, can_modify, can_delete, can_reassign)
SELECT r.id, 'ticket', TRUE, FALSE, FALSE, FALSE, FALSE
FROM roles r
WHERE r.name = 'viewer'
  AND NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.role_id = r.id AND p.area = 'ticket'
  );

INSERT INTO permissions (role_id, area, can_read, can_create, can_modify, can_delete, can_reassign)
SELECT r.id, 'comment', TRUE, FALSE, FALSE, FALSE, FALSE
FROM roles r
WHERE r.name = 'viewer'
  AND NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.role_id = r.id AND p.area = 'comment'
  );
