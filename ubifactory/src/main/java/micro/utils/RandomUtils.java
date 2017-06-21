package micro.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class RandomUtils {

    public static JsonObject convertQueryString(String query){
        JsonObject res = new JsonObject();
        String [] queryArray = query.split("&");
        IntStream.range(0, queryArray.length)
                .mapToObj(i -> queryArray[i].split("="))
                .forEach(elem -> res.put(elem[0], elem[1]));
        return res;
    }

    public static JsonArray convertDoubleObjectArrayToJsonArray(Double [] array){
        JsonArray res = new JsonArray();
        Arrays.stream(array).forEach(res::add);
        return res;
    }

    public static JsonArray convertDoubleArrayToJsonArray(double [] array){
        JsonArray res = new JsonArray();
        Arrays.stream(array).forEach(res::add);
        return res;
    }

    public static double [] convertPrototype(JsonArray array){
        return convertJsonArrayToDoubleArray(array);
    }

    public static double [][] convertObservations(JsonObject json){
        JsonArray array = json.getJsonArray("data");
        List<Double[]> res = new ArrayList<>(array.size());
        array.stream().forEach(o -> res.add(convertJsonArrayToDoubleObjectArray((JsonArray) o)));
        return convertToDoubleMatrix(res);
    }

    private static double [] convertJsonArrayToDoubleArray(JsonArray old){
        double [] res = new double[old.size()];
        IntStream.range(0, old.size())
                .forEach(i -> res[i] = old.getDouble(i));
        return res;
    }

    private static Double [] convertJsonArrayToDoubleObjectArray(JsonArray old){
        Double [] res = new Double[old.size()];
        IntStream.range(0, old.size())
                .forEach(i -> res[i] = old.getDouble(i));
        return res;
    }

    private static double [][] convertToDoubleMatrix(List<Double[]> old){
        double [][] res = new double [old.size()][old.get(0).length];
        for(int i = 0; i < old.size(); i++)
            for(int k = 0; k < old.get(i).length; k++)
                res[i][k] = old.get(i)[k];
        return res;
    }

    public static boolean isIntParsable(String... ids){
        return Arrays.stream(ids)
                .filter(s -> !isValidInt(s))
                .count() == 0;
    }

    private static boolean isValidInt(String s){
        try{
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
