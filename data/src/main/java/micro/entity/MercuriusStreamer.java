package micro.entity;

import io.vertx.core.json.JsonObject;

public class MercuriusStreamer extends DataStreamerImpl{

    public MercuriusStreamer(String id, String outAddress) {
        super(id, DataStreamerType.MERCURIUS);
        this.channel = new DataStreamChannel(id);
        this.channel.setOutputUrl(outAddress);
    }

    public MercuriusStreamer(JsonObject o){
        super(o.getString("id"), DataStreamerType.MERCURIUS);
        this.channel = new DataStreamChannel(this.id);
        this.channel.setOutputUrl(o.getString("out"));
    }

    public String getOutputUrl(){
        return this.channel.out();
    }

    @Override
    public JsonObject toJson() {
        return channel.toJson()
                .put("id", this.id)
                .put("type", this.getType())
                .put("in", this.channel.in())
                .put("out", this.channel.out());
    }
}
