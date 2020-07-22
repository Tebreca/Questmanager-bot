package com.tebreca.leaderboardapi.pojo;

public class ServerConfig {

    public static final ServerConfig EMPTY = new ServerConfig();

    private final String[] problemChannelNames;

    /** including '?' */
    private final String[] questions;

    private final String[] discussionChannelNames;

    private final String API_KEY;

    public ServerConfig(String[] problemChannelNames, String[] questions, String[] discussionChannelNames, String api_key){

        this.problemChannelNames = problemChannelNames;
        this.questions = questions;
        this.discussionChannelNames = discussionChannelNames;
        API_KEY = api_key;
    }

    protected ServerConfig(){
        this(new String[0], new String[0], new String[0], "");
    }

    public static ServerConfig getEMPTY() {
        return EMPTY;
    }

    public String[] getProblemChannelNames() {
        return problemChannelNames;
    }

    public String[] getQuestions() {
        return questions;
    }

    public String[] getDiscussionChannelNames() {
        return discussionChannelNames;
    }

    public String getAPI_KEY() {
        return API_KEY;
    }
}
