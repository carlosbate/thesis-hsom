package micro.entity;

import io.vertx.core.json.JsonObject;

public class DBStreamer extends DataStreamerImpl {

    private long spamDelta = 100;

    private String dbName;

    private int resLimit = 5;

    public DBStreamer(JsonObject o){
        super(o.getString("id"), DataStreamerType.valueOf(o.getString("type")));
        this.dbName = o.getString("db");
        this.spamDelta = o.getLong("timer");
        this.resLimit = o.getInteger("randomness");
        this.channel = new DataStreamChannel(this.id);
    }

    public String getDbName(){ return dbName; }

    public long getTimer() {
        return spamDelta;
    }

    public int getRandomness() {
        return resLimit;
    }

    @Override
    public JsonObject toJson(){
        JsonObject o = new JsonObject();
        o.put("id", this.id);
        o.put("type", this.type);
        o.put("db", this.dbName);
        o.put("randomness", this.resLimit);
        o.put("timer", this.spamDelta);
        o.put("out", this.channel.out());
        return o;
    }

}
