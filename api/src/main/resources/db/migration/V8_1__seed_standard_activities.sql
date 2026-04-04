INSERT INTO activities (id, code, name, description)
VALUES
  (1, 'code_review', 'Code Review', 'Reviewing implementation changes.'),
  (2, 'development', 'Development', 'Implementing product or technical changes.'),
  (3, 'testing', 'Testing', 'Verifying behavior through testing activities.'),
  (4, 'planning', 'Planning', 'Planning or task breakdown work.'),
  (5, 'design', 'Design', 'Technical or product design work.'),
  (6, 'documentation', 'Documentation', 'Writing or updating documentation.'),
  (7, 'refinement', 'Refinement', 'Refining scope or requirements.'),
  (8, 'debugging', 'Debugging', 'Diagnosing and fixing issues.');

ALTER TABLE activities ALTER COLUMN id RESTART WITH 9;
