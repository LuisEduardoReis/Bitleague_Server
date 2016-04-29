package models;

import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.MongoId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Draft extends Model {

    public static MongoCollection drafts() { return jongo.getCollection("drafts");}

    @MongoId
    public ObjectId id;
    public String league;
    public Map<String, Long> users = new HashMap<>();


    public static Draft findById(String id) {
        try {return drafts().findOne(new ObjectId(id)).as(Draft.class);}
        catch(Exception e) { return null; }
    }

    public Draft insert() { drafts().save(this); return this; }

    public void remove() { drafts().remove(this.id); }
}
