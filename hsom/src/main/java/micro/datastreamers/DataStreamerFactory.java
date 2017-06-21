package micro.datastreamers;

import io.vertx.core.json.JsonObject;
import micro.utils.JsonUtils;

import java.util.List;

public final class DataStreamerFactory {

  public static <T> T create(JsonObject o){
    DataStreamerType type = DataStreamerType.valueOf(o.getString("type"));
    switch (type){
      case ZIP:
        return (T) new ZipDataStreamer(o);
      case PROXY:
        return (T) new ProxyDataStreamer(o);
      default:
        return null;
    }
  }

  public static JsonObject createZipperPostRequest(List<String> order, long timer){
    JsonObject o = new JsonObject();
    o.put("type", DataStreamerType.ZIP);
    o.put("order", JsonUtils.convertToJA(order));
    o.put("timer", timer);
    return o;
  }

  public static JsonObject createProxyPostRequest(String in, String out){
    JsonObject o = new JsonObject();
    o.put("type", DataStreamerType.PROXY);
    o.put("in", in);
    o.put("out", out);
    return o;
  }

}
