package controllers;

import com.fasterxml.jackson.databind.JsonNode;
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

public class TeamController extends Controller {

    public Result getTeam(String league_id)
    {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");
        if (league_id.equals("")) return badRequest("Missing league_id");

        User user = User.findByToken(request().getHeader("Authorization"));
        if (user == null) return unauthorized("Invalid authorization token: user not found!");
        if (!user.leagues.containsKey(league_id)) return unauthorized("You are not in this league");

        League league = League.findById(league_id);
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

        UserTeam team = league.teams.get(user.id.toString());

        team.lineup.clear();
        for(JsonNode player : json.withArray("lineup")) {
            team.lineup.put(player.textValue(),true);
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
