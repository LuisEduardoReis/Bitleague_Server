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

    public Result index() { return ok(index.render()); }

    public Result chat() { return ok(chat.render()); }


    // League

    public Result getLeagues(int page, int page_size) {
        MongoCursor<League> leagues = League.leagues()
                .find().skip(page*page_size).limit(page_size)
                .as(League.class);
        return ok(Json.toJson(leagues));
    }

    public Result getLeague(String id) {
        League league = League.findById(id);
        if (league != null)
            return ok(Json.toJson(league));
        else
            return notFound("League not found");
    }


    //to do:
    //verificar creator e users
    //verificar autorização
    //
    @BodyParser.Of(BodyParser.Json.class)
    public Result addUser() {

        JsonNode json = request().body().asJson();
        String args[] = {"id", "user_id"};
        ParameterParser params = new ParameterParser(json, args);
        if (!params.success) return badRequest(params.reason);

        League league = League.findById(params.get("id"));
        if(league == null)
            return notFound();

        if(league.users != null) {
            ArrayList<String> temp = new ArrayList<String>(Arrays.asList(league.users));
            temp.add(params.get("user_id"));
            league.users = temp.toArray(new String[0]);
        }
        else
            league.users = new String[]{params.get("user_id")};

        league.insert();

        return ok();
    }


    @BodyParser.Of(BodyParser.Json.class)
    public Result addLeague() {
        JsonNode json = request().body().asJson();
        String args[] = {"id", "name"};
        ParameterParser params = new ParameterParser(json, args);
        if (!params.success) return badRequest(params.reason);


        String creator =  params.get("id");
        String name = params.get("name");

        League league = new League();
        league.name = name;
        league.creator = creator;
        league.users =  new String[0];


        league.insert();

        return created("League created");
    }

    public Result deleteLeague(String id) {
        League league = League.findById(id);
        if (league != null) {
            league.remove();
            return ok();
        } else
            return notFound("League not found");
    }

}