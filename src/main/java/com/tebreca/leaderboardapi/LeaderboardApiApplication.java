package com.tebreca.leaderboardapi;

import com.tebreca.leaderboardapi.db.MongoManager;
import com.tebreca.leaderboardapi.discord.DiscordBot;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;

@EnableAdminServer
@SpringBootApplication
public class LeaderboardApiApplication {


    public static LeaderboardApiApplication APPLICATION = null;

    Timer scheduler = new Timer();

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
        bot.updateDB();
        scheduler.scheduleAtFixedRate(new Runner(), 100L, 3600000L);
    }

    private class Runner extends TimerTask {

        @Override
        public void run() {
            bot.purgeOldMessages();
        }

    }

}
