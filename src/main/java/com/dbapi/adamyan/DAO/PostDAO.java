package com.dbapi.adamyan.DAO;

import com.dbapi.adamyan.Model.Post;
import com.dbapi.adamyan.Model.Thread;
import com.dbapi.adamyan.Model.User;
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

    public List<Post> getPostsById(List<Post> postList) {
        try (Connection connection = jdbc.getDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM posts WHERE id = ?");
            for (Post post : postList) {
                if (post.getParent() != 0) {
                    preparedStatement.setInt(1, post.getParent());
                    preparedStatement.addBatch();
                }
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Post> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(new Post(
                        resultSet.getInt("id"),
                        resultSet.getString("author"),
                        resultSet.getString("created"),
                        resultSet.getString("forum"),
                        resultSet.getBoolean("idEdited"),
                        resultSet.getString("message"),
                        resultSet.getInt("parent"),
                        resultSet.getInt("thread"),
                        new Object[]{resultSet.getArray("children")}
                ));
            }
            return result;
        } catch (SQLException e) {
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

    public Integer createPostss(List<Post> posts, Thread thread, List<Post> parents) {
        int i = 0;
        for (Post post : posts) {

            Post parent = parents.get(i);
            i++;

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
        }

        if (posts.size() != 0) {
            String incrementPostsCount = "UPDATE forums set posts = posts + ? WHERE slug = ?";
            jdbc.update(incrementPostsCount, posts.size(), posts.get(0).getForum());
        }

        return 201;
    }

    public Integer createPosts(List<Post> posts, Thread thread, List<Post> parents) {
        try (Connection connection = jdbc.getDataSource().getConnection()) {
            PreparedStatement pst = connection.prepareStatement("INSERT INTO posts (id, author, created, forum, message, parent, thread, path) " +
                    "VALUES (?,?, ?::TIMESTAMP WITH TIME ZONE, ?, ?, ?, ?, ?)");
            int i = 0;
            for (Post post : posts) {
                ArrayList arrObj;
                Post parent = parents.get(i);
                i++;
                Integer id = jdbc.queryForObject("SELECT nextval('posts_id_seq')", Integer.class);
                if (post.getParent() == 0) {
                    ArrayList arr = new ArrayList<>(Arrays.asList(id));
                    arrObj = arr;
                } else {
                    ArrayList arr = new ArrayList<>(Arrays.asList(parent.getChildren()));
                    arr.add(id);
                    arrObj = arr;
                }

                post.setId(id);
                post.setChildren(arrObj.toArray());
                ArrayList finalArrObj = arrObj;
                pst.setInt(1, post.getId());
                pst.setString(2, post.getAuthor());
                pst.setString(3, post.getCreated());
                pst.setString(4, post.getForum());
                pst.setString(5, post.getMessage());
                pst.setInt(6, post.getParent());
                if (post.getParent() == null) {
                    System.err.println("------------------------------------------------------------------------------");
                }
                pst.setLong(7, post.getThread());
//                pst.setBoolean(4, post.getIsedited());
                pst.setArray(8, connection.createArrayOf("int", finalArrObj.toArray()));

                pst.addBatch();

            }
            pst.executeBatch();
            connection.close();

            if (posts.size() != 0) {
                String incrementPostsCount = "UPDATE forums set posts = posts + ? WHERE slug = ?";
                jdbc.update(incrementPostsCount, posts.size(), posts.get(0).getForum());
            }

            return 201;

        } catch (SQLException e) {
            return 404;
        }
    }

    public Integer createPostsss(List<Post> posts, Thread thread) {
        if (posts.size() == 0) {
            return 201;
        }
//        List<Post> parents = getPostsById(posts);
//        if (parents.size() == 0) {
//            return 201;
//        }

        String query = "INSERT INTO posts (author, created, forum, message, parent, thread) " +
                "VALUES (?, ?::TIMESTAMP WITH TIME ZONE, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = jdbc.getDataSource().getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (Post post : posts){
                Post parent = getPostById(post.getParent());
                post.setCreated(posts.get(0).getCreated());
                post.setThread(thread.getId());
                post.setForum(thread.getForum());

                try  {
                    if ((parent == null && post.getParent() != 0) || (parent != null && !parent.getThread().equals(post.getThread()))) {
                        return 409;
                    }
                } catch (Exception e) {
                    return 409;
                }

                preparedStatement.setString(1, post.getAuthor());
                preparedStatement.setString(2, post.getCreated());
                preparedStatement.setString(3, post.getForum());
                preparedStatement.setString(4, post.getMessage());
                preparedStatement.setInt(5, post.getParent());
                preparedStatement.setInt(6, post.getThread());

                preparedStatement.addBatch();
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            int i = 0;
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("id"));
                posts.get(i).setId(resultSet.getInt("id"));
                i++;
            }

            if (posts.size() != 0) {
                String incrementPostsCount = "UPDATE forums set posts = posts + ? WHERE slug = ?";
                jdbc.update(incrementPostsCount, posts.size(), posts.get(0).getForum());
            }

            return 201;

        } catch (SQLException e) {
            return 500;
        } catch (DataAccessException e) {
            return 404;
        }
    }

    public void updateForum_Users(String slug, User user) {
        String sql = "INSERT INTO forum_users (slug, about, email, fullname, nickname) VALUES (?::citext, ?::citext, ?::citext, ?::citext, ?::citext) ON CONFLICT DO NOTHING";
        try {
            jdbc.update(sql, slug, user.getAbout(), user.getEmail(), user.getFullname(), user.getNickname());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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
            } else {
                sql += ", id ";
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

            return jdbc.query(sql, params.toArray(), postMapper);
        }
    }


    public List<Post> getPosts(Thread thread, Integer limit, Integer since, String sort, Boolean desc) {
        long threadId = thread.getId();
        List<Object> myObj = new ArrayList<>();
        String asc = desc ? "<" : ">";
        String myStr = "";
        if (sort == null)
            sort = "flat";
        switch (sort) {
            case "flat":
                myStr = "select * from posts where thread = ? ";
                myObj.add(threadId);
                if (since != null) {
                    myStr += " and id " + asc + "?";
                    myObj.add(since);
                }
                myStr += " order by created ";
                if (desc != null && desc) {
                    myStr += " desc, id desc";
                } else {
                    myStr += ", id";
                }
                if (limit != null) {
                    myStr += " limit ? ";
                    myObj.add(limit);
                }
                break;

            case "tree":
                myStr = "select * from posts where thread = ? ";
                myObj.add(threadId);
                if (since != null) {
                    myStr += " and path" + asc +" (select path from posts where id = ?) ";
                    myObj.add(since);
                }
                myStr += " order by path ";
                if (desc != null && desc) {
                    myStr += " desc";
                }
                if (limit != null) {
                    myStr += " limit ? ";
                    myObj.add(limit);
                }
                break;
            case "parent_tree":
               myStr += "SELECT * FROM posts WHERE thread=? ";
                myObj.add(thread.getId());

                if (since != null) {
                    if (desc) {
                        myStr += " AND path < (SELECT path FROM posts WHERE id=?) ";
                    } else {
                        myStr += " AND path > (SELECT path FROM posts WHERE id=?) ";
                    }
                    myObj.add(since);
                }

                myStr += "ORDER BY path ";

                if (desc != null && desc) {
                    myStr += "DESC, id DESC ";
                }

                if (limit != null) {
                    myStr += "LIMIT ?;";
                    myObj.add(limit);
                }

                break;
        }
        return jdbc.query(myStr, myObj.toArray(), postMapper);
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