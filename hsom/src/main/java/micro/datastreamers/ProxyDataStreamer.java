package micro.datastreamers;

import io.vertx.core.json.JsonObject;

public class ProxyDataStreamer {

  private String id;
  private String in;
  private String out;
  private DataStreamerType type;

  public ProxyDataStreamer(JsonObject o){
    this.id = o.getString("id");
    this.in = o.getString("in");
    this.out = o.getString("out");
    this.type = DataStreamerType.valueOf(o.getString("type"));
  }

  public String getId() {
    return id;
  }

  public String getInputChannel() {
    return in;
  }

  public String getOutputChannel() {
    return out;
  }

  public DataStreamerType getType() {
    return type;
  }

  public JsonObject toJson(){
    return new JsonObject()
        .put("id", this.id)
        .put("in", this.in)
        .put("out", this.out)
        .put("type", this.type);
  }

}
