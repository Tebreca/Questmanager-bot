package com.tebreca.leaderboardapi.pojo;

public class Question {

    /** key = question, value = description/answer */
    private String questionContents;

    private Long userID;

    public void setQuestionContents(String questionContents) {
        this.questionContents = questionContents;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getQuestionContents() {
        return questionContents;
    }

    public Question(String questionContents, Long userID) {
        this.questionContents = questionContents;
        this.userID = userID;
    }

    public Question(){
        this("", -1L);
    }

}
