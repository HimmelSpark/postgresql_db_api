package com.dbapi.adamyan.Controller;

import com.dbapi.adamyan.DAO.ForumDAO;
import com.dbapi.adamyan.DAO.ThreadDAO;
import com.dbapi.adamyan.DAO.UserDAO;
import com.dbapi.adamyan.Model.Forum;
import com.dbapi.adamyan.Model.Message;
import com.dbapi.adamyan.Model.Thread;
import com.dbapi.adamyan.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.sql.Timestamp;
import java.util.List;
import java.util.function.BinaryOperator;


@RestController
@RequestMapping("api/forum")
public class ForumController {
    private ForumDAO forumDAO;
    private UserDAO userDAO;
    private ThreadDAO threadDAO;
    public static Integer count = 0;
    @Autowired
    ForumController(
            ForumDAO forumDAO,
            UserDAO userDAO,
            ThreadDAO threadDAO
    ) {
        this.forumDAO = forumDAO;
        this.userDAO = userDAO;
        this.threadDAO = threadDAO;
    }

    @PostMapping(path = "/create")
    public ResponseEntity createForum(@RequestBody Forum forum) {

        User user = userDAO.getUserByNickname(forum.getUser());

        if (user != null) {
            try {
                forum.setNickname(user.getNickname());
                forumDAO.createForum(forum);
                return ResponseEntity.status(HttpStatus.CREATED).body(forum);
            } catch (DuplicateKeyException e) {
                Forum result = forumDAO.getForumBySlug(forum.getSlug());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find user with nickname " + forum.getUser()));
    }

    @PostMapping(path = "{slug}/create")
    public ResponseEntity createThread(@PathVariable(name = "slug") String slug, @RequestBody Thread thread) {
        count++;
        try {
            Forum forum = forumDAO.getForumBySlug(slug);
            if (forum == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find forum " + slug));
            }
            thread.setForum(forum.getSlug());
            Thread result = threadDAO.createThread(thread);
            User user = userDAO.getUserByNickname(result.getAuthor());
            threadDAO.updateForum_Users(slug, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (DuplicateKeyException e ) {
            Thread duplicate = threadDAO.getThreadBySlug(thread.getSlug());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicate);
        } catch (DataAccessException e ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find user " + thread.getAuthor()));
        }
    }

    @GetMapping(path = "{slug}/details")
    public ResponseEntity getForum(@PathVariable(name = "slug") String slug) {
        Forum forum = forumDAO.getForumBySlug(slug);
        if (forum == null ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find forum with slug " + slug));
        }

        return ResponseEntity.status(HttpStatus.OK).body(forum);
    }

    @GetMapping(path = "{slug}/threads")
    public ResponseEntity getThreads(
            @PathVariable(name = "slug") String slug,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "since", required = false) String since,
            @RequestParam(name = "desc", required = false) Boolean desc
            ) {
        if (forumDAO.getForumBySlug(slug) == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find forum with slug " + slug));

        List<Thread> result = threadDAO.getAllThreadsByForum(slug, limit, since, desc);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping(path = "{slug}/users")
    public ResponseEntity getUsers (
            @PathVariable(name = "slug") String slug,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "since", required = false) String since,
            @RequestParam(name = "desc", required = false) Boolean desc
    ) {
        if (forumDAO.getForumBySlug(slug) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find forum with slug " + slug));
        }
        List<User> result = userDAO.getNotAllUsersByForum(since, slug, limit, desc);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}