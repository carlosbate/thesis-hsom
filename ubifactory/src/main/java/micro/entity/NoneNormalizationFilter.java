package micro.entity;

import io.vertx.core.json.JsonObject;

public class NoneNormalizationFilter extends NormalizationFilter{

    public NoneNormalizationFilter(){
        super(NormalizationType.NONE);
    }

    @Override
    public double[] normalize(double[] observation) {
        return observation;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject();
    }


}
