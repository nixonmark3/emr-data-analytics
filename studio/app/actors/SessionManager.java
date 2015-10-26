package actors;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import emr.analytics.models.messages.OutputMessage;
import play.libs.Json;

import java.util.*;

public class SessionManager {

    private static SessionManager _instance = null;
    private Map<UUID, Session> _sessions = new HashMap<UUID, Session>();
    private Map<UUID, Subscriptions> _diagramSubscriptions = new HashMap<UUID, Subscriptions>();
    private Subscriptions _dashboardSubscriptions = new Subscriptions();

    /**
     * Used to initialize singleton.
     */
    protected SessionManager() { }

    public static SessionManager getInstance() {

        if(_instance == null) {
            synchronized (SessionManager.class) {
                _instance = new SessionManager();
            }
        }
        return _instance;
    }

    /**
     *
     * @param id
     * @param actor
     */
    public void addSession(UUID id, ActorRef actor){

        _sessions.put(id, new Session(id, actor));
    }

    /**
     *
     * @param id
     */
    public void removeSession(UUID id) {

        _sessions.remove(id);
    }

    /**
     *
     * @param message
     * @param excludedId
     */
    public void notifyAll(OutputMessage message, UUID excludedId){

        JsonNode node = Json.toJson(message);
        for(Session session : _sessions.values()){

            if (excludedId != null && session.getId().equals(excludedId))
                continue;

            session.getActor().tell(node, null);
        }
    }

    /**
     *
     * @param message
     */
    public void notifyAll(OutputMessage message) {
        notifyAll(message, null);
    }

    /**
     *
     * @param message
     */
    public void notifyDashboards(OutputMessage message) {

        JsonNode node = Json.toJson(message);

        Subscriptions subscriptions = this._dashboardSubscriptions;
        for(UUID id : subscriptions.get()){

            Session session = this.getSession(id);
            if (session != null)
                session.getActor().tell(node, null);
        }
    }

    /**
     *
     * @param diagramId
     * @param excludedId
     * @param message
     */
    public void notifySubscribers(UUID diagramId, UUID excludedId, OutputMessage message){

        // convert base message into JSON
        JsonNode node = Json.toJson(message);

        // iterate over the diagram's subscriptions
        Subscriptions subscriptions = this.getDiagramSubscriptions(diagramId);
        for(UUID id : subscriptions.get()){

            if (excludedId != null && excludedId == id)
                continue;

            Session session = this.getSession(id);
            if (session != null)
                session.getActor().tell(node, null);
        }

        this.notifyDashboards(message);
    }

    /**
     *
     * @param diagramId
     * @param message
     */
    public void notifySubscribers(UUID diagramId, OutputMessage message){

        notifySubscribers(diagramId, null, message);
    }

    /**
     * Subscribe a session to diagram
     * @param id
     * @param diagramId
     */
    public void subscribe(UUID id, UUID diagramId){

        Session session = this.getSession(id);
        if (session != null){

            // todo: add locking

            // check whether the session is already subscribed to the specified diagram
            if (session.getDiagramSubscription() == diagramId)
                return;

            // clear the session's current subscription
            this.clearSubscriptions(id, session);

            session.setDiagramSubscription(diagramId);
            Subscriptions subscriptions = this.getDiagramSubscriptions(diagramId);
            subscriptions.add(id);
        }
    }

    public void subscribeToDashboard(UUID id){

        Session session = this.getSession(id);
        if (session != null){

            // todo: add locking

            if (session.getDashboardSubscription())
                return;

            // clear the session's current subscription
            this.clearSubscriptions(id, session);

            session.setDashboardSubscription(true);
            _dashboardSubscriptions.add(id);
        }
    }

    private void clearSubscriptions(UUID id, Session session){

        // todo: add locking

        if (session.getDashboardSubscription()){
            // if the session is currently subscribed to dashboard - clear

            _dashboardSubscriptions.remove(id);
            session.setDashboardSubscription(false);
        }
        else if(session.hasDiagramSubscription()){
            // if the session is currently subscribe to a diagram - clear

            UUID diagramId = session.getDiagramSubscription();
            if (_diagramSubscriptions.containsKey(diagramId)){

                Subscriptions subscriptions = _diagramSubscriptions.get(diagramId);
                subscriptions.remove(id);
            }

            session.setDiagramSubscription(null);
        }
    }

    private Subscriptions getDiagramSubscriptions(UUID diagramId){

        // todo: add locking

        Subscriptions subscriptions;
        if (_diagramSubscriptions.containsKey(diagramId)){
            subscriptions = _diagramSubscriptions.get(diagramId);
        }
        else{
            subscriptions = new Subscriptions();
            _diagramSubscriptions.put(diagramId, subscriptions);
        }

        return subscriptions;
    }

    private Session getSession(UUID id){

        Session session = null;
        if (_sessions.containsKey(id)) {

            // retrieve the session
            session = _sessions.get(id);
        }

        return session;
    }

    private class Session {

        private UUID id;
        private ActorRef actor;
        private boolean dashboardSubscription = false;
        private UUID diagramSubscription = null;

        public Session(UUID id, ActorRef actor){
            this.id = id;
            this.actor = actor;
        }

        public UUID getId() { return this.id; }

        public ActorRef getActor(){ return this.actor; }

        public boolean getDashboardSubscription() { return this.dashboardSubscription; }

        public void setDashboardSubscription(boolean value) { this.dashboardSubscription = value; }

        public UUID getDiagramSubscription() { return this.diagramSubscription; }

        public boolean hasDiagramSubscription() { return this.diagramSubscription != null; }

        public void setDiagramSubscription(UUID id) { this.diagramSubscription = id; }
    }

    private class Subscriptions {

        private Set<UUID> set = new HashSet<UUID>();

        public List<UUID> get() { return new ArrayList<UUID>(set); }

        public void add(UUID id) { set.add(id); }

        public boolean contains(UUID id) { return set.contains(id); }

        public void remove(UUID id) { set.remove(id); }
    }
}
