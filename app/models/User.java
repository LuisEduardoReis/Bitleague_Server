package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jongo.marshall.jackson.oid.MongoId;
import play.Logger;
import play.Play;
import play.libs.Json;
import uk.co.panaxiom.playjongo.PlayJongo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;

public class User extends Model {

    public static MongoCollection users() {
        return jongo.getCollection("users");
    }

    @MongoId
    public ObjectId id;
    public String name;
    public String facebook_id;
    public String picture;
    public String token;
    public boolean isAdmin;
    public Date created_at;
    public Date active_at;
    public HashMap<String,Boolean> leagues = new HashMap<>();

    public User() {
        this.created_at = new Date();
        this.isAdmin = false;
    }

    public User insert() { this.active_at = new Date(); users().save(this); return this; }

    public void remove() { users().remove(this.id); }

    public static User findById(String id) {
        try {return users().findOne(new ObjectId(id)).as(User.class);}
        catch(IllegalArgumentException e) { return null; }
    }


    public static User findByName(String name) {
        return users().findOne("{name: #}", name).as(User.class);
    }
    public static User findByToken(String token) {return users().findOne("{token: #}", token).as(User.class); }

    public static User findOrCreateByFacebookId(String fb_id, String access_token) {
        if (User.users().find("{facebook_id:#}", fb_id).as(User.class).count() == 0) {
            String name = "", picture="";
            try {
                String debug_token_url = "https://graph.facebook.com/" + fb_id;
                debug_token_url += "?access_token=" + access_token;
                debug_token_url += "&fields=name,picture";
                URL url = new URL(debug_token_url);
                URLConnection conn = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String res = in.readLine();
                in.close();

                JsonNode fb_json = Json.parse(res);

                if (fb_json != null) {
                    name = fb_json.findValue("name").textValue();
                    picture = fb_json.findValue("picture").findValue("data").findValue("url").textValue();
                }
            } catch (Exception e) {
            }
            User user = new User();
            user.name = name;
            user.picture = picture;
            user.facebook_id = fb_id;
            user.insert();
        };
        return User.users().findOne("{facebook_id:#}", fb_id).as(User.class);
    }


}
