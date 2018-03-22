package com.dbapi.adamyan.DAO;


import com.dbapi.adamyan.Model.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ThreadDAO {
    private final JdbcTemplate jdbc;

    @Autowired
    public ThreadDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void createThread(Thread thread, String forum) {
        String query = "INSERT INTO threads (author, created, message, title, forum) VALUES (?,?,?,?)";
        jdbc.update(query, thread.getAuthor(), thread.getCreated(), thread.getMessage(), thread.getTitle(), forum);
    }
}
