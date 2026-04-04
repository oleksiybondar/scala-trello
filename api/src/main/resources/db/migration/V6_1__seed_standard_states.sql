INSERT INTO states (id, name, description)
VALUES
  (1, 'new', 'Ticket has been created and is awaiting work.'),
  (2, 'in_progress', 'Work on the ticket is currently in progress.'),
  (3, 'code_review', 'Implementation is ready for review.'),
  (4, 'in_testing', 'Changes are being verified.'),
  (5, 'done', 'Work is completed.');

ALTER TABLE states ALTER COLUMN id RESTART WITH 6;
