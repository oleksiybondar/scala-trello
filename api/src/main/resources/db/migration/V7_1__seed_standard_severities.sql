INSERT INTO severities (id, name, description)
VALUES
  (1, 'minor', 'Low impact issue or task.'),
  (2, 'normal', 'Standard impact issue or task.'),
  (3, 'major', 'High impact issue or task.');

ALTER TABLE severities ALTER COLUMN id RESTART WITH 4;
