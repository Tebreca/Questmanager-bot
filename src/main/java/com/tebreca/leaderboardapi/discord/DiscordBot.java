package com.tebreca.leaderboardapi.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tebreca.leaderboardapi.LeaderboardApiApplication;
import com.tebreca.leaderboardapi.db.MongoManager;
import com.tebreca.leaderboardapi.db.repo.DiscordMemberRepository;
import com.tebreca.leaderboardapi.pojo.DiscordMember;
import com.tebreca.leaderboardapi.pojo.Question;
import com.tebreca.leaderboardapi.pojo.ServerConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tebreca.leaderboardapi.LeaderboardApiApplication.APPLICATION;


public class DiscordBot extends ListenerAdapter {


    public static final Gson GSON = new GsonAutoConfiguration().gson(new GsonBuilder());

    private final JDA api;
    private final ServerConfig config;

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        User user = event.getMember().getUser();
        if (!user.isBot()) {
            APPLICATION.getDataManager().memberRepository.save(DiscordMember.fromUser(user));
        }
        super.onGuildMemberJoin(event);
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        User user = event.getMember().getUser();
        if (!user.isBot()) {
            DiscordMemberRepository repository = APPLICATION.getDataManager().memberRepository;
            repository.delete(repository.findByDiscordID(user.getIdLong()).orElse(DiscordMember.fromUser(user)));
        }
        super.onGuildMemberRemove(event);
    }

    private DiscordBot(ServerConfig config) throws LoginException, InterruptedException {
        this.config = config;
        JDABuilder jdaBuilder = JDABuilder.create(config.getAPI_KEY(),//
                GatewayIntent.GUILD_MEMBERS,//
                GatewayIntent.DIRECT_MESSAGES,//
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
        Message eventMessage = event.getMessage();
        String message = eventMessage.getContentRaw();
        List<String> problemsChannelNames = Arrays.asList(config.getProblemChannelNames());
        List<String> questions = Arrays.asList(config.getQuestions());

        if (problemsChannelNames.contains(channelName)) {

            //check if message is correct
            if (!questions.stream().map(message::contains).reduce(Boolean::logicalAnd).orElse(true)) {
                badProblem(eventMessage, Problem.MISSING_QUESTION);
            } else if (message.contains("@")) {
                badProblem(eventMessage, Problem.MENTION);
            } else if (message.length() > 1500) {
                badProblem(eventMessage, Problem.TOO_LONG);
            } else {
                DiscordMember discordMember = APPLICATION.getDataManager().memberRepository.findByDiscordID(event.getAuthor().getIdLong()).orElse(null);
                if (discordMember != null) {
                    List<Question> list = new ArrayList<>(Arrays.asList(discordMember.getAsked()));
                    list.add(new Question(message, event.getAuthor().getIdLong(), eventMessage.getIdLong()));
                    discordMember.setAsked(list.toArray(new Question[0]));
                    APPLICATION.getDataManager().memberRepository.save(discordMember);
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
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        TextChannel channel = event.getChannel();
        String channelName = channel.getName();
        List<String> problemsChannelNames = Arrays.asList(config.getProblemChannelNames());
        if(!event.getReactionEmote().getEmoji().equals("✅")){
            return;
        }
        Message message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
        if (problemsChannelNames.contains(channelName)) {
            if(!event.getUser().equals(message.getAuthor())){
                MessageBuilder messageBuilder = new MessageBuilder();
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Problem marked as solved", message.getJumpUrl());
                builder.setDescription("Hello " + message.getAuthor().getAsMention() + " " + event.getUser().getAsMention() + " has marked your problem as solved by them! If they did indeed solve your issue, then react to this message with ✅");
                messageBuilder.setEmbed(builder.build());
                sendToBotSpam(messageBuilder.build());
            }
        } else if(channelName.toLowerCase().contains("bot-spam") && message.getAuthor().getIdLong() == 734848876481085521L){
            Optional<MessageEmbed> embedOptional = message.getEmbeds().stream().findFirst();
            if(!embedOptional.isPresent()){
                return;
            }
            MessageEmbed embed = embedOptional.get();
            String embedUrl = embed.getUrl();
            String embedDescription = embed.getDescription();
            List<String> mentions = Arrays.stream(embedDescription.split(" ")).filter(s -> s.contains("@")).collect(Collectors.toUnmodifiableList());
            Guild guild = channel.getGuild();
            User owner =  Objects.requireNonNull(guild.getMemberById(mentions.get(0).substring(2, mentions.get(0).length() - 2))).getUser();
            User solver = Objects.requireNonNull(guild.getMemberById(mentions.get(1).substring(2, mentions.get(1).length() - 2))).getUser();
            assert embedUrl != null;
            embedUrl = embedUrl.replace("https://discord.com/channels/", "");
            String[] ids = embedUrl.split("/");
            String guildID = ids[0];//unused; just for clarity
            String channelID = ids[1];
            String messageID = ids[2];
            MongoManager manager = APPLICATION.getDataManager();
            Optional<DiscordMember> ownerOptional = manager.memberRepository.findByDiscordID(owner.getIdLong());
            Optional<DiscordMember> solverOptional = manager.memberRepository.findByDiscordID(solver.getIdLong());
            if(ownerOptional.isEmpty() || solverOptional.isEmpty()) {
                return;
            }
            DiscordMember ownerMember = ownerOptional.get();
            DiscordMember solverMember = solverOptional.get();
            Question question = ownerMember.getAskedByMessageId(Long.parseLong(messageID)).orElseThrow();
            List<Question> answered = new ArrayList<>(Arrays.asList(solverMember.getAnswered()));
            answered.add(question);
            solverMember.setAnswered(answered.toArray(Question[]::new));
            manager.memberRepository.save(solverMember);
            Objects.requireNonNull(channel.getGuild().getTextChannelById(channelID)).deleteMessageById(messageID).queue();
        }
    }

    private void sendToBotSpam(Message message) {
        api.getGuilds().stream().map(Guild::getChannels).map(Collection::stream).reduce(Stream::concat).orElse(Stream.empty()).filter(guildChannel -> guildChannel.getName().equals("bot-spam")).map(guildChannel -> (MessageChannel) guildChannel).forEach(
                channel -> channel.sendMessage(message).queue()
        );
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
        LeaderboardApiApplication application = APPLICATION;
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
                .filter(l -> !l.getUser().isBot())
                .collect(Collectors.toList());
        newUsers.stream().map(Member::getUser).map(DiscordMember::fromUser).forEach(operations::insert);
    }

    public void purgeOldMessages() {
        APPLICATION.logger.info("purging!");
        api.getGuilds().forEach(this::purgeOldMessages);
    }

    private void purgeOldMessages(Guild guild) {
        List<String> problemsChannelNames = Arrays.asList(config.getProblemChannelNames());
        guild.getTextChannels().stream().filter(channel -> problemsChannelNames.contains(channel.getName())).forEach(this::purgeOldMessages);
    }

    private void purgeOldMessages(TextChannel channel) {
        OffsetDateTime _10daysbefore = OffsetDateTime.now().minus(Period.ofDays(10));
        //       channel.getHistory().getRetrievedHistory().stream().filter((Message m) -> m.getTimeCreated().isBefore(_10daysbefore)).filter(m->!m.isPinned()).map(Message::getId).forEach(l-> channel.deleteMessageById(l).queue());
        List<Message> history = channel.getHistoryFromBeginning(10).complete().getRetrievedHistory();
            history.stream().filter((Message m) -> m.getTimeCreated().isBefore(_10daysbefore)).filter(m -> !m.isPinned()).map(Message::getId).forEach(l -> {
                try {
                    channel.deleteMessageById(l).queue();
                } catch (Exception e){
                    APPLICATION.logger.warn("error when deleting message of id " + l , e);
                }
            });
    }


    public JDA getApi() {
        return api;
    }
}
