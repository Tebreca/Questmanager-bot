package com.tebreca.leaderboardapi.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tebreca.leaderboardapi.LeaderboardApiApplication;
import com.tebreca.leaderboardapi.db.repo.DiscordMemberRepository;
import com.tebreca.leaderboardapi.pojo.DiscordMember;
import com.tebreca.leaderboardapi.pojo.Question;
import com.tebreca.leaderboardapi.pojo.ServerConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
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
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        String channelName = channel.getName();
        String message = event.getMessage().getContentRaw();
        List<String> problemsChannelNames = Arrays.asList(config.getProblemChannelNames());
        List<String> questions = Arrays.asList(config.getQuestions());

        if (problemsChannelNames.contains(channelName)) {

            //check if message is correct
            if (!questions.stream().map(message::contains).reduce(Boolean::logicalAnd).orElse(true)) {
                badProblem(event.getMessage(), Problem.MISSING_QUESTION);
            } else if (message.contains("@")) {
                badProblem(event.getMessage(), Problem.MENTION);
            } else if (message.length() > 1500) {
                badProblem(event.getMessage(), Problem.TOO_LONG);
            } else {
                DiscordMember discordMember = LeaderboardApiApplication.APPLICATION.getDataManager().memberRepository.findByDiscordID(event.getAuthor().getIdLong()).orElse(null);
                if (discordMember != null) {
                    List<Question> list = new ArrayList<>(Arrays.asList(discordMember.getAsked()));
                    list.add(new Question(message, event.getAuthor().getIdLong()));
                    discordMember.setAsked(list.toArray(new Question[0]));
                    LeaderboardApiApplication.APPLICATION.getDataManager().memberRepository.save(discordMember);
                }
            }
        }
    }

    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        Message message = event.getMessage();
        String msg = message.getContentRaw();
        if (msg.startsWith("!question")) {
            StringBuilder back = new StringBuilder();
            for (String question : config.getQuestions()) {
                back.append("**").append(question).append("**");
                back.append("\n(your answer here)\n\n");
            }

            back.append("Use shift+enter to add a line break :wink:");
            message.getChannel().sendMessage(back).queue();
        }
        super.onPrivateMessageReceived(event);
    }

    @Override
    public void onPrivateMessageReactionAdd(@Nonnull PrivateMessageReactionAddEvent event) {
        if (event.getReactionEmote().getEmoji().equals("✅") && !event.getUser().isBot()) {
            Message msg = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
            String msgLink = Arrays.stream(msg.getContentDisplay().split("\n")).filter(s -> s.contains("https://discordapp.com/channels/")).findFirst().orElse("https://discordapp.com/channels/").substring(32);
            String[] strings = msgLink.split("/");
            String guildId = strings[0];
            String channelId = strings[1];
            String msgId = strings[2];
            TextChannel channel = ((TextChannel) api.getGuildById(guildId).getGuildChannelById(channelId));
            String contentRaw = channel.retrieveMessageById(msgId).complete().getContentRaw();
            channel.deleteMessageById(msgId).queue();
            DiscordMemberRepository discordMemberRepository = LeaderboardApiApplication.APPLICATION.getDataManager().memberRepository;
            DiscordMember member = discordMemberRepository.findByDiscordID(event.getUserIdLong()).orElse(null);
            if (member == null) {
                return;
            }
            Question question = Arrays.stream(member.getAsked()).filter(q -> q.getQuestionContents().equals(contentRaw)).findAny().orElse(new Question(contentRaw, event.getUserIdLong()));
            String[] nameAndTag = msg.getContentRaw().split("#");
            DiscordMember discordMember = discordMemberRepository.findByNameAndTag(nameAndTag[0], nameAndTag[1].substring(0, 4)).orElse(null);
            if (discordMember == null || discordMember.getDiscordID().equals(member.getDiscordID())) {
                event.getChannel().deleteMessageById(event.getMessageId()).queue();
                return;
            }
            List<Question> questions = new ArrayList<>();
            questions.addAll(Arrays.asList(discordMember.getAnswered()));
            questions.add(question);
            discordMember.setAnswered(questions.toArray(new Question[0]));
            event.getChannel().deleteMessageById(event.getMessageId()).queue();
        }
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        TextChannel channel = event.getChannel();
        String channelName = channel.getName();
        List<String> problemsChannelNames = Arrays.asList(config.getProblemChannelNames());
        if (problemsChannelNames.contains(channelName) && event.getReactionEmote().getEmoji().equals("✅")) {
            Message message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
            StringBuilder msg = new StringBuilder();
            msg.append(String.format("%s marked your problem as solved; \n \n", event.getUser().getAsTag()));
            msg.append("https://discordapp.com/channels/");
            msg.append(event.getGuild().getId()).append('/');
            msg.append(channel.getId()).append('/');
            msg.append(message.getId());
            msg.append("\n \nIs this correct? If not, just ignore this message and nothing will happen!");
            PrivateChannel privateChannel = message.getAuthor().openPrivateChannel().complete();
            privateChannel.sendMessage(msg)//
                    .queue(message1 -> privateChannel.addReactionById(message1.getId(), "✅").queue());
        }
    }

    @Override
    public void onMessageUpdate(@Nonnull MessageUpdateEvent event) {
        super.onMessageUpdate(event);
    }

    private void badProblem(Message message, Problem missingQuestion) {
        message.getChannel().deleteMessageById(message.getId()).queue();
        User author = message.getAuthor();
        StringBuilder msg = new StringBuilder();
        msg.append("You submitted the following problem; \n\n");
        msg.append(message.getContentDisplay());
        msg.append("\n\nIt was removed; ```");
        msg.append(missingQuestion);
        msg.append("```");
        author.openPrivateChannel().complete().sendMessage(msg).queue();
    }

    public static DiscordBot init() {
        DiscordBot bot = null;
        File config = new File("botConfig.json");
        if (!config.exists()) {
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

    public void updateDB() {
        LeaderboardApiApplication application = LeaderboardApiApplication.APPLICATION;
        assert application != null;
        MongoOperations operations = application.getOperations();

        if (!operations.collectionExists(DiscordMember.class)) {
            operations.createCollection(DiscordMember.class);
        }

        List<Long> ids = operations.findAll(DiscordMember.class).stream().map(DiscordMember::getId).collect(Collectors.toList());
        List<Member> newUsers = api.getGuilds().stream()
                .map(Guild::getMembers)
                .flatMap(List::stream)
                .filter(l -> !ids.contains(l.getIdLong()))
                .collect(Collectors.toList());
        newUsers.stream().map(Member::getUser).map(DiscordMember::fromUser).forEach(operations::insert);
    }

    public void purgeOldMessages() {
        api.getGuilds().forEach(this::purgeOldMessages);
    }

    private void purgeOldMessages(Guild guild) {
        List<String> problemsChannelNames = Arrays.asList(config.getProblemChannelNames());
        guild.getChannels().stream().filter(channel -> problemsChannelNames.contains(channel.getName())).map(c -> ((TextChannel) c)).forEach(this::purgeOldMessages);
    }

    private void purgeOldMessages(TextChannel channel) {
        OffsetDateTime _10daysbefore = OffsetDateTime.now().minus(Period.ofDays(10));
        channel.getHistory().getRetrievedHistory().stream().filter((Message m) -> m.getTimeCreated().isBefore(_10daysbefore)).filter(m->!m.isPinned()).map(Message::getId).forEach(l-> channel.deleteMessageById(l).queue());
    }

}
