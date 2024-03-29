package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import helper.ParameterParser;
import models.League;
import models.User;
import models.UserTeam;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TeamController extends Controller {

    public Result getTeam(String id)
    {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");
        if (id.equals("")) return badRequest("Missing league_id");

        User user = User.findByToken(request().getHeader("Authorization"));
        if (user == null) return unauthorized("Invalid authorization token: user not found!");
        if (!user.leagues.containsKey(id)) return unauthorized("You are not in this league");

        League league = League.findById(id);
        if(league == null ) return notFound("League not found");
        if(league.state!= League.State.DURATION) return badRequest("League hasn't drafted yet");

        UserTeam ut = league.teams.get(user.id.toString());

        return ok(Json.toJson(ut));
    }


    @BodyParser.Of(BodyParser.Json.class)
    public Result updateTeam()
    {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        User user = User.findByToken(request().getHeader("Authorization"));
        if (user == null) return unauthorized("Invalid authorization token: user not found!");

        JsonNode json = request().body().asJson();
        String args[] = {"league_id","lineup","bench"};
        String check = ParameterParser.checkParameters(json, args);
        if (!check.equals("ok")) return badRequest(check);

        League league = League.findById(json.findValue("league_id").textValue());
        if(league == null) return notFound("League not found");
        if(league.state!= League.State.DURATION) return badRequest("League hasn't drafted yet");
        if(!league.users.containsKey(user.id.toString())) return unauthorized("You are not in this league!");

        UserTeam team = league.teams.get(user.id.toString());

        team.lineup.clear();
        Iterator<Map.Entry<String,JsonNode>> i = json.get("lineup").fields();
        while(i.hasNext()) {
            Map.Entry<String,JsonNode> e = i.next();
            int position = e.getValue().intValue();
            if (position < 1 || position > 4) return badRequest("Invalid player position");
            team.lineup.put(e.getKey(),position);
        }

        team.bench.clear();
        for(JsonNode player : json.withArray("bench")) {
            team.bench.put(player.textValue(),true);
        }

        check = team.checkValidity();
        if (!check.equals("ok")) return badRequest(check);

        team.hasTeam = true;
        league.insert();
        return ok();
    }
}
