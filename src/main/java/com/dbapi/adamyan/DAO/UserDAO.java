package com.dbapi.adamyan.DAO;

import com.dbapi.adamyan.Model.User;
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

    public List<User> getDublicateUsers(User user) {
        String query = "SELECT * FROM users WHERE nickname=?::citext OR email=?::citext";
        List<Object> params = new ArrayList<>();
        params.add(user.getNickname());
        params.add(user.getEmail());
        try {
            return jdbc.query(query, params.toArray(), userMapper);
        } catch (Exception e) {
            return null;
        }
    }

    public void updateUser(User user) {
        String query = "UPDATE users SET " +
                "about=COALESCE(?, about), " +
                "email=COALESCE(?, email), " +
                "fullname=COALESCE(?, fullname) WHERE nickname=?::citext";
        jdbc.update(query, user.getAbout(), user.getEmail(), user.getFullname(), user.getNickname());
    }

    public List<User> getNotAllUsersByForumm(String since, String slug, Integer limit, Boolean desc) {
        List<Object> params = new ArrayList<>();
        String query =
                "SELECT DISTINCT U.nickname, U.about, U.email, U.fullname FROM users U " +
                "JOIN posts p ON U.nickname = p.author AND p.forum=?::citext ";
        params.add(slug);

        if (since != null) {
            if (desc == null || (desc != null && !desc)) {
                query += "WHERE lower(nickname) > lower(?) ";
                params.add(since);
            } else {
                query += "WHERE lower(nickname) < lower(?) ";
                params.add(since);
            }
        }

        query +=
                "UNION " +
                "SELECT DISTINCT U.nickname, U.about, U.email, U.fullname FROM users U " +
                "JOIN threads t ON U.nickname = t.author AND t.forum=?::citext ";
        params.add(slug);

        if (since != null) {
            if (desc == null || (desc != null && !desc)) {
                query += "WHERE lower(nickname) > lower(?) ";
                params.add(since);
            } else {
                query += "WHERE lower(nickname) < lower(?) ";
                params.add(since);
            }
        }

        query += "ORDER BY nickname ";
        if (desc != null && desc) {
            query += "DESC ";
        }

        if (limit != null) {
            query += "LIMIT ?";
            params.add(limit);
        }
        return jdbc.query(query, params.toArray(), userMapper);
    }

    public List<User> getNotAllUsersByForummm(String since, String slug, Integer limit, Boolean desc) {
        List<Object> params = new ArrayList<>();
        String sql = "U.about, U.email, U.fullname, U.nickname FROM forum_users JOIN users U ON forum_users.author = U.nickname\n" +
                "WHERE slug = ?::citext ";
        params.add(slug);
        if (since != null) {
            if (desc == null || (desc != null && !desc)) {
                sql += " AND lower(U.nickname) > lower(?) ";
                params.add(since);
            } else {
                sql += "AND lower(U.nickname) < lower(?) ";
                params.add(since);
            }
        }
        sql += "ORDER BY U.nickname ";
        if (desc != null && desc) {
            sql += "DESC ";
        }
        if (limit != null) {
            sql += "LIMIT ?";
            params.add(limit);
        }
        return jdbc.query(sql, params.toArray(), userMapper);
    }

    public List<User> getNotAllUsersByForum(String since, String slug, Integer limit, Boolean desc) {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT about, email, fullname, nickname FROM forum_users " +
                "WHERE slug = ?::citext ";
        params.add(slug);

        if (since != null) {
            if (desc == null || (desc != null && !desc)) {
                sql += " AND lower(nickname) > lower(?) ";
                params.add(since);
            } else {
                sql += "AND lower(nickname) < lower(?) ";
                params.add(since);
            }
        }

        sql += "ORDER BY nickname ";

        if (desc != null && desc) {
            sql += "DESC ";
        }
        if (limit != null) {
            sql += "LIMIT ? ";
            params.add(limit);
        }


        return jdbc.query(sql, params.toArray(), userMapper);
    }

    private static class UserMapper implements RowMapper<User> {
        public User mapRow(ResultSet result, int rowNum) throws SQLException {
            return new User(
                    result.getString("about"),
                    result.getString("email"),
                    result.getString("fullname"),
                    result.getString("nickname")
            );
        }
    }
}