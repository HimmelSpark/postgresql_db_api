package com.gameapi.rha.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Forum {
    private Integer posts;
    private String slug;
    private Integer threads;
    private String title;
    private String user;

    @JsonCreator
    public Forum(
            @JsonProperty(value = "posts") int posts,
            @JsonProperty(value = "slug") String slug,
            @JsonProperty(value = "threads") int threads,
            @JsonProperty(value = "title") String title,
            @JsonProperty(value = "user") String user
    ) {
        this.posts = posts;
        this.slug = slug;
        this.threads = threads;
        this.title = title;
        this.user = user;
    }

    public Integer getPosts() {
        return posts;
    }

    public void setPosts(Integer posts) {
        this.posts = posts;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setNickname(String nickname) {
        this.user = nickname;
    }
}
