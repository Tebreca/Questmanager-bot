package com.tebreca.leaderboardapi.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tebreca.leaderboardapi.LeaderboardApiApplication;
import com.tebreca.leaderboardapi.db.MongoManager;
import com.tebreca.leaderboardapi.pojo.DiscordMember;
import com.tebreca.leaderboardapi.pojo.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordBot extends ListenerAdapter {

    public static final Gson GSON = new GsonAutoConfiguration().gson(new GsonBuilder());

    private final JDA api;
    private final ServerConfig config;

    private DiscordBot(ServerConfig config) throws LoginException, InterruptedException {
        this.config = config;
        JDABuilder jdaBuilder = JDABuilder.create(config.getAPI_KEY(),//
                GatewayIntent.GUILD_MEMBERS,//
                GatewayIntent.DIRECT_MESSAGES,//
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,//
                GatewayIntent.GUILD_MESSAGE_REACTIONS,//
                GatewayIntent.GUILD_MESSAGES//
        );
        jdaBuilder.addEventListeners(this);
        jdaBuilder.disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOTE);
        api = jdaBuilder.build();
        api.awaitReady();
    }

    @Override
    public void onGenericGuildMessage(@Nonnull GenericGuildMessageEvent event) {
        System.out.printf("Recieved message: %s \n", event.toString());
        super.onGenericGuildMessage(event);
    }

    public static DiscordBot init() {
        DiscordBot bot = null;
        File config = new File("botConfig.json");
        if(!config.exists()){
            try {
                config.createNewFile();
                FileWriter writer = new FileWriter(config);
                GSON.toJson(ServerConfig.EMPTY, writer);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        try {
            FileReader reader = new FileReader(config);
            ServerConfig serverConfig = GSON.fromJson(reader, ServerConfig.class);
            reader.close();
            bot = new DiscordBot(serverConfig);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return bot;
    }

    @Override
    public void onGenericEvent(@NotNull GenericEvent event)
    {
        if (event instanceof ReadyEvent) {
            updateDB();
        }
    }


    private void updateDB() {
        LeaderboardApiApplication application = LeaderboardApiApplication.APPLICATION;
        assert application != null;
        MongoOperations operations = application.getOperations();

        if(!operations.collectionExists(DiscordMember.class)){
            operations.createCollection(DiscordMember.class);
        }

        List<Long> ids = operations.findAll(DiscordMember.class).stream().map(DiscordMember::getId).collect(Collectors.toList());
        List<Long> newUsers = new ArrayList<>();
        for(Guild guild : api.getGuilds()){
            guild.getMembers().stream().map(Member::getIdLong).filter(l ->!ids.contains(l)).forEach(newUsers::add);
        }
        System.out.printf("found these new members; %s \n", Arrays.toString(newUsers.toArray()));
        newUsers.stream().map(DiscordMember::new).forEach(operations::save);
        //System.out.printf("%s entries in db!", operations.findAll(DiscordMember.class).size());
    }
}
