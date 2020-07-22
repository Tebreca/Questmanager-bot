package com.tebreca.leaderboardapi.db;

import com.tebreca.leaderboardapi.db.repo.DiscordMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;


public class MongoManager {

    @Autowired
    public final MongoTemplate template = null;

    public final DiscordMemberRepository memberRepository;

    MongoManager(DiscordMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

}
