package micro.datastreamers;

import io.vertx.core.json.JsonObject;
import micro.utils.JsonUtils;

import java.util.List;

public class ZipDataStreamer {

  private String id;
  private DataStreamerType type;
  private String in;
  private String out;
  private List<String> order;
  private long timer;

  public ZipDataStreamer(JsonObject o){
    this.id = o.getString("id");
    this.in = o.getString("in");
    this.out = o.getString("out");
    this.type = DataStreamerType.valueOf(o.getString("type"));
    this.timer = o.getLong("timer");
    this.order = JsonUtils.convertJaToList(o.getJsonArray("order"));
  }

  public String getId() {
    return id;
  }

  public DataStreamerType getType() {
    return type;
  }

  public String getInputChannel() {
    return in;
  }

  public String getOutputChannel() {
    return out;
  }

  public List<String> getOrder() {
    return order;
  }

  public long getTimer() {
    return timer;
  }

  public JsonObject toJson(){
    return new JsonObject()
        .put("id", this.id)
        .put("in", this.in)
        .put("out", this.out)
        .put("type", this.type)
        .put("timer", this.timer)
        .put("order", JsonUtils.convertToJA(order));
  }

}
