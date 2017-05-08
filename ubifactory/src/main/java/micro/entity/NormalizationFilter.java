package micro.entity;

import io.vertx.core.json.JsonObject;

public abstract class NormalizationFilter {

    private NormalizationType type;

    public NormalizationFilter(NormalizationType type){
        this.type = type;
    }

    public NormalizationType getType(){
        return this.type;
    }

    public abstract double [] normalize(double [] observation);

    public abstract JsonObject toJson();

}
