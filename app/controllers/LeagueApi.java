package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import models.League;
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
import java.util.List;

public class LeagueApi extends Controller {

    public Result index() { return ok(index.render()); }

    public Result chat() { return ok(chat.render()); }


    // User

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
            return notFound("User not found");
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result addLeague() {
        JsonNode json = request().body().asJson();
        String name = json.findPath("name").textValue();
        if (name == null)  return badRequest("Missing parameter 'name'.");
        if (name == "")  return badRequest("Parameter 'name' can't be null.");
        if (League.findByName(name) != null) return badRequest("League " + name + " already exists.");

        League user = new League();
        user.name = name;
        user.insert();

        return created("User created");
    }

    public Result deleteLeague(String id) {
        League league = League.findById(id);
        if (league != null) {
            league.remove();
            return ok();
        } else
            return notFound("User not found");
    }

}