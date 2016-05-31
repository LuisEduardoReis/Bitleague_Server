package controllers.draft;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import helper.ParameterParser;
import models.League;
import models.User;
import play.Logger;
import play.libs.Json;

import java.util.ArrayList;
import java.util.HashMap;

public class DraftUserActor extends UntypedActor {

    public static Props props(ActorRef out) { return Props.create(DraftUserActor.class, out); }

    public final ActorRef out;
    public boolean initialized;
    public String user_id;
    public ActorRef draftManager;


    public DraftUserActor(ActorRef out) {
        this.initialized = false;
        this.out = out;
    }

    @Override
    public void postStop() throws Exception {
        if (initialized) {
            draftManager.tell(new DraftManagerActor.RemoveUserActor(self()), self());
        }
    }

    public void onReceive(Object message) throws Exception {
        // Websocket messages
        if (message instanceof String) {
            try {
                JsonNode json = Json.parse((String) message);
                String event = json.get("event").textValue();

                if (!initialized && event.equals("init")) {
                    String args[] = {"Authorization", "league_id"};
                    ParameterParser parameterParser = new ParameterParser(json.get("data"), args);
                    if (!parameterParser.success) throw new RuntimeException(parameterParser.reason);

                    User user_t = User.findByToken(parameterParser.get("Authorization"));
                    if (user_t == null) throw new RuntimeException("Invalid authorization token: user not found!");
                    user_id = user_t.id.toString();

                    League league = League.findById(parameterParser.get("league_id"));
                    if (league == null) throw new RuntimeException("League not found!");
                    if (league.state == League.State.INVITE) throw new RuntimeException("League draft hasn't started yet");
                    if (league.state == League.State.DURATION) throw new RuntimeException("League has already drafted.");

                    draftManager = DraftController.getDraftManager(parameterParser.get("league_id"));
                    draftManager.tell(new DraftManagerActor.AddUserActor(user_t.id.toString(), self()), self());

                    out.tell("initialized",self());
                    initialized = true;
                } else if (event.equals("removeFavourite")){
                    if (!json.has("data") || !json.get("data").has("player_id")) return;

                    draftManager.tell(new DraftManagerActor.RemoveFavourite(user_id, json.get("data").get("player_id").asText()), self());
                }else if (event.equals("pick")){
                    if (!json.has("data") || !json.get("data").has("player_id")) return;

                    draftManager.tell(new DraftManagerActor.MakePick(user_id, json.get("data").get("player_id").asText()), self());
                } else if (event.equals("favourite")){
                    if (!json.has("data") || !json.get("data").has("player_id")) return;

                    draftManager.tell(new DraftManagerActor.FavouritePick(user_id, json.get("data").get("player_id").asText()), self());
                }
            } catch(Exception e) {
                Logger.error(e.toString());
                out.tell("error - " + e.toString(), self());
                out.tell("close", self());
                self().tell(PoisonPill.getInstance(), self());
            }
        // Actor messages
        } else if (message instanceof DraftManagerActor.UserListUpdate) {
            DraftManagerActor.UserListUpdate update = (DraftManagerActor.UserListUpdate) message;
            ObjectNode res = Json.newObject();
            res.put("event","user_list");
            ObjectNode data = Json.newObject();
                data.put("users", Json.toJson(update.users));
                data.put("usernames", Json.toJson(update.usernames));
                data.put("online", Json.toJson(update.online));
            res.put("data",data);
            out.tell(res.toString(),self());
        } else if (message instanceof DraftManagerActor.PickList) {
            DraftManagerActor.PickList picks = (DraftManagerActor.PickList) message;
            ObjectNode res = Json.newObject();
                res.put("event","pick_list");
                res.put("data",Json.toJson(picks.picks));
            out.tell(res.toString(), self());
        }else if (message instanceof DraftManagerActor.RemoveFavourite) {
            DraftManagerActor.RemoveFavourite pick = (DraftManagerActor.RemoveFavourite) message;
            ObjectNode res = Json.newObject();
            res.put("event","removeFavourite");
            res.put("data",Json.toJson(pick));
            out.tell(res.toString(), self());
        }  else if (message instanceof DraftManagerActor.FavouritePick) {
            DraftManagerActor.FavouritePick pick = (DraftManagerActor.FavouritePick) message;
            ObjectNode res = Json.newObject();
            res.put("event","favourite");
            res.put("data",Json.toJson(pick));
            out.tell(res.toString(), self());
        } else if (message instanceof  DraftManagerActor.MakePick) {
            DraftManagerActor.MakePick pick = (DraftManagerActor.MakePick) message;
            ObjectNode res = Json.newObject();
                res.put("event","pick");
                res.put("data",Json.toJson(pick));
            out.tell(res.toString(), self());
        } else if (message instanceof DraftManagerActor.TurnUpdate) {
            DraftManagerActor.TurnUpdate update = (DraftManagerActor.TurnUpdate) message;
            ObjectNode res = Json.newObject();
                res.put("event","turn_update");
                res.put("data",Json.toJson(update));
            out.tell(res.toString(), self());
        }
    }
}
