package com.tebreca.leaderboardapi.pojo;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class DiscordMember {

    @Id
    ObjectId id = new ObjectId();

    public void setId(ObjectId id) {
        this.id = id;
    }

    @Field
    private Long discordID;

    @Field
    private Question[] answered;

    @Field
    private Question[] asked;

    @Field
    private String name;

    @Field
    private String tag;

    public void setDiscordID(Long discordID) {
        this.discordID = discordID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public DiscordMember(Long discordID, Question[] answered, Question[] asked, String name, String tag) {
        this.discordID = discordID;
        this.answered = answered;
        this.asked = asked;
        this.name = name;
        this.tag = tag;
    }

    public DiscordMember(Long id, Question[] answered, Question[] asked) {
        this(id, answered, asked, "", "");
    }

    public DiscordMember(Long id) {
        this(id, new Question[0], new Question[0]);
    }

    public DiscordMember() {
        this(-1l);
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

    public static DiscordMember fromUser(User user) {
        String[] split = user.getAsTag().split("#");
        return new DiscordMember(user.getIdLong(), new Question[0], new Question[0], split[0], split[1]);
    }

    public Long getDiscordID() {
        return discordID;
    }

    public void setAnswered(Question[] answered) {
        this.answered = answered;
    }

    public void setAsked(Question[] asked) {
        this.asked = asked;
    }
}
