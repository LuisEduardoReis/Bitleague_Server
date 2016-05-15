package controllers;

import models.Player;
import models.Season;
import play.Logger;
import play.libs.Json;
import play.mvc.*;

import java.util.ArrayList;
import java.util.HashMap;

import static play.mvc.Results.ok;


public class DataController {

    public static HashMap<String, Player> getPlayersHash() {

        HashMap<String, Player> players = new HashMap<>();
        for(Player player : Player.players().find().as(Player.class)) {
            players.put(player.data_id, player);
        }
        return players;
    }

    public static void generatePlayers() {
        String[] positionDescriptions = {"Goalkeeper", "Defender", "Midfielder", "Forward"};

        Season season = Season.seasons().findOne().as(Season.class);
        for(Season.League league : season.leagues) {
            for(Season.Team team : league.teams) {
                for(Season.Player player_s : team.players) {
                    Player player = new Player();
                    player.data_id = ""+player_s._id;
                    player.name = player_s.name;
                    player.number = player_s.number;
                    player.position = player_s.position;
                    player.positionDescription = positionDescriptions[player_s.position-1];
                    player.team = team.name;

                    player.insert();
                }
            }
        }
    }

    public Result genPlayers() { generatePlayers(); return ok(); }
    public Result getPlayers() {
        return ok(Json.toJson(getPlayersHash()));
    }
}
