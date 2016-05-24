package controllers.draft;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import controllers.DataController;
import models.League;
import models.Player;
import models.Season;
import models.User;
import play.Logger;

import java.util.*;


public class DraftManagerActor extends UntypedActor {

    private float TURN_TIME = 10;
    private static final int PICKS_PER_PLAYER = 18;
    public static Props props = Props.create(DraftManagerActor.class);

    public boolean started;
    public Cancellable cancel;

    public String league_id;
    public ArrayList<String> users;
    public HashMap<String, String> usernames;
    public HashMap<String, ActorRef> userActors;

    public float timer;
    public long lastTick;
    public int turn;
    public String currentUser;
    public List<Pick> picks;
    public HashMap<String, ArrayList<String>> shortLists;
    public HashMap<String, Player> playersLeft;

    public DraftManagerActor() {
        this.started = false;
        this.cancel = null;

        this.league_id = "";
        this.users = new ArrayList<>();
        this.usernames = new HashMap<>();
        this.userActors = new HashMap<>();

        this.timer = TURN_TIME;
        this.lastTick = -1;
        this.turn = 0;
        this.currentUser = "";
        this.picks = new ArrayList<>();
        this.playersLeft = DataController.getPlayersHash();
        this.shortLists = new HashMap<>();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Init) {
            Init init = (Init) message;
            this.league_id = init.league_id;
            League league = League.findById(this.league_id);
            this.TURN_TIME = league.turn_timer;
            this.timer = TURN_TIME;
            for (String user_id : league.users.keySet()) {
                User user = User.findById(user_id);
                users.add(user_id);
                usernames.put(user_id, user.name);
            }
        } else if (message instanceof Start) {
            if (cancel != null) cancel.cancel();
            this.cancel = ((Start) message).cancel;
            this.turn = 0;
            this.currentUser = users.get(0);
        } else if (message instanceof AddUserActor) {
            AddUserActor add = (AddUserActor) message;
            this.userActors.put(add.user_id, add.ref);

            SendUserListUpdate();
            add.ref.tell(new PickList(picks), self());
        } else if (message instanceof RemoveUserActor) {
            RemoveUserActor rem = (RemoveUserActor) message;
            for (HashMap.Entry<String, ActorRef> e : userActors.entrySet()) {
                if (e.getValue() == rem.ref)
                    userActors.remove(e.getKey());
            }
            SendUserListUpdate();
        }else if (message instanceof RemoveFavourite) {
            RemoveFavourite pick = (RemoveFavourite) message;
            RemoveFromShortList(pick);
        } else if (message instanceof FavouritePick) {
            FavouritePick pick = (FavouritePick) message;
            AddToShortList(pick);
        } else if (message instanceof MakePick) {
            MakePick pick = (MakePick) message;
            if (pick.user_id.equals(currentUser)) DoPick(pick.player_id);
        } else if (message instanceof String) {
            String string = (String) message;
            if (string == "tick") {
                long time = System.nanoTime();
                if (lastTick > 0) {
                    float elapsed = (float) ((time - lastTick) / 1e9);
                    timer = Math.max(timer - elapsed, 0);
                    if (timer == 0 || !userActors.containsKey(currentUser)) DoPick(null);

                    // Send update
                    SendUpdate(currentUser, timer);

                    // Finish
                    if (turn >= users.size() * PICKS_PER_PLAYER) {
                        cancel.cancel();
                        cancel = null;
                        SendUpdate("noone", -1);

                        currentUser = null;
                        League league = League.findById(league_id);
                        league.generateTeams(picks);
                        league.startDuration();
                        league.insert();
                    }
                }
                lastTick = time;
            } else
                Logger.info(string);
        }
    }


    private void RemoveFromShortList(RemoveFavourite pick) {
        if (pick.player_id == null) {
            return;
        }
        if(!shortLists.containsKey(pick.user_id))
        {
            shortLists.get(pick.user_id).remove(pick.player_id);
        }


        ActorRef user = userActors.get(pick.user_id);
        user.tell(pick, self());



    }

    private void AddToShortList(FavouritePick pick) {
        if (pick.player_id == null) {
           return;
        }
        ArrayList<String> shortList = new ArrayList<>();
        if(!shortLists.containsKey(pick.user_id))
        {
            shortList.add(pick.player_id);
            shortLists.put(pick.user_id, shortList);
        }
        else
        {
            shortList = shortLists.get(pick.user_id);
            if(shortList.contains(pick.player_id)) {
                return;
            }
            else {
                shortList.add(pick.player_id);
                shortLists.put(pick.player_id, shortList);
            }
        }


        ActorRef user = userActors.get(pick.user_id);
        user.tell(pick, self());



    }
    private void DoPick(String player_id) {
        if (player_id == null || !playersLeft.containsKey(player_id)) {

            if(shortLists.containsKey(currentUser)) {
                if (shortLists.get(currentUser).size() < 1)
                    player_id = "" + playersLeft.get(playersLeft.keySet().iterator().next()).data_id;
                else
                    player_id = shortLists.get(currentUser).remove(0);
            }else
                player_id = "" + playersLeft.get(playersLeft.keySet().iterator().next()).data_id;

        }

        playersLeft.remove(player_id);

        for (Map.Entry<String, ArrayList<String>> entry : shortLists.entrySet())
        {
           entry.getValue().remove(player_id);
        }

        picks.add(new Pick(currentUser, player_id));
        for(ActorRef ref : userActors.values()) {
            ref.tell(new MakePick(currentUser, player_id), self());
        }

        turn++;
        timer = TURN_TIME;
        //currentUser = users.get(League.SNAKE_ORDER[turn%(2*League.NUM_USERS)]);
        int n = users.size();
        currentUser = users.get(turn % n);
    }

    private void SendUserListUpdate() {
        UserListUpdate update = new UserListUpdate(users, usernames, userActors);
        for(ActorRef ref : userActors.values()) {
            ref.tell(update, self());
        }
    }

    private void SendUpdate(String currentUser, float timer) {
        TurnUpdate update = new TurnUpdate(currentUser, Math.round(timer));
        for (ActorRef ref : userActors.values()) {
            ref.tell(update, self());
        }
    }

    public class Pick {
        public String user_id;
        public String player_id;
        public Pick(String user_id, String player_id) {
            this.user_id = user_id;
            this.player_id = player_id;
        }
    }

    // Messages

    public static class Start {
        public final Cancellable cancel;
        public Start(Cancellable cancel) {
            this.cancel = cancel;
        }
    }

    public static class AddUserActor {
        public final String user_id;
        public final ActorRef ref;
        public AddUserActor(String user_id, ActorRef ref) {
            this.user_id = user_id;
            this.ref = ref;
        }
    }

    public static class RemoveUserActor {
        public final ActorRef ref;
        public RemoveUserActor(ActorRef ref) {
            this.ref = ref;
        }
    }

    public static class UserListUpdate {
        public final ArrayList<String> users;
        public final HashMap<String,String> usernames;
        public final HashMap<String,Boolean> online;
        public UserListUpdate(ArrayList<String> users, HashMap<String,String> usernames, HashMap<String,ActorRef> userActors) {
            this.users = users;
            this.usernames = usernames;
            this.online = new HashMap<>();
            for(String user_id : users) {
                if (userActors.containsKey(user_id)) online.put(user_id,true);
            }
        }
    }

    public static class MakePick {
        public final String player_id;
        public final String user_id;
        public MakePick(String user_id, String player_id) {
            this.user_id = user_id;
            this.player_id = player_id;
        }
    }

    public static class FavouritePick extends MakePick {
        public FavouritePick(String user_id, String player_id) {
           super(user_id,player_id);
        }
    }

    public static class RemoveFavourite extends MakePick {
        public RemoveFavourite(String user_id, String player_id) {
            super(user_id,player_id);
        }
    }

    public static class PickList {
        public final List<Pick> picks;
        public PickList(List<Pick> picks) {
            this.picks = picks;
        }
    }

    public static class Init {
        public final String league_id;
        public Init(String league_id) { this.league_id = league_id; }
    }

    public static class TurnUpdate {
        public final String currentUser;
        public final int timeLeft;
        public TurnUpdate(String currentUser, int timeLeft) {
            this.currentUser = currentUser;
            this.timeLeft = timeLeft;
        }
    }
}
