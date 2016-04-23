package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.MongoId;
import play.Play;
import uk.co.panaxiom.playjongo.PlayJongo;

public class User extends Model {

    public static MongoCollection users() {
        return jongo.getCollection("users");
    }

    @MongoId
    public String id;

    public String name;

    public User insert() { users().save(this); return this; }

    public void remove() {
        users().remove(this.id);
    }

    public static User findById(String id) {
        return users().findOne(new ObjectId(id)).as(User.class);
    }
    public static User findByName(String name) {
        return users().findOne("{name: #}", name).as(User.class);
    }

}
