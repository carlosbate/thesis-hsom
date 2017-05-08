package micro.entity;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import micro.utils.ZipSet;

public class ZipStreamer extends DataStreamerImpl{

  private ZipSet order;
  long bmillis;

  public ZipStreamer(JsonObject o){
    super(o.getString("id"), DataStreamerType.ZIP);
    this.order = new ZipSet();
    this.bmillis = o.getLong("timer");
    initOrder(o.getJsonArray("concat-order"));
  }

  public ZipSet getOrder(){
    return this.order;
  }

  public long getTimer(){
    return this.bmillis;
  }

  public void initOrder(JsonArray array){
    for(Object o : array)
      order.add((String)o);
  }

  public JsonObject toJson(){
    return this.channel.toJson()
        .put("id", this.id)
        .put("type", this.getType())
        .put("in", this.channel.in())
        .put("out", this.channel.out())
        .put("order", this.order.toJson())
        .put("timer", this.bmillis);
  }

}
