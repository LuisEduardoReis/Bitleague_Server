package controllers;


import helper.ScoringHelper;
import models.Season;
import play.mvc.*;

import java.util.List;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

/**
 * Created by Win7 on 15-5-2016.
 */
public class MatchdayController {

    public static boolean locked = false;
    public static int matchday = 0;

    public Result getLockState() {return ok(locked ? "true" : "false");}

    public Result postLock() {
        locked = true;
        return ok();
    }

    public Result postUnlock() {
        locked = false;
        return ok();
    }

    public Result postMatchday(Integer matchday_num) {
        List<Season.MatchDay> matchDays = Season.seasons().find().as(Season.class).next().leagues.get(0).matchdays;
        if (matchday_num < 1 || matchday_num > matchDays.size()) return badRequest();

        ScoringHelper.handleMatchday(matchDays.get(matchday_num-1));
        return ok();
    }

}
