package controllers;


import play.mvc.*;

import static play.mvc.Results.ok;

/**
 * Created by Win7 on 15-5-2016.
 */
public class MatchdayController {

    public static boolean locked = false;

    public Result getLockState() {return ok(locked ? "true" : "false");}

    public Result postLock() {
        locked = true;
        return ok();
    }

    public Result postUnlock() {
        locked = false;
        return ok();
    }

    public Result postMatchday() {
        return ok();
    }
}
