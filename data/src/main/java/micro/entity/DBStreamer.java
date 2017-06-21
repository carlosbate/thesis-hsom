package micro.entity;

import io.vertx.core.json.JsonObject;

public class DBStreamer extends DataStreamerImpl {

    private long spamDelta = 100;

    private String dbName;

    private String selectors;

    private int resLimit = 5;

    private DBPullerType pullingType;

    public DBStreamer(JsonObject o){
        super(o.getString("id"), DataStreamerType.valueOf(o.getString("type")));
        this.dbName = o.getString("db");
        this.selectors = o.getString("selectors");
        this.spamDelta = o.getLong("timer");
        this.pullingType = DBPullerType.valueOf(o.getString("pull-type"));
        if(pullingType.equals(DBPullerType.RANDOM))
            this.resLimit = o.getInteger("randomness");
        this.channel = new DataStreamChannel(this.id);
    }

    public String getDbName(){ return this.dbName; }

    public long getTimer() {
        return this.spamDelta;
    }

    public int getRandomness() {
        return this.resLimit;
    }

    public String getSelectors() {
        return this.selectors;
    }

    public DBPullerType getPullingType() {
        return pullingType;
    }

    @Override
    public JsonObject toJson(){
        JsonObject o = new JsonObject();
        o.put("id", this.id);
        o.put("type", this.type);
        o.put("db", this.dbName);
        o.put("selectors", this.selectors);
        if(pullingType.equals(DBPullerType.RANDOM))
            o.put("randomness", this.resLimit);
        o.put("timer", this.spamDelta);
        o.put("out", this.channel.out());
        o.put("pull-type", this.pullingType);
        return o;
    }

}
