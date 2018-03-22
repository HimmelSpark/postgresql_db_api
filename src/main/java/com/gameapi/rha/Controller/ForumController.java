package com.gameapi.rha.Controller;

import com.gameapi.rha.DAO.ForumDAO;
import com.gameapi.rha.DAO.ThreadDAO;
import com.gameapi.rha.DAO.UserDAO;
import com.gameapi.rha.Model.Forum;
import com.gameapi.rha.Model.Message;
import com.gameapi.rha.Model.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/forum")
public class ForumController {
    private ForumDAO forumDAO;
    private UserDAO userDAO;
    private ThreadDAO threadDAO;

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

        if (userDAO.getUserByNickname(forum.getUser()) != null) {
            try {
                forumDAO.createForum(forum);
                return ResponseEntity.status(HttpStatus.CREATED).body(forum);
            } catch (DuplicateKeyException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Not unique slug"));
            }
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Can't find user with nickname " + forum.getUser()));
    }

    @PostMapping(path = "{slug}/create")
    public ResponseEntity createThread(@PathVariable(name = "slug") String slug, @RequestBody Thread thread) {

        if (userDAO.getUserByNickname(thread.getAuthor()) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find user with nickname " + thread.getAuthor()));
        }

        if (forumDAO.getForumBySlug(slug) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find forum with slug " + slug));
        }

        try {
            threadDAO.createThread(thread, slug);
            return ResponseEntity.status(HttpStatus.CREATED).body(thread);
        } catch (Exception e ) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Not unique slug of thread"));
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
}