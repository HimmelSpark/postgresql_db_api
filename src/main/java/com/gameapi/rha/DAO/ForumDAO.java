package com.gameapi.rha.DAO;

import com.gameapi.rha.Model.Forum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@Transactional
public class ForumDAO {
    private final JdbcTemplate jdbc;

    @Autowired
    ForumDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static ForumMapper forumMapper = new ForumMapper();

    public void createForum(Forum forum) {
        String query = "INSERT INTO forums (slug, title, creator) VALUES (?::citext,?::citext,?::citext)";
        jdbc.update(query, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public Forum getForumBySlug(String slug) {
        String query = "SELECT * FROM forums WHERE slug=?::citext";
        try {
            return jdbc.queryForObject(query, forumMapper, slug);
        } catch (Exception e) {
            return null;
        }
    }

    public Forum getForumByUser(String nickname) {
        String query =
                "SELECT posts, slug, threads, title, creator FROM forums " +
                "JOIN users ON forums.creator = users.nickname " +
                "WHERE creator=?::citext";
        try {
            return jdbc.queryForObject(query, forumMapper, nickname);
        } catch (Exception e) {
            return null;
        }
    }

    public static class ForumMapper implements RowMapper<Forum> {
        public Forum mapRow(ResultSet result, int rowNum) throws SQLException {
            return new Forum(
                    result.getInt("posts"),
                    result.getString("slug"),
                    result.getInt("threads"),
                    result.getString("title"),
                    result.getString("user")
            );
        }
    }
}

