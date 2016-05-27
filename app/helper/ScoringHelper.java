package helper;

import models.*;
import play.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Luis on 20/05/2016.
 */
public class ScoringHelper {


    public static void handleMatchday(Season.MatchDay matchday) {

        HashMap<Integer, Float> playerPoints = new HashMap<>();

        for(Season.Match match : matchday.matches) {
            MatchEvent matchEvent = MatchEvent.findById(match._id);
            if (matchEvent != null) {
                for(MatchEvent.Player player : matchEvent.players) {
                    float value = 1;
                    if (player.startEleven) value+=1;
                    for(MatchEvent.Event event : player.events) {
                        // Cartão Amarelo
                        if (event.typeId == 2) value -= 1;
                        // Cartão Vermelho
                        if (event.typeId == 3) value -= 1.5;
                        // GOLO
                        if (event.typeId == 4) value += 5;
                        // Duplo Amarelo
                        if (event.typeId == 6) value -= 1;
                    }
                    playerPoints.put(player._id, (playerPoints.containsKey(player._id) ? playerPoints.get(player._id) : 0) + value);
                }
            }
        }
        //for(HashMap.Entry<Integer, Float> entry : playerPoints.entrySet()) {
        ///    Logger.info(entry.getKey() + " " + entry.getValue());
        //}

        for(League league : League.leagues().find().as(League.class)) {
            if (league.state != League.State.DURATION) continue;
            // Matchday counter ++

            // Get matchday

            ArrayList<League.Match> league_matchday = league.matches.get(0);

            // Calculate Points
            for(League.Match match : league_matchday) {
                UserTeam hometeam = league.teams.get(match.homePlayer);
                UserTeam awayteam = league.teams.get(match.awayPlayer);
                float homepoints = calculatePoints(hometeam, playerPoints);
                float awaypoints = calculatePoints(awayteam, playerPoints);
            }
        }
    }

    public static float calculatePoints(UserTeam team, HashMap<Integer, Float> playerPoints) {
        if (!team.hasTeam) return 0;

        float sum = 0;

        for(Map.Entry<String,Integer> player : team.lineup.entrySet()) {
            int player_id = Integer.parseInt(player.getKey());
            double points = (playerPoints.containsKey(player_id) ? playerPoints.get(player_id) : 0);
            Player playa = Player.findByDataId(player.getKey());
            if(playa.position == player.getValue())
                sum += points;
            else
                sum += 0.5*points;
        }
        for(String player : team.bench.keySet()) {
            int player_id = Integer.parseInt(player);
            sum += 0.5*(playerPoints.containsKey(player_id) ? playerPoints.get(player_id) : 0);

        }
        return sum;
    }
}
