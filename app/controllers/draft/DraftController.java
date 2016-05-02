package controllers.draft;

import akka.actor.*;
import akka.stream.impl.fusing.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Draft;
import models.User;
import play.Logger;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.*;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        if (!draftManagers.containsKey(league_id)) draftManagers.put(league_id, system.actorOf(DraftManager.props));

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
        draftManager.tell(new DraftManager.Start(cancel), null);

        return ok();
    }

    public LegacyWebSocket<String> socket() {
        return WebSocket.withActor(SocketActor::props);
    }



    public static class SocketActor extends UntypedActor {

        public static Props props(ActorRef out) { return Props.create(SocketActor.class, out); }

        public final ActorRef out;
        public boolean initialized;
        public String draft_id;
        public String user_id;
        public ActorRef draftManager;


        public SocketActor(ActorRef out) {
            Logger.info("open");
            this.initialized = false;
            this.out = out;
        }

        @Override
        public void postStop() throws Exception {
            if (initialized) {
                Logger.info("close");
                draftManager.tell(new DraftManager.RemoveUserActor(self()), self());
            }
        }

        public void onReceive(Object message) throws Exception {
            if (message instanceof String) {
                try {
                    JsonNode json = Json.parse((String) message);
                    if (!initialized) {
                        if (!json.get("event").textValue().equals("init")) return;
                        String authorization = json.get("data").get("Authorization").textValue();
                        User user_t = User.findByToken(authorization);
                        if (user_t == null) throw new RuntimeException("Invalid authorization token: user not found!");

                        draftManager = getDraftManager(json.get("data").get("league_id").asText());
                        draftManager.tell(new DraftManager.AddUserActor(user_t.id.toString(), self()), self());

                        out.tell("initialized", self());
                        initialized = true;
                    } else {

                    }
                } catch(Exception e) {
                    Logger.error(e.toString());
                    self().tell(PoisonPill.getInstance(), self());
                }
            } else if (message instanceof DraftManager.UserListUpdate) {
                DraftManager.UserListUpdate update = (DraftManager.UserListUpdate) message;
                ObjectNode res = Json.newObject();
                res.put("event","user_list");
                ArrayNode list = Json.newArray();
                for(DraftManager.UserActor user : update.users) {
                    list.add(user.name);
                }
                res.put("data",list);
                out.tell(res.toString(),self());
            }
        }
    }


}
