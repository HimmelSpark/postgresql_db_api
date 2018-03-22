package com.dbapi.adamyan.DAO;

import com.dbapi.adamyan.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.sql.ResultSet;
import java.sql.SQLException;


@Service
@Transactional
public class UserDAO {
    private final JdbcTemplate jdbc;

    @Autowired
    public UserDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static UserMapper userMapper = new UserMapper();

    public void createUser(User user) {
        String query = "INSERT INTO users (about, email, fullname, nickname) VALUES (?, ?, ?, ?)";
        jdbc.update(query, user.getAbout(), user.getEmail(), user.getFullname(), user.getNickname());
    }

    public User getUserByNickname(String nickname) {
        String query = "SELECT * FROM users WHERE nickname=?::citext";
        try {
            return jdbc.queryForObject(query, userMapper, nickname);
        } catch (Exception e) {
            return null;
        }
    }

    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email=?::citext";
        try {
            return jdbc.queryForObject(query, userMapper, email);
        } catch (Exception e) {
            return null;
        }
    }

    public void updateUser(User user) {
        String query = "UPDATE users SET about=?::citext, email=?::citext, fullname=?::citext WHERE nickname=?::citext";
        jdbc.update(query, user.getAbout(), user.getEmail(), user.getFullname(), user.getNickname());
    }

    private static class UserMapper implements RowMapper<User> {
        public User mapRow(ResultSet result, int rowNum) throws SQLException {
            User user = new User(
                    result.getString("about"),
                    result.getString("email"),
                    result.getString("fullname"),
                    result.getString("nickname")
            );
            return user;
        }
    }

}