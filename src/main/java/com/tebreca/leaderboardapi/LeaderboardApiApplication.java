package com.tebreca.leaderboardapi;

import com.tebreca.leaderboardapi.db.MongoManager;
import com.tebreca.leaderboardapi.discord.DiscordBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class LeaderboardApiApplication {


    public static LeaderboardApiApplication APPLICATION = null;

    @Autowired
    private final MongoManager dataManager = null;

    @Autowired
    MongoOperations operations;

    public MongoOperations getOperations() {
        return operations;
    }

    public LeaderboardApiApplication() {
        LeaderboardApiApplication.APPLICATION = this;
    }

    public MongoManager getDataManager() {
        return dataManager;
    }

    public DiscordBot getBot() {
        return bot;
    }

    private DiscordBot bot;

    public static void main(String[] args) {
        SpringApplication.run(LeaderboardApiApplication.class, args);

    }

    @PostConstruct
    public void init(){
        this.bot = DiscordBot.init();
    }

}
