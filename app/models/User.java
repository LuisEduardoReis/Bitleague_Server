package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jongo.marshall.jackson.oid.MongoId;
import play.Logger;
import play.Play;
import uk.co.panaxiom.playjongo.PlayJongo;

public class User extends Model {

    public static MongoCollection users() {
        return jongo.getCollection("users");
    }

    @MongoId
    public ObjectId id;
    public String name;
    public String facebook_id;
    public String token;


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
    public static User findByToken(String token) {return users().findOne("{name: #}", token).as(User.class); }

    public static User findOrCreateByFacebookId(String fb_id) {
        if (User.users().find("{facebook_id:#}", fb_id).as(User.class).count() == 0) {
            User user = new User();
            user.name = "test";
            user.facebook_id = fb_id;
            user.insert();
        };
        return User.users().findOne("{facebook_id:#}", fb_id).as(User.class);
    }


}
