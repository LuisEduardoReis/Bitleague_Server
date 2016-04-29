package controllers;

import models.Season;
import play.Logger;
import play.libs.Json;
import play.mvc.*;

import java.util.ArrayList;

import static play.mvc.Results.ok;


public class DataController {

    public Result getPlayers() {
        Season season = Season.seasons().findOne().as(Season.class);
        ArrayList<Season.Player> players = new ArrayList<>();
        for(Season.League league : season.leagues) {
            for(Season.Team team : league.teams) {
                for(Season.Player player : team.players) {
                    player.team = team.name;
                    players.add(player);
                }
            }
        }
        return ok(Json.toJson(players));
    }
}
