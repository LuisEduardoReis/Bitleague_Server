package models;

import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.MongoId;

/**
 * Created by Win7 on 15-5-2016.
 */
public class RealTeam extends Model {

    public static MongoCollection realTeams() { return jongo.getCollection("teams"); }

     @MongoId
    public ObjectId id;

    public Integer data_id;
    public String name;

    public RealTeam insert() { realTeams().save(this); return this; }

    public void remove() {
        realTeams().remove(this.id);
    }

}
