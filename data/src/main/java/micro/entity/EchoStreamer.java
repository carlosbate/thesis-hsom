package micro.entity;

import io.vertx.core.json.JsonObject;

public class EchoStreamer extends DataStreamerImpl{

    public EchoStreamer(JsonObject o){
        super(o.getString("id"), DataStreamerType.ECHO);
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
