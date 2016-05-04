package controllers.draft;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import models.User;
import play.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class DraftManagerActor extends UntypedActor {

    public static Props props = Props.create(DraftManagerActor.class);

    public boolean started;
    public Cancellable cancel;

    public String league_id;
    public HashMap<String, UserActor> userActors;

    public int turn;
    public List<Pick> picks;

    public DraftManagerActor() {
        this.started = false;
        this.cancel = null;

        this.league_id = "";
        this.userActors = new HashMap<>();

        this.picks = new ArrayList<>();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Start) {
            if (cancel != null) cancel.cancel();
            this.cancel = ((Start) message).cancel;
        } else
        if (message instanceof AddUserActor) {
            AddUserActor add = (AddUserActor) message;
            UserActor user = new UserActor();
                user.ref = add.ref;
                user.id = add.user_id;
                user.name = User.findById(user.id).name;
            this.userActors.put(user.id, user);

            SendUserListUpdate();
            user.ref.tell(new PickList(picks), self());
        } else
        if (message instanceof RemoveUserActor) {
            RemoveUserActor rem = (RemoveUserActor) message;
            for(HashMap.Entry<String, UserActor> e : userActors.entrySet()) {
                if (e.getValue().ref == rem.ref)
                    userActors.remove(e.getKey());
            }
            SendUserListUpdate();
        } else
        if (message instanceof MakePick) {
            MakePick pick = (MakePick) message;
            picks.add(new Pick(pick.user_id, pick.player_id));

            for(UserActor a : userActors.values()) {
                a.ref.tell(pick, self());
            }
        } else
        if (message instanceof String) {
            Logger.info((String)message);
        }
    }

    private void SendUserListUpdate() {
        UserListUpdate update = new UserListUpdate(userActors.values());
        for(UserActor a : userActors.values()) {
            a.ref.tell(update, self());
        }
    }

    public class UserActor {
        public String id;
        public String name;
        public ActorRef ref;
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
        public final Collection<UserActor> users;
        public UserListUpdate(Collection<UserActor> users) {
            this.users = users;
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

    public static class PickList {
        public final List<Pick> picks;
        public PickList(List<Pick> picks) {
            this.picks = picks;
        }
    }

}
