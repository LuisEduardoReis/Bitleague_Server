package controllers;

import akka.actor.*;
import com.fasterxml.jackson.databind.JsonNode;
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
    public static ActorRef draftManager;

    @Inject
    public DraftController(ActorSystem system) {
        this.system = system;
        this.draftManager = system.actorOf(DraftManager.props);
    }

    public static class DraftManager extends UntypedActor {

        public static Props props = Props.create(DraftManager.class);

        public HashMap<String, List<ActorRef>> userActors = new HashMap<>();

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof AddUserActor) {
                AddUserActor a = (AddUserActor) message;
                if (!userActors.containsKey(a.draft_id)) userActors.put(a.draft_id, new ArrayList<>());

                List<ActorRef> users = userActors.get(a.draft_id);
                users.add(a.ref);

                for(ActorRef ar : users) {
                    ar.tell(new UserListUpdate(users),self());
                };
            }
        }

        public static class AddUserActor {
            public final String draft_id;
            public final ActorRef ref;
            public AddUserActor(String draft_id, ActorRef ref) {
                this.draft_id = draft_id;
                this.ref = ref;
            }
        }

        public static class UserListUpdate {
            public final List<ActorRef> users;
            public UserListUpdate(List<ActorRef> users) {
                this.users = users;
            }
        }
    }

    /*
    public Result scheduleDraft() {
        DraftManager
        system.scheduler().scheduleOnce(
                Duration.create(10, TimeUnit.MILLISECONDS),
                ,
                system.dispatcher(),
                null
        );
    }*/

    public LegacyWebSocket<String> socket() {
        //if (!request().hasHeader("Authorization")) return WebSocket.reject(unauthorized("Missing authorization header"));
        //User user_t = User.findByToken(request().getHeader("Authorization"));
        //if (user_t == null) return WebSocket.reject(unauthorized("Invalid authorization token: user not found!"));

        return WebSocket.withActor(SocketActor::props);
    }



    public static class SocketActor extends UntypedActor {

        public static Props props(ActorRef out) { return Props.create(SocketActor.class, out); }

        private final ActorRef out;
        private boolean initialized;
        private String draft_id;
        private String user_id;


        public SocketActor(ActorRef out) {
            this.initialized = false;
            this.out = out;
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

                        draftManager.tell(new DraftManager.AddUserActor(json.get("data").get("draft").textValue(),self()),self());
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
                out.tell("update",self());
            }
        }
    }

    public Result poll(String draft_id) {
        if (!request().hasHeader("Authorization")) return unauthorized("Missing authorization header");
        User user_t = User.findByToken(request().getHeader("Authorization"));
        if (user_t == null) return unauthorized("Invalid authorization token: user not found!");

        Draft draft = Draft.findById(draft_id);
        if (draft == null) return notFound("Draft not found");

        draft.online_users.put(user_t.id.toString(),true);
        draft.insert();

        return ok();
    }

}
