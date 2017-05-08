package micro.entity;

import io.vertx.core.json.JsonObject;
import micro.utils.RandomUtils;

public class HUbiSOMPrototype {

    private int x, y;
    private int hitCount;
    private double distance;
    private double [] weights;

    public HUbiSOMPrototype(int x, int y, double[] weights, int hitCount, double distance) {
        this.x = x;
        this.y = y;
        this.weights = weights;
        this.hitCount = hitCount;
        this.distance = distance;
    }

    public HUbiSOMPrototype(int x, int y, double[] weights) {
        this.x = x;
        this.y = y;
        this.weights = weights;
        this.hitCount = 0;
        this.distance = 0.0;
    }

    public HUbiSOMPrototype(JsonObject o){
        this.x = o.getInteger("x");
        this.y = o.getInteger("y");
        this.weights = RandomUtils.convertPrototype(o.getJsonArray("weights"));
        this.hitCount = o.getInteger("hit_count");
        this.distance = o.getDouble("distance");
    }

    public HUbiSOMPrototype(Integer x, int y, double[] weights, int hitCount, double distance) {
        this.x = x;
        this.y = y;
        this.weights = weights;
        this.hitCount = hitCount;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return this.toJson().encode();
    }

    public JsonObject toJson(){
        return new JsonObject()
                .put("x", this.x)
                .put("y", this.y)
                .put("hit_count", this.hitCount)
                .put("distance", this.distance)
                .put("weights", RandomUtils.convertDoubleArrayToJsonArray(this.weights));
    }

}
