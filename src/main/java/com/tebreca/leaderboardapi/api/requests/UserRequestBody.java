package com.tebreca.leaderboardapi.api.requests;

public class UserRequestBody {

    public String username;

    public String tag;

    public UserRequestBody() {
        this("", "");
    }

    public UserRequestBody(String username, String tag) {
        this.username = username;
        this.tag = tag;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
