package micro.utils;

import io.vertx.core.json.JsonObject;
import micro.entity.*;

public class NormalizationFilterFactory {

    public static NormalizationFilter create(NormalizationType type){
        return create(type, new JsonObject());
    }

    public static NormalizationFilter create(NormalizationType type, JsonObject o) {
        NormalizationFilter filter;
        switch (type){
            case NONE:
                filter = new NoneNormalizationFilter();
                break;
            case FEATURE_SCALING:
                filter = new FeatureScalingNormFilter();
                break;
            case BAKER_SCALING:
                filter = new BakerScalingNormalizationFilter(o);
                break;
            default:
                filter = null;
                break;
        }
        return filter;
    }

}
