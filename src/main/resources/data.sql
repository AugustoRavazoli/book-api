INSERT INTO book (title, description, isbn, published) VALUES
  ('The Lord of the Rings', 'Fantasy', '9780544003415', true),
  ('The Hobbit', 'Some detailed description', '9780008376055', false),
  ('The Silmarillion', 'description', '9780618391110', true),
  ('Design Patterns', 'Some description', '9780201633610', true);

INSERT INTO author (name, email) VALUES
  ('J.R.R. Tolkien', 'tolkien@example.com'),
  ('Erich Gamma', 'erich@example.com'),
  ('Richard Helm', 'richard@example.com'),
  ('Ralph Johnson', 'ralph@example.com'),
  ('John Vlissides', 'john@example.com');

INSERT INTO book_author (book_id, author_id) VALUES 
  (
    SELECT id FROM book WHERE title = 'The Lord of the Rings',
    SELECT id FROM author WHERE name = 'J.R.R. Tolkien'
  ),
  (
    SELECT id FROM book WHERE title = 'The Hobbit',
    SELECT id FROM author WHERE name = 'J.R.R. Tolkien'
  ),
  (
    SELECT id FROM book WHERE title = 'The Silmarillion',
    SELECT id FROM author WHERE name = 'J.R.R. Tolkien'
  ),
  (
    SELECT id FROM book WHERE title = 'Design Patterns',
    SELECT id FROM author WHERE name = 'Erich Gamma'
  ),
  (
    SELECT id FROM book WHERE title = 'Design Patterns',
    SELECT id FROM author WHERE name = 'Richard Helm'
  ),
  (
    SELECT id FROM book WHERE title = 'Design Patterns',
    SELECT id FROM author WHERE name = 'Ralph Johnson'
  ),
  (
    SELECT id FROM book WHERE title = 'Design Patterns',
    SELECT id FROM author WHERE name = 'John Vlissides'
  );
