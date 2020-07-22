package com.tebreca.leaderboardapi.pojo;

import java.util.List;
import java.util.Map;

public class Question {

    /** key = question, value = description/answer */
    List<Map.Entry<String, String>> description;

    public List<Map.Entry<String, String>> getDescription() {
        return description;
    }

    public Question(List<Map.Entry<String, String>> description) {
        this.description = description;
    }
}
