package com.dbapi.adamyan.DAO;


import com.dbapi.adamyan.Model.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    public void createThread(Thread thread) {
        String query = "INSERT INTO threads (author, created, message, title, forum, slug) VALUES (?,?,?,?,?,?);" +
                "UPDATE forums SET threads = threads + 1 WHERE slug =?::citext";
        jdbc.update(query, thread.getAuthor(), thread.getCreated(),
                thread.getMessage(), thread.getTitle(),
                thread.getForum(), thread.getSlug(), thread.getForum());
    }

    public Thread getThreadBySlug(String slug) {
        String query = "SELECT * FROM threads WHERE slug=?::citext";
        try {
            return jdbc.queryForObject(query, threadMapper, slug);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Thread> getAllThreadsByForum(String slug) {
        String query = "SELECT T.author, T.created, T.forum, T.message, T.slug, T.title, T.votes FROM threads as T " +
                "JOIN forums as F ON (F.slug = ?);"; //TODO: почему-то возвращает все объекты
        List<Object> params = new ArrayList<>();
        params.add(slug);
        try {
            return jdbc.query(query, params.toArray(), threadMapper);
        } catch (Exception e ) {
            return null;
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
                    result.getInt("votes")
            );
        }
    }
}
