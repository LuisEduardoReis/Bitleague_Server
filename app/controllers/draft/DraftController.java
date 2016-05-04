package controllers.draft;

import akka.actor.*;
import com.google.inject.Inject;
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
        if (!draftManagers.containsKey(league_id)) draftManagers.put(league_id, system.actorOf(DraftManagerActor.props));

        return draftManagers.get(league_id);
    }

    public Result startDraft(String league_id) {
        ActorRef draftManager = getDraftManager("test");

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
