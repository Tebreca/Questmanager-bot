package com.tebreca.leaderboardapi.api;

import com.tebreca.leaderboardapi.LeaderboardApiApplication;
import com.tebreca.leaderboardapi.api.requests.UserRequestBody;
import com.tebreca.leaderboardapi.api.responses.GetScoreResponseBody;
import com.tebreca.leaderboardapi.pojo.DiscordMember;
import com.tebreca.leaderboardapi.pojo.Question;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class WebHandler {

    @Autowired
    private LeaderboardApiApplication application = null;


    private JDA api = null;

    @GetMapping(path = "/getScore", params = {"username","tag"})
    public GetScoreResponseBody getScore(UserRequestBody requestBody){
        DiscordMember member = application.getDataManager().memberRepository.findByNameAndTag(requestBody.username, requestBody.tag).orElse(null);

        if(member == null){
            return new GetScoreResponseBody(-1);
        }

        return new GetScoreResponseBody(member.getAnswered().length);
    }

    @GetMapping(path = "/getAsked", params = {"username","tag"})
    public Question[] getAsked(UserRequestBody requestBody, Integer amount){
        DiscordMember member = application.getDataManager().memberRepository.findByNameAndTag(requestBody.username, requestBody.tag).orElse(null);

        if(member == null){
            return new Question[0];
        }

        return amount != null ? Arrays.copyOfRange(member.getAsked(), 0, amount) : member.getAsked();
    }

    @GetMapping(path = "/getAnswered", params = {"username","tag"})
    public Question[] getAnswered(UserRequestBody requestBody, Integer amount){
        DiscordMember member = application.getDataManager().memberRepository.findByNameAndTag(requestBody.username, requestBody.tag).orElse(null);

        if(member == null){
            return new Question[0];
        }

        return amount != null ? Arrays.copyOfRange(member.getAnswered(), 0, amount) : member.getAnswered();
    }


    @GetMapping(path = "/getTop", params = "count")
    public List<DiscordMember> getTop(Integer count){
        return application.getDataManager().memberRepository.findAllByOrderByAnsweredDesc(PageRequest.of(0, count)).toList();
    }

    public JDA getApi() {
        return api;
    }

    public void setApi(JDA api) {
        this.api = api;
    }
}
