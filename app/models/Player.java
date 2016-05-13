package models;

import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.MongoId;

/**
 * Created by Luis on 13/05/2016.
 */
public class Player extends Model{

    public static MongoCollection players() { return jongo.getCollection("players"); }

    public static int GOALKEEPER = 1;
    public static int DEFENCE = 2;
    public static int MIDFIELDER = 3;
    public static int STRIKER = 4;


    @MongoId
    public ObjectId id;

    public String data_id;
    public String name;
    public Integer number;
    public Integer position;
    public String positionDescription;

    public String team;

    public Player insert() { players().save(this); return this; }

    public void remove() {
        players().remove(this.id);
    }

    public static Player findById(String id) {
        return players().findOne(new ObjectId(id)).as(Player.class);
    }
    public static Player findByDataId(String id) {
        return players().findOne("{data_id: #}", id).as(Player.class);
    }
}
