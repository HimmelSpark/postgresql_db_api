package com.dbapi.adamyan.Controller;

import com.dbapi.adamyan.DAO.ForumDAO;
import com.dbapi.adamyan.DAO.PostDAO;
import com.dbapi.adamyan.DAO.ThreadDAO;
import com.dbapi.adamyan.DAO.UserDAO;
import com.dbapi.adamyan.Model.*;
import com.dbapi.adamyan.Model.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/post")
public class PostController {
    private PostDAO postDAO;
    private UserDAO userDAO;
    private ThreadDAO threadDAO;
    private ForumDAO forumDAO;

    @Autowired
    PostController(
        PostDAO postDAO,
        UserDAO userDAO,
        ThreadDAO threadDAO,
        ForumDAO forumDAO
    ) {
        this.postDAO = postDAO;
        this.userDAO = userDAO;
        this.threadDAO = threadDAO;
        this.forumDAO = forumDAO;
    }

//    @PostMapping(path = "/{id}/details")
//    public ResponseEntity updatePost(
//            @RequestBody Post post,
//            @PathVariable(name = "id") Integer id
//    ) {
//        Post post1;
//        post1 = postDAO.getPostById(id);
//        if (post1 == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Post not found"));
//        }
//        postDAO.updatePost(id, post);
//        post1 = postDAO.getPostById(id);
//
//        Map<String, Object> kostyl = new HashMap<>();
//        kostyl.put("author", post1.getAuthor());
//        kostyl.put("created", post1.getCreated());
//        kostyl.put("forum", post1.getForum());
//        kostyl.put("id", post1.getId());
//        if (post1.getIsedited() != null && post1.getIsedited()) {
//            kostyl.put("isEdited", post1.getIsedited());
//        }
//        kostyl.put("message", post1.getMessage());
//        kostyl.put("thread", post1.getThread());
//
//        return ResponseEntity.status(HttpStatus.OK).body(kostyl);
//    }

    @PostMapping(path = "/{id}/details")
    public ResponseEntity updatePost2(
            @RequestBody Post post,
            @PathVariable(name = "id") Integer id
    ) {
        Post post1 = postDAO.getPostById(id);
        if (post1 == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Post not found"));
        }
        Boolean flag = false;
        if (post.getAuthor() != null && !post.getAuthor().equals(post1.getAuthor())) {
            flag = true;
        } else if (post.getForum() != null && !post.getForum().equals(post1.getForum())) {
            flag = true;
        } else if (post.getMessage() != null && !post.getMessage().equals(post1.getMessage())) {
            flag = true;
        } else if (post.getParent() != null && !post.getParent().equals(post1.getParent())) {
            flag = true;
        } else if (post.getThread() != null && !post.getThread().equals(post1.getThread())) {
            flag = true;
        }

        if (!flag) {
            return ResponseEntity.status(HttpStatus.OK).body(post1);
        }

        postDAO.updatePost(id, post);
        post1 = postDAO.getPostById(id);

        Map<String, Object> kostyl = new HashMap<>();
        kostyl.put("author", post1.getAuthor());
        kostyl.put("created", post1.getCreated());
        kostyl.put("forum", post1.getForum());
        kostyl.put("id", post1.getId());
        if (post1.getIsedited() != null && post1.getIsedited()) {
            kostyl.put("isEdited", post1.getIsedited());
        }
        kostyl.put("message", post1.getMessage());
        kostyl.put("thread", post1.getThread());

        return ResponseEntity.status(HttpStatus.OK).body(kostyl);
    }

    @GetMapping(path = "{id}/details")
    public ResponseEntity getPost(
            @PathVariable(name = "id") Integer id,
            @RequestParam(name = "related", required = false) String[] related
    ) {
        Post post = postDAO.getPostById(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("404"));
        }
        post.setChildren(null);
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> kostyl = new HashMap<>();
        kostyl.put("author", post.getAuthor());
        kostyl.put("created", post.getCreated());
        kostyl.put("forum", post.getForum());
        kostyl.put("id", post.getId());
        if (post.getIsedited() != null && post.getIsedited()) {
            kostyl.put("isEdited", post.getIsedited());
        }
        kostyl.put("message", post.getMessage());
        kostyl.put("thread", post.getThread());
        kostyl.put("parent", post.getParent());

        if (related == null) {
            result.put("post", kostyl);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        }

        for (String temp : related) {
            switch (temp) {
                case "user":
                    User user = userDAO.getUserByNickname(post.getAuthor());
                    result.put("author", user);
                    break;
                case "thread":
                    Thread thread = threadDAO.getThreadById(post.getThread());
                    result.put("thread", thread);
                    break;
                case "forum":
                    Forum forum = forumDAO.getForumBySlug(post.getForum());
                    result.put("forum", forum);
                    break;
            }
        }

        result.put("post", kostyl);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
