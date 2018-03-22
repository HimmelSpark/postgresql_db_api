DROP TABLE IF EXISTS thread;
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
  creator citext,
  FOREIGN KEY (creator) REFERENCES users(nickname)
);


CREATE TABLE thread (
  id SERIAL NOT NULL PRIMARY KEY,
  author citext NOT NULL , -- ref users.nickname
  created TIMESTAMP,
  forum citext, -- ref forum.id
  message citext NOT NULL,
  slug citext UNIQUE ,
  title citext NOT NULL,
  votes NUMERIC DEFAULT 0,
  FOREIGN KEY (forum) REFERENCES forums(slug)
);

SELECT * FROM users;

SELECT * FROM forums WHERE slug='pirates';
