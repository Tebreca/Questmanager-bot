package com.tebreca.leaderboardapi.pojo;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Document
public class DiscordMember {

    @Id
    ObjectId id = new ObjectId();

    @Field
    private final Long discordID;

    @Field
    private Question[] answered;

    @Field
    private Question[] asked;

    public DiscordMember(Long id, Question[] answered, Question[] asked) {
        this.discordID = id;
        this.answered = answered;
        this.asked = asked;
    }

    public DiscordMember(Long id) {
        this(id, new Question[0], new Question[0]);
    }

    public Long getId() {
        return discordID;
    }

    public Question[] getAnswered() {
        return answered;
    }

    public Question[] getAsked() {
        return asked;
    }
}
