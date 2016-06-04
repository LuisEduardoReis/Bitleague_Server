package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import helper.ParameterParser;
import models.League;
import models.Player;
import models.User;
import models.UserTeam;
import org.bson.types.ObjectId;
import org.jongo.MongoCursor;
import play.Logger;
import play.api.libs.json.DefaultReads;
import play.api.libs.json.JsArray;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;
import views.html.chat;

import java.util.*;

public class LeagueController extends Controller {

    // League

    public Result getLeagues() {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        User user = User.findByToken(request().getHeader("Authorization"));
        if (user == null) return unauthorized("Invalid authorization token: user not found!");

        ArrayNode leagues = Json.newArray();
        for(String league_id : user.leagues.keySet()) {
            League league = League.findById(league_id);
            ObjectNode node = Json.newObject();
                node.put("id", league.id.toString());
                node.put("name", league.name);
                node.put("state", league.state.toString());
                node.put("numTeams", league.users.size());
            leagues.add(node);
        }

        return ok(leagues);
    }



    public Result getLeague(String id) {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        League league = League.findById(id);
        if (league == null) return notFound("League not found");

        User user_t = User.findByToken(request().getHeader("Authorization"));
        if (user_t == null) return unauthorized("Invalid authorization token: user not found!");

        if (!league.users.containsKey(user_t.id.toString())) return unauthorized("You do not have access to this league!");

        ObjectNode league_json = Json.newObject();
            league_json.put("id", id);
            league_json.put("name", league.name);
            league_json.put("creator", league.creator);
            league_json.put("state", league.state.toString());
            league_json.put("matches", Json.toJson(league.matches));
            league_json.put("turn_time", league.turn_timer);
            ArrayNode users = Json.newArray();
            for(String user_id : league.users.keySet()) {
                ObjectNode user_node = Json.newObject();
                    User user = User.findById(user_id);
                    user_node.put("id",user_id);
                    user_node.put("name",user.name);
                    user_node.put("picture",user.picture);
                users.add(user_node);
            }
            league_json.put("users",users);
        return ok(Json.toJson(league_json));
    }




    @BodyParser.Of(BodyParser.Json.class)
    public Result addUser() {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        JsonNode json = request().body().asJson();
        String args[] = {"id"};
        ParameterParser params = new ParameterParser(json, args);
        if (!params.success) return badRequest(params.reason);

        League league = League.findById(params.get("id"));
        if(league == null) return notFound("League not found");

        if (league.state != League.State.INVITE) return badRequest("League has already started");

        User user = User.findByToken(request().getHeader("Authorization"));
        if (user == null) return unauthorized("Invalid authorization token: user not found!");


        league.users.put(user.id.toString(), true);
        league.insert();

        user.leagues.put(league.id.toString(), true);
        user.insert();

        return ok(Json.toJson(league.users));
    }


    @BodyParser.Of(BodyParser.Json.class)
    public Result addLeague() {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        JsonNode json = request().body().asJson();
        String args[] = {"name", "time"};
        ParameterParser params = new ParameterParser(json, args);
        if (!params.success) return badRequest(params.reason);

        User user = User.findByToken(request().getHeader("Authorization"));
        if (user == null) return unauthorized("Invalid authorization token: user not found!");


        String creator =  user.id.toString();
        String name = params.get("name");

        int turn_timer = json.findValue("time").asInt();

        if(turn_timer < 10)
            return badRequest("Your league's turns mustn't last less than 10 seconds!");

        if(turn_timer > 60)
            return badRequest("Your league's turns mustn't be longer than 60 seconds!");

        League league = new League();
        league.name = name;
        league.creator = creator;
        league.users.put(creator,true);
        league.turn_timer = turn_timer;
        league.insert();

        user.leagues.put(league.id.toString(), true);
        user.insert();

        return created("League created");
    }

    public Result deleteLeague(String id) {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        User user_t = User.findByToken(request().getHeader("Authorization"));
        if (user_t == null) return unauthorized("Invalid authorization token: user not found!");

        League league = League.findById(id);
        if(league == null) return  notFound("League not found");

        if(!(user_t.isAdmin|| league.creator.equals(user_t.id.toString()))) return unauthorized();

        for(String user_id : league.users.keySet()) {
            User user = User.findById(user_id);
            user.leagues.remove(id);
            user.insert();
        }
        league.remove();
        return ok();
    }

    public Result getPlayers(String id) {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        User user_t = User.findByToken(request().getHeader("Authorization"));
        if (user_t == null) return unauthorized("Invalid authorization token: user not found!");

        League league = League.findById(id);
        if(league == null) return  notFound("League not found");

        if(!(user_t.isAdmin|| league.creator.equals(user_t.id.toString()))) return unauthorized();

        HashMap<String, String> team_ownership = new HashMap<>();
        for(Map.Entry<String,UserTeam> pair : league.teams.entrySet())
            for(String player : pair.getValue().players.keySet())
                team_ownership.put(player, pair.getKey());

        ArrayNode list = Json.newArray();
        for(Player player : Player.players().find().as(Player.class)) {
            ObjectNode node = Json.newObject();
                node.put("id", player.data_id);
                node.put("name", player.name);
                node.put("position", player.position);
                node.put("positionDescription", player.positionDescription);
                if (team_ownership.containsKey(player.data_id)) {
                    String owner = team_ownership.get(player.data_id);
                    User user = User.findById(owner);
                    node.put("owner", owner);
                    node.put("ownerName", user.name);
                } else {
                    node.put("owner","");
                    node.put("ownerName", "Free");
                }
            list.add(node);
        }


        return ok(list);
    }

}