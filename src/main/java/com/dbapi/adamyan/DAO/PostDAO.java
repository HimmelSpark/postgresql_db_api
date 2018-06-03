package com.dbapi.adamyan.DAO;

import com.dbapi.adamyan.Model.Post;
import com.dbapi.adamyan.Model.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class PostDAO {
    private final JdbcTemplate jdbc;

    @Autowired
    PostDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static PostMapper postMapper = new PostMapper();

    public Post getPostById(Integer id) {
        String query = "SELECT * FROM posts WHERE id=?";
        try {
            return jdbc.queryForObject(query, postMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Post getPostInThread(Thread thread, Integer id) {
        String sql = "SELECT * FROM posts WHERE thread=? AND id=?";
        try {
            return jdbc.queryForObject(sql, postMapper, thread.getId(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Post> createPostsByThreadId(List<Post> posts, Integer thread_id) {
        Integer num = 0;
        String query = null;
        for (Post post : posts) {
            query = "INSERT INTO posts (author, created, forum, isedited, message, parent, thread) VALUES (?, ?::timestamptz , ?, ?, ?, ?, ?) RETURNING *; ";
            posts.set(num, jdbc.queryForObject(query, postMapper, post.getAuthor(), post.getCreated(), post.getForum(), post.getIsedited(), post.getMessage(), post.getParent(), post.getThread()));
        }
        return posts;
    }

    public Integer createPosts(List<Post> posts, Thread thread) {
        for (Post post : posts) {
            post.setCreated(posts.get(0).getCreated());
            post.setThread(thread.getId());
            post.setForum(thread.getForum());
            Post parent = getPostById(post.getParent());

            // Если нет родителя и он сам не корневой, то кака
            // Либо есть родитель, но не совпали треды
            if ((parent == null && post.getParent() != 0) || (parent != null && !parent.getThread().equals(post.getThread()))) {
                return 409;
            }

            String query = "INSERT INTO posts (author, created, forum, message, parent, thread) " +
                    "VALUES (?, ?::TIMESTAMP WITH TIME ZONE, ?, ?, ?, ?) RETURNING id";
            Integer id;
            try {
                id = jdbc.queryForObject(query, new Object[] {post.getAuthor(), post.getCreated(), post.getForum(), post.getMessage(), post.getParent(), post.getThread()}, Integer.class);
            } catch (DataAccessException e) {
                return 404;
            }
            post.setId(id);
            setPostsPath(parent, post);

            String incrementPostsCount = "UPDATE forums set posts = posts + 1 WHERE slug = ?";
            jdbc.update(incrementPostsCount, post.getForum());
        }
        return 201;
    }

    private void setPostsPath(Post chuf, Post body) {
        jdbc.update(con -> {
            PreparedStatement pst = con.prepareStatement(
                    "update posts set" +
                            "  path = ? " +
                            "where id = ?");
            if (body.getParent() == 0) {
                pst.setArray(1, con.createArrayOf("INT", new Object[]{body.getId()}));//String.valueOf(body.getId()));
            } else {
                ArrayList arr = new ArrayList<Object>(Arrays.asList(chuf.getChildren()));
                arr.add(body.getId());
                pst.setArray(1, con.createArrayOf("INT", arr.toArray()));//chuf.getPath() + "-" + String.valueOf(body.getId()));
            }
            pst.setLong(2, body.getId());
            return pst;
        });

    }

    private void setMultiplePostsPath(List<Post> parents, List<Post> posts) {

        System.out.println(posts);

        Connection connection;
        try {
            connection = jdbc.getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement("UPDATE posts SET  path = ? WHERE id = ?");
            for (int i = 0; i < posts.size(); i++) {
                if (posts.get(i).getParent() == 0) {
                    pst.setArray(1, connection.createArrayOf("INT", new Object[]{posts.get(i).getId()}));
                } else {
                    ArrayList arr = new ArrayList<Object>(Arrays.asList(parents.get(i).getChildren()));
                    arr.add(posts.get(i).getId());
                    pst.setArray(1, connection.createArrayOf("INT", arr.toArray()));
                }
                System.out.println(posts.get(i).getId());
                pst.setLong(2, posts.get(i).getId());
                pst.addBatch();
            }
            pst.executeBatch();
        } catch (SQLException e) {
            return;
        }
    }

    public List<Post> getPostsOfThread(Thread thread, Integer limit, Integer since, String sort, Boolean desc) {
        ArrayList<Object> params = new ArrayList<>();

        if (sort.equals("flat")) {
            String sql = "SELECT * FROM posts WHERE thread=? ";
            params.add(thread.getId());
            if (since != null) {
                if (desc) {
                    sql += "AND id < ?";
                } else {
                    sql += "AND id > ?";
                }
                params.add(since);
            }

            sql += "ORDER BY created ";

            if (desc != null && desc) {
                sql += "DESC, id DESC ";
            } else {
                sql += ", id ";
            }

            if (limit != null) {
                sql += "LIMIT ?";
                params.add(limit);
            }

            return jdbc.query(sql, postMapper, params.toArray());
        } else if (sort.equals("tree")) {
            String sql = "SELECT * FROM posts WHERE thread=? ";
            params.add(thread.getId());

            if (since != null) {
                if (desc) {
                    sql += " AND path < (SELECT path FROM posts WHERE id=?) ";
                } else {
                    sql += " AND path > (SELECT path FROM posts WHERE id=?) ";
                }
                params.add(since);
            }

            sql += "ORDER BY path ";

            if (desc != null && desc) {
                sql += "DESC, id DESC ";
            }

            if (limit != null) {
                sql += "LIMIT ?;";
                params.add(limit);
            }

            return jdbc.query(sql, postMapper, params.toArray());
        } else {
            String sql = "SELECT * FROM posts JOIN ";
            if (since != null) {
                if (desc) {
                    if (limit != null) {
                        sql += "  (SELECT id FROM posts WHERE parent = 0 AND thread = ? AND path[1] < (SELECT path[1] FROM posts WHERE id = ?) ORDER BY path DESC, thread DESC LIMIT ?) AS selected ON (thread = ? AND selected.id = path[1]) ORDER BY path[1] DESC, path";
                    }
                } else {
                    if (limit != null) {
                        sql += "  (SELECT id FROM posts WHERE parent = 0 AND thread = ? AND path > (SELECT path FROM posts WHERE id = ?) ORDER BY id LIMIT ?) AS selected ON (thread = ? AND selected.id = path[1]) ORDER BY path";
                    }
                }
                params.add(thread.getId());
                params.add(since);
                params.add(limit);
                params.add(thread.getId());
            } else if (limit != null) {
                if (desc) {
                    sql += " (SELECT id FROM posts WHERE parent = 0 AND thread = ? ORDER BY path desc LIMIT ? ) AS selected ON (selected.id = path[1] AND thread = ?) ORDER BY path[1] DESC, path";
                } else {
                    sql += " (SELECT id FROM posts WHERE parent = 0 AND thread = ? ORDER BY id LIMIT ? ) AS selected ON (thread = ? AND selected.id = path[1]) ORDER BY path";
                }
                params.add(thread.getId());
                params.add(limit);
                params.add(thread.getId());
            }

//            sql += "ORDER BY path ";
//            if (desc) {
//                sql += " DESC ";
//            }
//            sql += " , thread ";
//            if (desc) {
//                sql += " DESC ";
//            }
            return jdbc.query(sql, params.toArray(), postMapper);
//            List<Object> insertionArr = new ArrayList<>();
//
//            String sql = "SELECT * FROM posts JOIN ";
//            desc = (desc != null) && desc;
//            if (since == null) {
//                if (desc) {
//                    if (limit != null) {
//                        sql += " (SELECT id FROM posts WHERE parent = 0 AND thread = ? " +
//                                " ORDER BY path desc LIMIT ? ) AS selected ON (selected.id = path[1] AND thread = ?) ORDER BY path[1] DESC, path";
//                        insertionArr.add(thread.getId());
//                        insertionArr.add(limit);
//                        insertionArr.add(thread.getId());
//                    }
//                }
//                else {
//                    if (limit != null) {
//                        sql += " (SELECT id FROM posts WHERE parent = 0 AND thread = ? " +
//                                " ORDER BY id LIMIT ? ) AS selected ON (thread = ? AND selected.id = path[1]) ORDER BY path";
//                        insertionArr.add(thread.getId());
//                        insertionArr.add(limit);
//                        insertionArr.add(thread.getId());
//                    }
//                }
//            }
//            else {
//                if (desc) {
//                    if (limit != null) {
//                        sql += "  (SELECT id FROM posts WHERE parent = 0 AND thread = ?" +
//                                "AND path[1] < (SELECT path[1] FROM posts WHERE id = ?)" +
//                                "ORDER BY path DESC, thread DESC LIMIT ?)" +
//                                "AS selected ON (thread = ? AND selected.id = path[1])" +
//                                "ORDER BY path[1] DESC, path";
//                        insertionArr.add(thread.getId());
//                        insertionArr.add(since);
//                        insertionArr.add(limit);
//                        insertionArr.add(thread.getId());
//                    }
//                }
//                else {
//                    if (limit != null) {
//                        sql += "  (SELECT id FROM posts WHERE parent = 0 AND thread = ?" +
//                                "AND path > (SELECT path FROM posts WHERE id = ?) ORDER BY id LIMIT ?)" +
//                                "AS selected ON (thread = ? AND selected.id = path[1]) ORDER BY path";
//                        insertionArr.add(thread.getId());
//                        insertionArr.add(since);
//                        insertionArr.add(limit);
//                        insertionArr.add(thread.getId());
//                    }
//                }
//            }
//
//            return jdbc.query(sql, insertionArr.toArray(), postMapper);
        }
    }

    public void updatePost(Integer id, Post post) {
        String sql = "UPDATE posts SET message = ?::citext, isedited=TRUE WHERE id = ?";
        jdbc.update(sql, post.getMessage(), id);
    }

    public static class PostMapper implements RowMapper<Post> {
        @Override
        public Post mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            Array arrayList = resultSet.getArray("path");
            Object[] objects = new Object[]{};
            if (arrayList != null) {
                objects = (Object[]) arrayList.getArray();
            }
            return new Post(
                    resultSet.getInt("id"),
                    resultSet.getString("author"),
                    resultSet.getTimestamp("created").toInstant().toString(),
                    resultSet.getString("forum"),
                    resultSet.getBoolean("isEdited"),
                    resultSet.getString("message"),
                    resultSet.getInt("parent"),
                    resultSet.getInt("thread"),
//                    (Object[]) resultSet.getArray("children").getArray()
                    objects
            );
        }
    }
}