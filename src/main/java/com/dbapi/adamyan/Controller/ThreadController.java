package com.dbapi.adamyan.Controller;

import com.dbapi.adamyan.DAO.*;
import com.dbapi.adamyan.Model.*;
import com.dbapi.adamyan.Model.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/thread")
public class ThreadController {
    private ThreadDAO threadDAO;
    private PostDAO postDAO;
    private UserDAO userDAO;
    private VoteDAO voteDAO;

    @Autowired
    ThreadController(
        ThreadDAO threadDAO,
        PostDAO postDAO,
        ForumDAO forumDAO,
        UserDAO userDAO,
        VoteDAO voteDAO
    ) {
        this.threadDAO = threadDAO;
        this.postDAO = postDAO;
        this.userDAO = userDAO;
        this.voteDAO = voteDAO;
    }

    @PostMapping(path = "/{slug_or_id}/create")
    public ResponseEntity createPost(
            @RequestBody ArrayList<Post> posts,
            @PathVariable(name = "slug_or_id") String slug_or_id
    ) {
        Integer id;
        Thread thread = null;
        try {
            id = Integer.parseInt(slug_or_id);
            thread = threadDAO.getThreadById(id);
        } catch (Exception e) {
            thread = threadDAO.getThreadBySlug(slug_or_id);
        }
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find thread with slug " + slug_or_id));
        }

        for (Post post : posts) {
            post.setCreated(posts.get(0).getCreated());
            post.setThread(thread.getId());
            post.setForum(thread.getForum());
            Post parent = postDAO.getPostById(post.getParent());
            // Если нет родителя и он сам не корневой, то кака
            // Либо есть родитель, но не совпали треды
            if ((parent == null && post.getParent() != 0) || (parent != null && !parent.getThread().equals(post.getThread()))) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Wrong parents"));
            }
        }

        Integer result = postDAO.createPosts(posts, thread);

        if (result == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("404"));
        } else {
            for (Post post : posts) {
                User user = userDAO.getUserByNickname(post.getAuthor());
                postDAO.updateForum_Users(thread.getForum(), user);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(posts);
        }
    }

    @PostMapping(path = "/{slug_or_id}/vote")
    public ResponseEntity doVote(
        @RequestBody Vote vote,
        @PathVariable(name = "slug_or_id") String slug_or_id
    ) {
        if (vote == null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(vote);
        }

        User user = userDAO.getUserByNickname(vote.getNickname());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find user " + vote.getNickname()));
        }

        Integer id = null;
        Thread thread = null;
        try {
            id = Integer.parseInt(slug_or_id);
            thread = threadDAO.getThreadById(id);
        } catch (NumberFormatException e) {
            thread = threadDAO.getThreadBySlug(slug_or_id);
        }

        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find thread " + slug_or_id));
        }

        Vote voted = voteDAO.getVote(user.getNickname(), thread.getId());
        Integer mark = 0;
        if (voted == null) {
            mark = vote.getVote();
            threadDAO.doVote(mark, user.getNickname(), thread.getId());
        } else {
            if (voted.getVote().equals(vote.getVote())) {
                return ResponseEntity.status(HttpStatus.OK).body(thread);
            } else if (voted.getVote() > vote.getVote()) {
                mark = -2;
            } else {
                mark = 2;
            }
            threadDAO.updateThreadVote(mark, thread.getId());
            voteDAO.updateVote(user.getNickname(), thread.getId(), vote.getVote());
        }
        thread.setVotes(thread.getVotes() + mark);
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @PostMapping(path = "/{slug_or_id}/details")
    public ResponseEntity updateThread(
            @RequestBody Thread thread,
            @PathVariable(name = "slug_or_id") String slug_or_id
    ) {
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Thread not found"));
        }
        Integer thread_id = null;
        String slug = null;
        try {
            thread_id = Integer.parseInt(slug_or_id);
        } catch (NumberFormatException e) {
            slug = slug_or_id;
        }

        try {
            return ResponseEntity.status(HttpStatus.OK).body(threadDAO.updateThread(slug, thread_id, thread));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("THread not found"));
        }
    }

    @GetMapping(path = "{slug_or_id}/details")
    public ResponseEntity getOneThread(
            @PathVariable(name = "slug_or_id") String slug_or_id
    ) {
        String slug = null;
        Integer thread_id = null;
        try {
            thread_id = Integer.parseInt(slug_or_id);
        } catch (NumberFormatException e) {
            slug = slug_or_id;
        }
        Thread result = threadDAO.getOneThread(slug, thread_id);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find thread"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping(path = "{slug_or_id}/posts")
    public ResponseEntity getPostsOfThread(
            @PathVariable(name = "slug_or_id") String slug_or_id,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "since", required = false) Integer since,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "desc", required = false) Boolean desc
    ) {
        Thread thread = null;
        if (sort == null) {
            sort = "flat";
        }
        if (desc == null) {
            desc = false;
        }
        try {
            thread = threadDAO.getThreadById(Integer.parseInt(slug_or_id));
        } catch (NumberFormatException e) {
            thread = threadDAO.getThreadBySlug(slug_or_id);
        }
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find thread"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(postDAO.getPostsOfThread(thread, limit, since, sort, desc));
    }
}
