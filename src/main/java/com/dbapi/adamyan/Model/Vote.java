package com.dbapi.adamyan.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Vote {
    private String nickname;
    private Integer threadID;
    private Integer vote;

    @JsonCreator
    public Vote(
        @JsonProperty(value = "nickname") String nickname,
        @JsonProperty(value = "thread") Integer threadID,
        @JsonProperty(value = "voice") Integer vote
    ) {
        this.nickname = nickname;
        this.threadID = threadID;
        this.vote = vote;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getVote() {
        return vote;
    }

    public void setVote(Integer vote) {
        this.vote = vote;
    }

    public Integer getThreadID() {
        return threadID;
    }

    public void setThreadID(Integer threadID) {
        this.threadID = threadID;
    }
}
