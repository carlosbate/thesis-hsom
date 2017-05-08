package micro.entity;

import io.vertx.core.json.JsonObject;
import micro.utils.RandomUtils;
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
