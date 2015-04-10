package actors;

import akka.actor.ActorPath;
import akka.actor.ActorSelection;

import play.libs.Akka;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * ClientActorManager
 */
public class ClientActorManager
{
    private static ClientActorManager _instance = null;
    private Map<UUID, ActorPath> _clientActorPathMap = new HashMap<UUID, ActorPath>();

    /**
     * Used to initialize singleton.
     */
    protected ClientActorManager() {}

    /**
     * Returns the singleton instance.
     * @return single instance of manager
     */
    public static ClientActorManager getInstance()
    {
        if(_instance == null)
        {
            synchronized (ClientActorManager.class)
            {
                _instance = new ClientActorManager();
            }
        }

        return _instance;
    }

    /**
     * Adds a Client Actor Path by client id.
     * @param id client actor id
     * @param actorPath client actor path
     */
    public void addClientActorPath(UUID id, ActorPath actorPath)
    {
        synchronized(ClientActorManager.class)
        {
            this._clientActorPathMap.put(id, actorPath);
        }
    }

    /**
     * Remove a Client Actor Path by id.
     * @param id client actor id
     */
    public void removeClientActorPath(UUID id)
    {
        synchronized(ClientActorManager.class)
        {
            this._clientActorPathMap.remove(id);
        }
    }

    /**
     * Return the requested Client Actor.
     * @param id client actor id
     * @return actor selection
     */
   public ActorSelection getClientActor(UUID id)
   {
        ActorSelection actor = null;

        synchronized (ClientActorManager.class)
        {
            if (this._clientActorPathMap.containsKey(id))
            {
                ActorPath actorPath = this._clientActorPathMap.get(id);

                if (actorPath != null)
                {
                    actor = Akka.system().actorSelection(actorPath);
                }
            }
        }

        return actor;
    }
}
