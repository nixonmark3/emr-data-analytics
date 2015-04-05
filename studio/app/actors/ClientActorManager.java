package actors;

import akka.actor.ActorPath;
import akka.actor.ActorSelection;

import play.libs.Akka;

import java.util.Map;
import java.util.HashMap;

/**
 * ClientActorManager
 */
public class ClientActorManager {
    private static ClientActorManager instance = null;
    public Map<String, ActorPath> clientActorPathMap = new HashMap<String, ActorPath>();

    /**
     * Used to initialize singleton.
     */
    protected ClientActorManager() {}

    /**
     * Returns the singleton instance.
     * @return single instance of manager
     */
    public static ClientActorManager getInstance() {
        if(instance == null) {
            synchronized (ClientActorManager.class) {
                instance = new ClientActorManager();
            }
        }
        return instance;
    }

    /**
     * Adds a Client Actor Path by client id.
     * @param id client actor id
     * @param actorPath client actor path
     */
    public void addClientActorPath(String id, ActorPath actorPath) {
        synchronized(ClientActorManager.class) {
            this.clientActorPathMap.put(id, actorPath);
        }
    }

    /**
     * Remove a Client Actor Path by id.
     * @param id client actor id
     */
    public void removeClientActorPath(String id) {
        synchronized(ClientActorManager.class) {
            this.clientActorPathMap.remove(id);
        }
    }

    /**
     * Return the requested Client Actor.
     * @param id client actor id
     * @return actor selection
     */
   public ActorSelection getClientActor(String id) {
        ActorSelection actor = null;
        synchronized (ClientActorManager.class) {
            if (this.clientActorPathMap.containsKey(id)) {
                ActorPath actorPath = this.clientActorPathMap.get(id);
                if (actorPath != null) {
                    actor = Akka.system().actorSelection(actorPath);
                }
            }
        }
        return actor;
    }

    // todo remove this when client passes down id
    public ActorSelection getActor() {
        ActorSelection actorToReturn = null;
        for (ActorPath actorPath : this.clientActorPathMap.values()) {
            actorToReturn = Akka.system().actorSelection(actorPath);
            break;
        }
        return actorToReturn;
    }
}
