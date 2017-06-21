package micro.entity;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import somlp.model.nn.ubisom.BestMatchingUnit;

public class HUbiSOMBMU {

    private BestMatchingUnit bmu;
    private HUbiSOMPrototype prototype;

    public HUbiSOMBMU(BestMatchingUnit bmu, HUbiSOMPrototype prototype) {
        this.bmu = bmu;
        this.prototype = prototype;
    }

    public HUbiSOMBMU(JsonObject o){
        this.bmu = new BestMatchingUnit(
                o.getInteger("x"),
                o.getInteger("y")
        );
        this.prototype = new HUbiSOMPrototype(o);
    }

    public JsonArray getBMUCoords(){
        return new JsonArray()
            .add(getCoordX())
            .add(getCoordY());
    }

    public int getCoordX(){
        return this.bmu.x;
    }

    public int getCoordY(){
        return this.bmu.y;
    }

    @Override
    public String toString(){
        return toJson().encode();
    }

    public JsonObject toJson(){
        return new JsonObject()
                .put("x",bmu.x)
                .put("y",bmu.y)
                .put("prototype", prototype.toJson());
    }
}
