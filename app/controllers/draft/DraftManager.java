package controllers.draft;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import models.User;
import play.Logger;

import java.util.Collection;
import java.util.HashMap;


public class DraftManager extends UntypedActor {

    public static Props props = Props.create(DraftManager.class);

    public boolean started;
    public Cancellable cancel;

    public String league_id;
    public HashMap<String, UserActor> userActors;

    public DraftManager() {
        this.started = false;
        this.cancel = null;

        this.league_id = "";
        this.userActors = new HashMap<>();
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
        } else
        if (message instanceof RemoveUserActor) {
            RemoveUserActor rem = (RemoveUserActor) message;
            for(HashMap.Entry<String, UserActor> e : userActors.entrySet()) {
                if (e.getValue().ref == rem.ref)
                    userActors.remove(e.getKey());
            }
            SendUserListUpdate();
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
}
