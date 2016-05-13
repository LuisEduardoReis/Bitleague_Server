package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.draft.DraftManagerActor;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.MongoId;
import play.Play;
import uk.co.panaxiom.playjongo.PlayJongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class League extends Model {

    public static final int NUM_USERS = 8;
    public static final int SNAKE_ORDER[] = {0,1,2,3,4,5,6,7,7,6,5,4,3,2,1,0};

    public static enum State {INVITE, DRAFTING, DURATION}

    public static MongoCollection leagues() {
        return jongo.getCollection("leagues");
    }

    @MongoId
    public ObjectId id;

    public String name;

    public String creator;
    public Map<String, Boolean> users;

    public Map<String, UserTeam> teams;

    public State state;

    public League() {
        state = State.INVITE;
        users = new HashMap<>();
        teams = new HashMap<>();
    }

    public League insert() { leagues().save(this); return this; }

    public void remove() {
        leagues().remove(this.id);
    }

    public static League findById(String id) {
        return leagues().findOne(new ObjectId(id)).as(League.class);
    }
    public static League findByName(String name) {
        return leagues().findOne("{name: #}", name).as(League.class);
    }

    public boolean readyForDraft() {
        return true;
        //return users.size() == NUM_USERS;
    }

    public void generateTeams(List<DraftManagerActor.Pick> picks) {
        for(DraftManagerActor.Pick pick : picks)
        {
            if(teams.containsKey(pick.user_id))
            {
                teams.get(pick.user_id).addPlayer(pick.player_id);
            }
            else {
                UserTeam team = new UserTeam();
                team.addPlayer(pick.player_id);
                teams.put(pick.user_id, team);

            }
        }
    }
}
