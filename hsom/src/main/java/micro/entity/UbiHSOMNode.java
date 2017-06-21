package micro.entity;

import io.vertx.core.json.JsonObject;
import micro.utils.JsonUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UbiHSOMNode {

    private String id;
    private Set<String> consumers;
    private Set<String> streamers;
    private List<String> order;
    private String zipperId;
    private long timer;
    private UbiSOM ubi;

    //The tests use this init
    public UbiHSOMNode(String id) {
        this.id = id;
        this.consumers = new HashSet<>();
        this.streamers = new HashSet<>();
    }

    public UbiHSOMNode(UbiSOM ubi, List<String> order, long timer){
        this.ubi = ubi;
        this.consumers = new HashSet<>();
        this.streamers = new HashSet<>();
        this.order = order;
        this.timer = timer;
    }

    public UbiHSOMNode(String id, UbiSOM ubi, List<String> order, long timer, String zipperId) {
        this.id = id;
        this.ubi = ubi;
        this.consumers = new HashSet<>();
        this.streamers = new HashSet<>();
        this.order = order;
        this.timer = timer;
        this.zipperId = zipperId;
    }

    public UbiHSOMNode(JsonObject o){
        this.id = o.getString("id");
        this.zipperId = o.getString("zipper");
        this.ubi = new UbiSOM(o.getJsonObject("model"));
        this.consumers = new HashSet<>();
        this.consumers.addAll(JsonUtils.convertJaToList(o.getJsonArray("consumers")));
        this.streamers = new HashSet<>();
        this.streamers.addAll(JsonUtils.convertJaToList(o.getJsonArray("streamers")));
        this.order = JsonUtils.convertJaToList(o.getJsonArray("order"));
        this.timer = o.getLong("timer");
    }

    public String getZipperId() {
        return this.zipperId;
    }

    public String getId(){return this.id;}

    public Set<String> getConsumers(){return this.consumers;}

    public boolean addStreamer(String id){
        if(!streamers.contains(id))
            return streamers.add(id);
        return false;
    }

    public boolean removeStreamer(String id){
        if(consumers.contains(id))
            return consumers.remove(id);
        return false;
    }

    public boolean addConsumer(String id){
        if(!consumers.contains(id))
         return consumers.add(id);
        return false;
    }

    public boolean addConsumers(Collection<String> newConsumers){
        return this.consumers.addAll(newConsumers);
    }

    public boolean removeConsumer(String id){
        if(consumers.contains(id))
            return consumers.remove(id);
        return false;
    }

    public int hashCode(){
        return ((String)this.id).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return ((UbiHSOMNode)obj).getId() == this.getId();
    }

    @Override
    public String toString() {
        return this.toJson().encode();
    }

    public UbiSOM getModel(){
        return this.ubi;
    }

    public List<String> getOrder(){return this.order;}

    public long getTimer(){return this.timer;}

    public String getOutputChannel(){
        return this.getModel().getOutputChannel();
    }

    public String getInputChannel(){
        return this.getModel().getInputChannel();
    }

    public JsonObject toJson(){
        JsonObject res = new JsonObject();
        if(this.id != null){
            res.put("id", this.id);
            res.put("zipper", this.zipperId);
            res.put("model", this.ubi.toJson());
        }
        res.put("consumers", JsonUtils.convertToJA(this.consumers));
        res.put("streamers", JsonUtils.convertToJA(this.streamers));
        res.put("order", this.order);
        res.put("timer", this.timer);
        return res;
    }
}
