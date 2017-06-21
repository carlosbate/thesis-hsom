package micro.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class JsonUtils {

  public static List<String> convertJaToList(JsonArray ja){
      List<String> res = new LinkedList<>();
      ja.stream().forEach(o -> res.add((String)o));
      return res;
  }

  public static JsonArray convertToJA(Collection collection){
      JsonArray res = new JsonArray();
      collection.forEach(res::add);
      return res;
  }

}
