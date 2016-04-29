package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import helper.ParameterParser;
import models.League;
import models.User;
import org.bson.types.ObjectId;
import org.jongo.MongoCursor;
import play.api.libs.json.JsArray;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;
import views.html.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeagueController extends Controller {

    // League

    public Result getLeagues(int page, int page_size) {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        MongoCursor<League> leagues = League.leagues()
                .find().skip(page*page_size).limit(page_size)
                .as(League.class);
        return ok(Json.toJson(leagues));
    }

    public Result getLeague(String id) {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        League league = League.findById(id);
        if (league != null) return ok(Json.toJson(league));
        else return notFound("League not found");
    }


    @BodyParser.Of(BodyParser.Json.class)
    public Result addUser() {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        JsonNode json = request().body().asJson();
        String args[] = {"league_id"};
        ParameterParser params = new ParameterParser(json, args);
        if (!params.success) return badRequest(params.reason);

        League league = League.findById(params.get("league_id"));
        if(league == null) return notFound("League not found");

        User user_t = User.findByToken(request().getHeader("Authorization"));
        if (user_t == null) return unauthorized("Invalid authorization token: user not found!");


        league.users.put(user_t.id.toString(), true);
        league.insert();

        return ok(Json.toJson(league.users));
    }


    @BodyParser.Of(BodyParser.Json.class)
    public Result addLeague() {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        JsonNode json = request().body().asJson();
        String args[] = {"name"};
        ParameterParser params = new ParameterParser(json, args);
        if (!params.success) return badRequest(params.reason);

        User user_t = User.findByToken(request().getHeader("Authorization"));
        if (user_t == null) return unauthorized("Invalid authorization token: user not found!");


        String creator =  user_t.id.toString();
        String name = params.get("name");

        League league = new League();
        league.name = name;
        league.creator = creator;
        league.users.put(creator,true);
        league.insert();

        return created("League created");
    }

    public Result deleteLeague(String id) {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        User user_t = User.findByToken(request().getHeader("Authorization"));
        if (user_t == null) return unauthorized("Invalid authorization token: user not found!");

        League league = League.findById(id);
        if(league != null) return  notFound("League not found");

        if(!(user_t.isAdmin|| league.creator.equals(user_t.id.toString()))) return unauthorized();

        if (league != null) {
            league.remove();
            return ok();
        } else
            return notFound("League not found");
    }

}