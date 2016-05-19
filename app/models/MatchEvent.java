package models;

import org.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Win7 on 15-5-2016.
 */
public class MatchEvent extends Model {

    public int _id;
    public int minutesPart1;
    public int minutesPart2;
    public int extraTimePart1;
    public int extraTimePart2;
    public List<Player> players;

    public static MongoCollection matchEvents() { return jongo.getCollection("matchEvents"); }

    public MatchEvent() {
        players = new ArrayList<>();
    }

    public static class Player {
        public int _id;
        public int teamId;
        public boolean startEleven;
        public List<Event> events;

        public Player() {
            events = new ArrayList<>();
        }
    }

    public static class Event {
        public int _id;
        public String description;
        public int typeId;
        public int minute;
        public int extraTime;
        public int part;
        public String partDescription;
        public int substitutionInPlayerId;
    }
}
