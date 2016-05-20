package helper;

import models.League;
import models.RealTeam;
import models.Season;
import play.Logger;

import java.util.Map;

/**
 * Created by Luis on 20/05/2016.
 */
public class ScoringHelper {


    public static void handleMatchday(Season.MatchDay matchday) {
        for(League league : League.leagues().find().as(League.class)) {

            // Matchday counter ++

            // Calculate Points
            for(int i = 0; i < league.matches.size(); i++){
                for(int j = 0; j < league.matches.get(i).size(); j+=2){
                    String homePlayer = league.matches.get(i).get(j).homePlayer;
                    String awayPlayer = league.matches.get(i).get(j+1).awayPlayer;

                    Map<String,Integer> homePlayerLineUp = league.teams.get(homePlayer).lineup;
                    Map<String,Integer> awayPlayerLineUp = league.teams.get(awayPlayer).lineup;
                    for(int k = 0; k < homePlayerLineUp.size(); k++){
                    }
                    for(int l = 0; l < awayPlayerLineUp.size(); l++){
                    }
                }
            }
        }
        Logger.info("test");
    }
}
