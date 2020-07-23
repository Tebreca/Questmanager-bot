package com.tebreca.leaderboardapi.discord;

public enum Problem {
    MISSING_QUESTION, MENTION, TOO_LONG;

    @Override
    public String toString() {
        switch (this){
            case MENTION:
                return "You used '@' in your problem, do not do that!";
            case MISSING_QUESTION:
                return "You haven't answered all the required questions! Try again. (use !questions in dm for a format)";
            case TOO_LONG:
                return "Your problem was bigger than the 1500 character limit! Try to compact it a little.";
            default:
                return "";
        }
    }
}
