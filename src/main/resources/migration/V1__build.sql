DROP TABLE IF EXISTS threads;
DROP TABLE IF EXISTS forums;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id SERIAL NOT NULL PRIMARY KEY,
  about citext,
  email citext NOT NULL UNIQUE ,
  fullname citext NOT NULL,
  nickname citext NOT NULL UNIQUE
);


CREATE TABLE forums (
  id SERIAL NOT NULL PRIMARY KEY,
  posts NUMERIC DEFAULT 0,
  slug citext NOT NULL UNIQUE,
  threads NUMERIC DEFAULT 0,
  title citext NOT NULL,
  creator citext NOT NULL ,
  FOREIGN KEY (creator) REFERENCES users(nickname)
);


CREATE TABLE threads (
  id SERIAL NOT NULL PRIMARY KEY,
  author citext NOT NULL ,
  created TIMESTAMP,
  forum citext,
  message citext NOT NULL,
  slug citext NOT NULL UNIQUE,
  title citext NOT NULL,
  votes NUMERIC DEFAULT 0,
  FOREIGN KEY (author) REFERENCES users(nickname),
  FOREIGN KEY (forum) REFERENCES forums(slug)
);

SELECT * FROM users;

SELECT * FROM forums ORDER BY threads DESC;

SELECT * FROM threads;

UPDATE forums SET threads = threads + 1 WHERE slug='pirates';