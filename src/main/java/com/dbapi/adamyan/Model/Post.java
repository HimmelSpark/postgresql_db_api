package com.dbapi.adamyan.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public class Post {
    private Integer id;
    private String author;
    private String created;
    private String forum;
    private Boolean isEdited;
    private String message;
    private Integer parent;
    private Integer thread;
    private Object [] children;

    @JsonCreator
    public Post(
            @JsonProperty(value = "id") Integer id,
            @JsonProperty(value = "author") String author,
            @JsonProperty(value = "created") String created,
            @JsonProperty(value = "forum") String forum,
            @JsonProperty(value = "isEdited") Boolean isEdited,
            @JsonProperty(value = "message") String message,
            @JsonProperty(value = "parent") Integer parent,
            @JsonProperty(value = "thread") Integer thread,
            @JsonProperty(value = "children") Object [] children
    ) {
        this.id = id;
        this.author = author;
        this.forum = forum;
        if (isEdited == null) {
            this.isEdited = false;
        } else {
            this.isEdited = isEdited;
        }
        this.message = message;
        if (parent == null) {
            this.parent = 0;
        } else {
            this.parent = parent;
        }
        this.thread = thread;
        if (created == null) {
            this.created = new Timestamp(System.currentTimeMillis()).toInstant().toString();
        } else {
            this.created = created;
        }
        this.children = children;

        if (this.getParent() == null) {
            System.err.println("SUUUUUUUUUUUUQAAAAAAAAAAAAA!");
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public Boolean getIsedited() {
        return isEdited;
    }

    public void setIsedited(Boolean isEdited) {
        this.isEdited = isEdited;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }

    public Object[] getChildren() {
        return children;
    }

    public void setChildren(Object[] children) {
        this.children = children;
    }
}
