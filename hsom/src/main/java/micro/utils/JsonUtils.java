package micro.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class JsonUtils {
    /*
    public static JsonObject toJson(Object obj){
        Gson gson = new Gson();
        String json = gson.toJson(obj);
        System.out.println();
        return new JsonObject(json);
    }

    public static <T> T fromJson(JsonObject json, Class<T> classOfT){
        Gson gson = new Gson();
        return gson.fromJson(json.toString(),classOfT);
    }
    */

    public static JsonArray convertListToJA(List<String> list){
        JsonArray res = new JsonArray();
        list.stream().forEach(res::add);
        return res;
    }

    public static List<String> convertJaToList(JsonArray ja){
        List<String> res = new LinkedList<>();
        ja.stream().forEach(o -> res.add((String)o));
        return res;
    }

    /*
    public static  JsonObject toJson(Object obj){
        JsonObject res = new JsonObject();
        Arrays.stream(obj.getClass().getMethods())
                .filter(m -> m.getName().startsWith("get"))
                .filter(m -> !m.getName().toLowerCase().contains("class"))
                .forEach(m -> convert(m, obj, res));
        return res;
    }

    private static void convert(Method m, Object o, JsonObject j){
        String varName = m.getName().split("get")[1].toLowerCase();
        try {
            Object r = m.invoke(o);
            j.put(varName, r);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    */

}
