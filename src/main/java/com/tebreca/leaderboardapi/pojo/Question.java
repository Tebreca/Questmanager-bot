package com.tebreca.leaderboardapi.pojo;

public class Question {

    private final Long messageId;
    /** key = question, value = description/answer */
    private String questionContents;

    private Long userID;

    public void setQuestionContents(String questionContents) {
        this.questionContents = questionContents;
    }

    public Long getUserID() {
        return userID;
    }
    
    public Long getMessageId(){
        return messageId;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getQuestionContents() {
        return questionContents;
    }

    public Question(String questionContents, Long userID, Long messageId) {
        this.questionContents = questionContents;
        this.userID = userID;
        this.messageId = messageId;
    }

    public Question(){
        this("", -1L, -1L);
    }

}
