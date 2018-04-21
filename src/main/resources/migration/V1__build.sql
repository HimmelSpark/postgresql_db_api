CREATE EXTENSION IF NOT EXISTS citext;

DROP TABLE IF EXISTS votes;
DROP TABLE IF EXISTS posts;
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
  created TIMESTAMP WITH TIME ZONE,
  forum citext,
  message citext NOT NULL,
  slug citext UNIQUE,
  title citext NOT NULL,
  votes NUMERIC DEFAULT 0,
  FOREIGN KEY (author) REFERENCES users(nickname),
  FOREIGN KEY (forum) REFERENCES forums(slug)
);


CREATE TABLE posts (
  id SERIAL NOT NULL PRIMARY KEY ,
  author citext ,
  created TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP),
  forum citext ,
  isEdited BOOLEAN DEFAULT FALSE ,
  message citext ,
  parent INTEGER DEFAULT 0,
  thread INTEGER NOT NULL DEFAULT 0,
  path INTEGER [],
  FOREIGN KEY (author) REFERENCES users(nickname),
  FOREIGN KEY (forum)  REFERENCES forums(slug),
--   FOREIGN KEY (parent) REFERENCES posts(id),
  FOREIGN KEY (thread) REFERENCES threads(id)
);

CREATE TABLE votes (
  nickname citext NOT NULL ,
  thread INTEGER,
  voice INTEGER,
  UNIQUE (nickname, thread),
  FOREIGN KEY (thread) REFERENCES threads(id),
  FOREIGN KEY (nickname) REFERENCES users(nickname)
);

-- SELECT U.id, U.about, U.email, U.nickname, U.fullname FROM forums F
-- JOIN users U ON (F.creator = U.nickname)
-- WHERE F.slug='2KMK_EPM8_6es'::citext AND U.nickname<>?::citext;


-- SELECT * FROM users
-- JOIN forums f ON users.nickname = f.creator and f.slug = 'bN-ktesJRB-xK';
--
--
-- SELECT DISTINCT U.nickname, U.about, U.email, U.fullname FROM users U
-- JOIN posts p ON U.nickname = p.author AND p.forum='bN-ktesJRB-xK'
-- UNION
-- SELECT DISTINCT U.nickname, U.about, U.email, U.fullname FROM users U
-- JOIN threads t ON U.nickname = t.author AND t.forum = 'bN-ktesJRB-xK'

-- SELECT DISTINCT U.nickname, U.about, U.email, U.fullname FROM users U
-- JOIN posts p ON U.nickname = p.author AND p.forum='oC4pR6898H-e8'
-- WHERE lower(nickname) < lower('PQg27MpSra6Dr.Jill')
-- UNION
-- SELECT DISTINCT U.nickname, U.about, U.email, U.fullname FROM users U
-- JOIN threads t ON U.nickname = t.author AND t.forum ='oC4pR6898H-e8'
-- WHERE lower(nickname) < lower('PQg27MpSra6Dr.Jill')
-- ORDER BY nickname DESC LIMIT 4;


-- SELECT DISTINCT U.nickname, U.about, U.email, U.fullname FROM users U
--   JOIN posts p ON U.nickname = p.author AND p.forum='nsH1fyvUkBiVr'
-- UNION
-- SELECT DISTINCT U.nickname, U.about, U.email, U.fullname FROM users U
--   JOIN threads t ON U.nickname = t.author AND t.forum ='nsH1fyvUkBiVr'
-- ORDER BY nickname LIMIT 100;
--
--
-- SELECT DISTINCT U.nickname, U.about, U.email, U.fullname FROM users U
--   JOIN posts p ON U.nickname = p.author AND p.forum=?
-- UNION
-- SELECT DISTINCT U.nickname, U.about, U.email, U.fullname FROM users U
--   JOIN threads t ON U.nickname = t.author AND t.forum =?
-- ORDER BY nickname LIMIT ?;
--
-- SELECT DISTINCT U.nickname, U.about, U.email, U.fullname FROM users U JOIN posts p ON U.nickname = p.author AND p.forum='nsH1fyvUkBiVr' UNION SELECT DISTINCT U.nickname, U.about, U.email, U.fullname FROM users U JOIN threads t ON U.nickname = t.author AND t.forum ='nsH1fyvUkBiVr' ORDER BY nickname LIMIT 100