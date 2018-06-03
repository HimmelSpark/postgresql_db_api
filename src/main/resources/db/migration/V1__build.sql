CREATE EXTENSION IF NOT EXISTS citext;

DROP TABLE IF EXISTS forum_users;
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

CREATE TABLE forum_users (
  slug citext NOT NULL,
  about citext,
  email citext NOT NULL,
  fullname citext NOT NULL,
  nickname citext NOT NULL,
  UNIQUE (slug, nickname)
);


-- CREATE INDEX IF NOT EXISTS posts_parent_thread_idx on posts(parent, thread);
DROP INDEX IF EXISTS posts_parent_thread_idx;
CREATE INDEX IF NOT EXISTS posts_parent_thread_idx on posts(thread, parent);
-- добавить по ловерам
DROP INDEX IF EXISTS posts_flat;
CREATE INDEX IF NOT EXISTS posts_flat on posts(thread, created, id);
--
DROP INDEX IF EXISTS threads_by_forum;
CREATE INDEX IF NOT EXISTS threads_by_forum on threads(forum, created);
--
DROP INDEX IF EXISTS threads_slug;
CREATE INDEX IF NOT EXISTS threads_slug on threads(slug);
-- forum users
DROP INDEX IF EXISTS  forum_users_slug;
CREATE INDEX IF NOT EXISTS forum_users_slug ON forum_users(slug);
CLUSTER forum_users USING forum_users_slug;

-- users of forum
DROP INDEX IF EXISTS users_lower_nickname;
CREATE INDEX users_lower_nickname on users(lower(nickname));

