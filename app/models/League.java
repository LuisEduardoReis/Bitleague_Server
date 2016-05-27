package models;

import controllers.draft.DraftManagerActor;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.MongoId;

import java.util.*;
import java.util.Collections;

public class League extends Model {

    public static final int NUM_USERS = 8;
    public static final int SNAKE_ORDER[] = {0,1,2,3,4,5,6,7,7,6,5,4,3,2,1,0};

    public enum State {INVITE, DRAFTING, DURATION}

    public static MongoCollection leagues() {
        return jongo.getCollection("leagues");
    }

    @MongoId
    public ObjectId id;

    public String name;

    public String creator;
    public Map<String, Boolean> users;
    public Map<String, UserTeam> teams;

    public int matchday_counter;
    public ArrayList<ArrayList<Match>> matches;

    public State state;

    public League() {
        state = State.INVITE;
        users = new HashMap<>();
        teams = new HashMap<>();


        matchday_counter = 0;
        matches = new ArrayList<>();
    }

    public League insert() { leagues().save(this); return this; }

    public void remove() {
        leagues().remove(this.id);
    }

    public static League findById(String id) {try {return leagues().findOne(new ObjectId(id)).as(League.class);} catch(IllegalArgumentException exp) {return null;}}
    public static League findByName(String name) {
        return leagues().findOne("{name: #}", name).as(League.class);
    }

    public boolean readyForDraft() {
        return (users.size() % 2) == 0;
        //return users.size() == NUM_USERS;
    }

    public void generateTeams(List<DraftManagerActor.Pick> picks) {
        for(DraftManagerActor.Pick pick : picks) {
            if(!teams.containsKey(pick.user_id)) {
                teams.put(pick.user_id, new UserTeam());
            }
            teams.get(pick.user_id).players.put(pick.player_id,true);
        }
    }

    public void startDuration() {
        state = League.State.DURATION;
        matchday_counter = 0;

        // Round robin tournament
        ArrayList<String> ul = new ArrayList<>();
        for(String user : users.keySet()) ul.add(user);
        Collections.shuffle(ul);

        int numMatchDays = ul.size()-1;
        int numPairs = ul.size()/2;

        matches.clear();
        for(int i = 0; i < 2*numMatchDays; i++) {
            ArrayList<Match> matchday = new ArrayList<>();
            if (i < numMatchDays)
                matchday.add(new Match(ul.get(0),ul.get(i % numMatchDays +1)));
            else
                matchday.add(new Match(ul.get(i % numMatchDays +1),ul.get(0)));

            for(int j = 1; j < numPairs; j++) {
                int first = (i + j) % numMatchDays + 1;
                int second = (i + numMatchDays - j) % numMatchDays + 1;

                if (i < numMatchDays)
                    matchday.add(new Match(ul.get(first),ul.get(second)));
                else
                    matchday.add(new Match(ul.get(second),ul.get(first)));
            }
            matches.add(matchday);
        }
        Collections.shuffle(matches);
    }

    public static class Match {
        public String homePlayer;
        public String awayPlayer;

        public int result;
        public float homePoints;
        public float awayPoints;

        public Match() {}

        public Match(String h, String a) {
            homePlayer = h;
            awayPlayer = a;
        }
    }
}
