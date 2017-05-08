package micro.entity;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import micro.utils.JsonUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HSOMNode extends AbstractVerticle {

    private String id;
    private Set<String> consumers;
    private Set<String> streamers;
    private UbiSOM ubi;

    public HSOMNode(){
        this.consumers = new HashSet<>();
        this.streamers = new HashSet<>();
    }

    public HSOMNode(String id) {
        this.id = id;
        this.consumers = new HashSet<>();
        this.streamers = new HashSet<>();
    }

    public HSOMNode(String id, UbiSOM ubi) {
        this.id = id;
        this.ubi = ubi;
        this.consumers = new HashSet<>();
        this.streamers = new HashSet<>();
    }

    public HSOMNode(String id, UbiSOM ubi, Set<String> consumers){
        this.id = id;
        this.ubi = ubi;
        this.consumers = consumers;
        this.streamers = new HashSet<>();
    }

    public HSOMNode(JsonObject o){
        this.id = o.getString("id");
        this.ubi = new UbiSOM(o.getJsonObject("model"));
        this.consumers = new HashSet<>();
        this.consumers.addAll(JsonUtils.convertJaToList(o.getJsonArray("consumers")));
        this.streamers = new HashSet<>();
        this.streamers.addAll(JsonUtils.convertJaToList(o.getJsonArray("streamers")));
    }

    @Override
    public void start() throws Exception {
        super.start();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
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
        return ((HSOMNode)obj).getId() == this.getId();
    }

    @Override
    public String toString() {
        return this.toJson().encode();
    }

    public UbiSOM getModel(){
        return this.ubi;
    }

    public JsonObject toJson(){
        JsonObject res = new JsonObject();
        res.put("id", this.id);
        res.put("consumers", new JsonArray(this.consumers.toString()));
        res.put("streamers", new JsonArray(this.streamers.toString()));
        //res.put("model", this.ubi.toJson());
        return res;
    }
}
