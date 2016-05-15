package controllers.draft;

import akka.actor.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import helper.ParameterParser;
import models.League;
import models.User;
import play.mvc.*;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DraftController extends Controller {

    public static ActorSystem system;
    public static HashMap<String, ActorRef> draftManagers;

    @Inject
    public DraftController(ActorSystem system) {
        this.system = system;
        this.draftManagers = new HashMap<>();
    }

    public static ActorRef getDraftManager(String league_id) {
        if (!draftManagers.containsKey(league_id)) {
            ActorRef draftManager = system.actorOf(DraftManagerActor.props);
            draftManager.tell(new DraftManagerActor.Init(league_id),null);
            draftManagers.put(league_id, draftManager);
        }

        return draftManagers.get(league_id);
    }

    public Result startDraft() {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");

        JsonNode json = request().body().asJson();
        String args[] = {"id"};
        ParameterParser params = new ParameterParser(json, args);
        if (!params.success) return badRequest(params.reason);

        String league_id = params.get("id");

        League league = League.findById(league_id);
        if (league == null) return notFound("League not found");

        User user_t = User.findByToken(request().getHeader("Authorization"));
        if (user_t == null) return unauthorized("Invalid authorization token: user not found!");

        if (!league.creator.equals(user_t.id.toString())) return unauthorized("You are not this league's creator!");
        if (league.state != League.State.INVITE && league.state != League.State.DRAFTING) return badRequest("League already drafted!");

        if (!league.readyForDraft()) return badRequest("This league's draft is not ready to start");

        league.state = League.State.DRAFTING;
        league.insert();

        ActorRef draftManager = getDraftManager(league_id);

        Cancellable cancel = system.scheduler().schedule(
                Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
                Duration.create(1, TimeUnit.SECONDS),     //Frequency 30 minutes
                draftManager,
                "tick",
                system.dispatcher(),
                null
        );
        draftManager.tell(new DraftManagerActor.Start(cancel), null);

        return ok();
    }

    public LegacyWebSocket<String> socket() {
        return WebSocket.withActor(DraftUserActor::props);
    }

}
