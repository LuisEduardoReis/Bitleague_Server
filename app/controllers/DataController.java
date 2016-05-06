package controllers;

import models.Season;
import play.Logger;
import play.libs.Json;
import play.mvc.*;

import java.util.ArrayList;
import java.util.HashMap;

import static play.mvc.Results.ok;


public class DataController {

    public static HashMap<String, Season.Player> getPlayersHash() {
        Season season = Season.seasons().findOne().as(Season.class);
        HashMap<String, Season.Player> players = new HashMap<>();
        for(Season.League league : season.leagues) {
            for(Season.Team team : league.teams) {
                for(Season.Player player : team.players) {
                    player.team = team.name;
                    players.put(""+player._id, player);
                }
            }
        }
        return players;
    }

    public Result getPlayers() {
        return ok(Json.toJson(getPlayersHash()));
    }
}
