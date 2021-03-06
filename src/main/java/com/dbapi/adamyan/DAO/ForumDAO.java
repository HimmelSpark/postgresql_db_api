package com.dbapi.adamyan.DAO;

import com.dbapi.adamyan.Model.Forum;
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
        String query = "INSERT INTO forums (slug, title, creator) VALUES (?::citext,?::citext,?)";
        jdbc.update(query, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public Forum getForumBySlug(String slug) {
        String query = "SELECT posts, slug, threads, title, creator FROM forums WHERE slug=?::citext;";
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
            return jdbc.queryForObject(query, Forum.class, nickname);
        } catch (Exception e) {
            return null;
        }
    }

    public Forum getForumByThreadId(Integer id) {
        String query =
                "SELECT F.posts, F.slug, F.threads, F.title, F.creator FROM posts " +
                "JOIN threads ON (posts.thread = threads.id) " +
                "JOIN forums AS F ON (threads.forum = F.slug) " +
                "WHERE posts.thread = ?"; //TODO:: запрос ничего не вернул! исправить!
        try {
            return jdbc.queryForObject(query, forumMapper, id);
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
                    result.getString("creator")
            );
        }
    }
}

