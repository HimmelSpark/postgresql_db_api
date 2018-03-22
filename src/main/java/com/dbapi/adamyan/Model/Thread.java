package com.dbapi.adamyan.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public class Thread {
    private String author;
    private Timestamp created;
    private String forum;
    private String message;
    private String slug;
    private String title;
    private Integer votes;

    @JsonCreator
    Thread(
            @JsonProperty(value = "author") String author,
            @JsonProperty(value = "created") Timestamp created,
            @JsonProperty(value = "forum") String forum,
            @JsonProperty(value = "message") String message,
            @JsonProperty(value = "slug") String slug,
            @JsonProperty(value = "title") String title,
            @JsonProperty(value = "votes") Integer votes
    ) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.message = message;
        this.slug = slug;
        this.title = title;
        this.votes = votes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }
}
