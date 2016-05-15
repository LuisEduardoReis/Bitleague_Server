package models;

import play.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserTeam {

    public Map<String,Boolean> players;
    public boolean hasTeam;
    public Map<String,Boolean> lineup;
    public Map<String,Boolean> bench;

    public UserTeam() {
        hasTeam = false;

        players = new HashMap<>();
        lineup = new HashMap<>();
        bench = new HashMap<>();
    }

    public String checkValidity() {
        int[] pos = new int[4];
        if (lineup.size() != 11 ) return "Lineup must have 11 players!";
        for(String player_id : lineup.keySet()) {
            if (!players.containsKey(player_id)) return "Illegal player in team!";
            Player player = Player.findByDataId(player_id);
            Logger.info(player_id + " " + player.positionDescription);
            pos[player.position-1]++;
        }
        if (bench.size() > 7 ) return "Bench can only have up to 7 players";
        for(String player_id : bench.keySet()) {
            if (!players.containsKey(player_id)) return "Illegal player in team!";
        }

        if (pos[0] !=1) return "Lineup must have exactly 1 goalkeeper";
        if (pos[1] < 3) return "Lineup can't have less than 3 defences";
        if (pos[1] > 5) return "Lineup can't have more than 5 defences";
        if (pos[2] < 2) return "Lineup can't have less than 2 midfielders";
        if (pos[2] > 5) return "Lineup can't have more than 5 midfielders";
        if (pos[3] < 1) return "Lineup must have at least 1 striker";
        if (pos[3] > 4) return "Lineup can't have more than 4 strikers";

        return "ok";
    }


}
