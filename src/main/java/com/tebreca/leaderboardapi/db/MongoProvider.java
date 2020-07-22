package com.tebreca.leaderboardapi.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.tebreca.leaderboardapi.db.repo.DiscordMemberRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoProvider {

    /*
     * Use the standard Mongo driver API to create a com.mongodb.client.MongoClient instance.

    public @Bean MongoClient mongoClient() {
        return MongoClients.create("mongodb+srv://server:@cluster0.cerur.azure.mongodb.net/?retryWrites=true&w=majority");
    }
    */

    public @Bean MongoManager mongoManager(DiscordMemberRepository memberRepository){
        return new MongoManager(memberRepository);
    }

}
