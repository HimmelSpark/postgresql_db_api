package com.dbapi.adamyan.DAO;


import com.dbapi.adamyan.Model.Thread;
import com.dbapi.adamyan.Model.User;
import com.dbapi.adamyan.Model.Vote;
import org.omg.PortableServer.THREAD_POLICY_ID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ThreadDAO {
    private final JdbcTemplate jdbc;

    @Autowired
    public ThreadDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static ThreadMapper threadMapper = new ThreadMapper();

    public Integer getIdByThreadSlug(String thread_slug) {
        String query = "SELECT id FROM threads WHERE slug=?::citext";
        try {
            return jdbc.queryForObject(query, Integer.class, thread_slug);
        } catch (Exception e) {
            return null;
        }
    }

    public Thread createThread(Thread thread) {
        String query =
                "INSERT INTO threads " +
                "(author, created, message, title, forum, slug) " +
                "VALUES (?,?,?,?,?::citext,?) RETURNING id";
        int result = jdbc.queryForObject(query, Integer.class,
                thread.getAuthor(),
                thread.getCreated(),
                thread.getMessage(),
                thread.getTitle(),
                thread.getForum(),
                thread.getSlug()
        );
        thread.setId(result);
        incrementThreadsCountInForum(thread);
        return thread;
    }

    public void updateForum_Users(String slug, User user) {
        String sql = "INSERT INTO forum_users (slug, about, email, fullname, nickname) VALUES (?::citext, ?::citext, ?::citext, ?::citext, ?::citext) ON CONFLICT DO NOTHING";
        try {
            jdbc.update(sql, slug, user.getAbout(), user.getEmail(), user.getFullname(), user.getNickname());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void incrementThreadsCountInForum(Thread thread) {
        String sql = "UPDATE forums SET threads = threads + 1 WHERE slug = ?";
        jdbc.update(sql, thread.getForum());
    }

    public Thread getThreadBySlug(String slug) {
        String query = "SELECT * FROM threads WHERE slug=?::citext";
        try {
            return jdbc.queryForObject(query, threadMapper, slug);
        } catch (Exception e) {
            return null;
        }
    }

    public Thread getThreadById(Integer id) {
        String query = "SELECT * FROM threads WHERE id=?";
        try {
            return jdbc.queryForObject(query, threadMapper, id);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public List<Thread> getAllThreadsByForum(String slug, Integer limit, String since, Boolean desc) {
        String query = "SELECT id, author, created, forum, message, slug, title, votes FROM \"threads\" WHERE forum=?::citext ";

        List<Object> params = new ArrayList<>();
        params.add(slug);

        if (since != null) {
            if (desc != null && desc) {
                query += "AND created <= ?::timestamptz ";
            }
            else  {
                query += "AND created >= ?::timestamptz ";
            }
            params.add(since);
        }
        query += "ORDER BY created ";
        //TODO: исправить govnocode
        if (desc != null && desc) {
            query += "DESC ";
        }

        if (limit != null) {
            query += "LIMIT (?) ";
            params.add(limit);
        }
        query+=";";
        return jdbc.query(query, threadMapper, params.toArray());
    }

    public void doVote(Integer voice, String nickname, Integer thread_id) {
        String sql = "INSERT INTO votes (nickname, thread, voice) VALUES (?::citext,?,?)";
        jdbc.update(sql, nickname, thread_id, voice);
        updateThreadVote(voice, thread_id);
    }

    public void updateThreadVote(Integer dVote, Integer thread_id) {
        String sql = "UPDATE threads SET votes = votes + ? WHERE id=?";
        jdbc.update(sql, dVote, thread_id);
    }


    public Thread updateThread(String slug, Integer thread_id, Thread newThread) {
        String sql = "UPDATE threads SET " +
                "author=COALESCE(?, author), " +
                "created=coalesce(?, created), " +
                "forum=coalesce(?, forum), " +
                "message=coalesce(?, message), " +
//                "slug=coalesce(?, slug), " +
                "title=coalesce(?, title), " +
                "votes=coalesce(?, votes) ";

        if (slug != null) {
            sql += "WHERE slug=?::citext RETURNING *;";
            ArrayList params = new ArrayList();
            params.add(newThread.getAuthor());
            params.add(newThread.getCreated());
            params.add(newThread.getForum());
            params.add(newThread.getMessage());
            params.add(newThread.getTitle());
            params.add(newThread.getVotes());
            params.add(slug);
            return jdbc.queryForObject(
                    sql,
                    params.toArray(),
                    threadMapper
            );

        } else {
            sql += "WHERE id=? RETURNING *;";
            ArrayList params = new ArrayList();
            params.add(newThread.getAuthor());
            params.add(newThread.getCreated());
            params.add(newThread.getForum());
            params.add(newThread.getMessage());
            params.add(newThread.getTitle());
            params.add(newThread.getVotes());
            params.add(thread_id);
            return jdbc.queryForObject(
                    sql,
                    params.toArray(),
                    threadMapper
            );
        }
    }

    public Thread getOneThread(String slug, Integer thread_id) {
        String sql = "SELECT * FROM threads WHERE ";
        if (slug != null) {
            sql += "slug=?::citext;";
            try {
                return jdbc.queryForObject(sql, threadMapper, slug);
            } catch (DataAccessException e) {
                return null;
            }
        } else {
            sql += "id=?";
            try {
                return jdbc.queryForObject(sql, threadMapper, thread_id);
            } catch (DataAccessException e) {
                return null;
            }
        }
    }

    public static class ThreadMapper implements RowMapper<Thread> {
        public Thread mapRow(ResultSet result, int rowNum) throws SQLException {
            return new Thread(
                    result.getString("author"),
                    result.getTimestamp("created"),
                    result.getString("forum"),
                    result.getString("message"),
                    result.getString("slug"),
                    result.getString("title"),
                    result.getInt("votes"),
                    result.getInt("id")
            );
        }
    }
}
