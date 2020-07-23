package com.tebreca.leaderboardapi.api;

import com.tebreca.leaderboardapi.LeaderboardApiApplication;
import com.tebreca.leaderboardapi.api.requests.UserRequestBody;
import com.tebreca.leaderboardapi.api.responses.GetScoreResponseBody;
import com.tebreca.leaderboardapi.pojo.DiscordMember;
import com.tebreca.leaderboardapi.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
public class WebHandler {

    @Autowired
    LeaderboardApiApplication application = null;

    @PostMapping(path = "/getScore", params = {"username","tag"})
    public GetScoreResponseBody getScore(UserRequestBody requestBody){
        DiscordMember member = application.getDataManager().memberRepository.findByNameAndTag(requestBody.username, requestBody.tag).orElse(null);

        if(member == null){
            return new GetScoreResponseBody(-1);
        }

        return new GetScoreResponseBody(member.getAnswered().length);
    }

    @PostMapping(path = "/getAsked", params = {"username","tag"})
    public Question[] getAsked(UserRequestBody requestBody, Integer amount){
        DiscordMember member = application.getDataManager().memberRepository.findByNameAndTag(requestBody.username, requestBody.tag).orElse(null);

        if(member == null){
            return new Question[0];
        }

        return amount != null ? Arrays.copyOfRange(member.getAsked(), 0, amount) : member.getAsked();
    }

    @PostMapping(path = "/getAnswered", params = {"username","tag"})
    public Question[] getAnswered(UserRequestBody requestBody, Integer amount){
        DiscordMember member = application.getDataManager().memberRepository.findByNameAndTag(requestBody.username, requestBody.tag).orElse(null);

        if(member == null){
            return new Question[0];
        }

        return amount != null ? Arrays.copyOfRange(member.getAnswered(), 0, amount) : member.getAnswered();
    }


}
