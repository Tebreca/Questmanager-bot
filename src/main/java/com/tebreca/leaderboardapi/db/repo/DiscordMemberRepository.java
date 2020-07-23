package com.tebreca.leaderboardapi.db.repo;

import com.tebreca.leaderboardapi.pojo.DiscordMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface DiscordMemberRepository extends MongoRepository<DiscordMember, Long> {


    Optional<DiscordMember> findByDiscordID(Long id);

    Optional<DiscordMember> findByNameAndTag(String name, String tag);
}
