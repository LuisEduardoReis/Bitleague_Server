package models;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    public static MongoCollection leagues() {
        return jongo.getCollection("leagues");
    }

    @MongoId
    public ObjectId id;

    public String name;

    public String creator;
    public Map<String, Boolean> users = new HashMap<>();

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
}
