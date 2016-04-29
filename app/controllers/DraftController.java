package controllers;

import models.Draft;
import models.User;
import play.libs.Json;
import play.mvc.*;

public class DraftController extends Controller {

    public Result poll(String draft_id) {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");
        User user_t = User.findByToken(request().getHeader("Authorization"));
        if (user_t == null) return unauthorized("Invalid authorization token: user not found!");

        Draft draft = Draft.findById(draft_id);
        if (draft == null) return notFound("Draft not found");

        draft.users.put(user_t.id.toString(),System.currentTimeMillis());
        draft.insert();

        return ok();
    }

}
