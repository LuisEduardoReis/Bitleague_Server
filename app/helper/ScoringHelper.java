package helper;

import models.League;
import models.RealTeam;
import models.Season;
import play.Logger;

/**
 * Created by Luis on 20/05/2016.
 */
public class ScoringHelper {


    public static void handleMatchday(Season.MatchDay matchday) {
        for(League league : League.leagues().find().as(League.class)) {

            // Matchday counter ++

            // Calculate Points
        }
        Logger.info("test");
    }
}
