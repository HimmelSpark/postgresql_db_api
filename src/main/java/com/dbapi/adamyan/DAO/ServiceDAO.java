package com.dbapi.adamyan.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServiceDAO {
    private final JdbcTemplate jdbc;

    @Autowired
    public ServiceDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Integer getForumCount() {
        String sql = "SELECT COUNT(*) FROM forums";
        return jdbc.queryForObject(sql, Integer.class);
    }

    public Integer getPostCount() {
        String sql = "SELECT COUNT(*) FROM posts";
        return jdbc.queryForObject(sql, Integer.class);
    }

    public Integer getThreadCount() {
        String sql = "SELECT COUNT(*) FROM threads";
        return jdbc.queryForObject(sql, Integer.class);
    }

    public Integer getUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        return jdbc.queryForObject(sql, Integer.class);
    }

    public void clearDB() {
        String sql = "TRUNCATE TABLE votes, posts, threads, forums, users";
        jdbc.execute(sql);
    }
}
