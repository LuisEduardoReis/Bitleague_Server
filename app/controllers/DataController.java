package controllers;

import models.MatchEvent;
import models.Player;
import models.RealTeam;
import models.Season;
import play.Logger;
import play.libs.Json;
import play.mvc.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import static play.mvc.Results.ok;


public class DataController {

    // Players
    public static HashMap<String, Player> getPlayersHash() {

        HashMap<String, Player> players = new HashMap<>();
        for(Player player : Player.players().find().as(Player.class)) {
            players.put(player.data_id, player);
        }
        return players;
    }

    public void generatePlayers() {
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


    // Team
    public static HashMap<Integer, RealTeam> getTeamsHash() {

        HashMap<Integer, RealTeam> teams = new HashMap<>();
        for(RealTeam team : RealTeam.realTeams().find().as(RealTeam.class)) {
            teams.put(team.data_id, team);
        }
        return teams;
    }

    public void generateTeams() {
        Season season = Season.seasons().findOne().as(Season.class);
        for(Season.League league : season.leagues) {
            for(Season.Team team_s : league.teams) {
                RealTeam team = new RealTeam();
                team.data_id = team_s._id;
                team.name = team_s.name;

                team.insert();
            }
        }
    }
    public Result genTeams() { generateTeams(); return ok(); }
    public Result getTeams() { return ok(Json.toJson(getTeamsHash())); }

    // Match events

    public Result getMatchEvents() {return ok(Json.toJson(MatchEvent.matchEvents().find().as(MatchEvent.class)));}

    // Matchdays

    public Result getMatchDays() {
        ArrayList<Season.MatchDay> matchDays = new ArrayList<>();
        Season season = Season.seasons().findOne().as(Season.class);
        for(Season.League league : season.leagues)
            for(Season.MatchDay matchDay : league.matchdays)
                matchDays.add(matchDay);
        return ok(Json.toJson(matchDays));
    }

}
