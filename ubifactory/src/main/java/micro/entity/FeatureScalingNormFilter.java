package micro.entity;

import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.stream.IntStream;

public class FeatureScalingNormFilter extends NormalizationFilter{

    private double min;
    private double max;

    public FeatureScalingNormFilter(){
        super(NormalizationType.FEATURE_SCALING);
        this.min = -1;
        this.max = -1;
    }

    public FeatureScalingNormFilter(JsonObject o){
        super(NormalizationType.valueOf(o.getString("type")));
        this.min = o.getDouble("min");
        this.max = o.getDouble("max");
    }

    @Override
    public double[] normalize(double [] observation) {
        double observationMin = Arrays.stream(observation).min().getAsDouble();
        double observationMax = Arrays.stream(observation).max().getAsDouble();
        //TODO RETIRAR A AFECTAÇÂO DO MIN E MAX
        this.min = observationMin < this.min ? this.min = observationMin : this.min;
        this.max = observationMax < this.max ? this.max = observationMax : this.max;
        return IntStream.range(0, observation.length)
                .mapToDouble(i -> featureScaling(observation[i]))
                .toArray();
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put("type", this.getType())
                .put("min", this.min)
                .put("max", this.max);
    }

    private double featureScaling(double example){
        return (example - min)/(max - min);
    }

}
