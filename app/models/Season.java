package models;

import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.MongoId;
import play.mvc.LegacyWebSocket;
import play.mvc.WebSocket;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luis on 29/04/2016.
 */
public class Season extends Model {

    public Integer _id;
    public String name;
    public String startDate;
    public String endDate;
    public List<League> leagues = new ArrayList<>();

    public static MongoCollection seasons() { return jongo.getCollection("seasons"); }


    public static class League {
        public Integer _id;
        public String name;
        public List<MatchDay> matchdays = new ArrayList<>();
        public List<Team> teams = new ArrayList<>();
    }

    public static class Team {
        public Integer _id;
        public String name;
        public List<Player> players = new ArrayList<>();
    }

    public static class Player {
        public Integer _id;
        public String name;
        public Integer number;
        public Integer position;
        public String positionDescription;

        public String team;
    }

    public static class MatchDay {
        public Integer _id;
        public String name;
        public Integer number;
        public String date;
        public List<Match> matches = new ArrayList<>();
    }

    public static class Match {
        public Integer _id;
        public Integer homeTeamId;
        public Integer awayTeamId;
        public String dateTime;
    }
}
