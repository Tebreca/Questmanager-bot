package com.tebreca.leaderboardapi.api.responses;

public class GetScoreResponseBody {

    int score;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public GetScoreResponseBody() {
        this(0);
    }

    public GetScoreResponseBody(int score) {
        this.score = score;
    }
}
