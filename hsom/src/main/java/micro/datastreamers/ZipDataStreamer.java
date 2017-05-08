package micro.datastreamers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import micro.utils.JsonUtils;

import java.util.LinkedList;
import java.util.List;

public class ZipDataStreamer {

  private String id;
  private DataStreamerType type;
  private String in;
  private String out;
  private List<String> order;
  private long timer;

  public ZipDataStreamer(String id, String in, String out, List<String> order, long timer){
    this.id = id;
    this.in = in;
    this.out = out;
    this.order = order;
    this.type = DataStreamerType.ZIP;
    this.timer = timer;
  }

  public ZipDataStreamer(JsonObject o){
    this.id = o.getString("id");
    this.type = DataStreamerType.valueOf(o.getString("type"));
    this.in = o.getString("in");
    this.out = o.getString("out");
    this.timer = o.getLong("timer");
    this.order = JsonUtils.convertJaToList(o.getJsonArray("order"));
  }

  public JsonObject toJson(){
    return new JsonObject()
        .put("id", this.id)
        .put("in", this.in)
        .put("out", this.out)
        .put("type", this.type)
        .put("timer", this.timer)
        .put("order", JsonUtils.convertListToJA(order));
  }

}
