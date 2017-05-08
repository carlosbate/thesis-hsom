package micro.entity;

import io.vertx.core.json.JsonObject;

public class ProxyStreamer extends  DataStreamerImpl {

    public ProxyStreamer(JsonObject o){
        super(o.getString("id"), DataStreamerType.PROXY);
        this.channel = new DataStreamChannel(this.id);
        this.setInputUrl(o.getString("in"));
        this.setOutputUrl(o.getString("out"));
    }

    @Override
    public JsonObject toJson() {
        return this.channel.toJson()
                .put("id", this.id)
                .put("type", this.getType())
                .put("in", this.channel.in())
                .put("out", this.channel.out());
    }
}
