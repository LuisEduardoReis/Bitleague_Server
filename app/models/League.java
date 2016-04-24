package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.MongoId;
import play.Play;
import uk.co.panaxiom.playjongo.PlayJongo;

public class League extends Model {

    public static MongoCollection leagues() {
        return jongo.getCollection("leagues");
    }

    @MongoId
    public String id;

    public String name;

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

}
