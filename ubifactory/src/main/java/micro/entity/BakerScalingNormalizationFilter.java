package micro.entity;

import io.vertx.core.json.JsonObject;

import java.util.Arrays;

public class BakerScalingNormalizationFilter extends NormalizationFilter{

    private float scalingFactor;
    private float multiplicationFactor;

    public BakerScalingNormalizationFilter(float scalingFactor, float multiplicationFactor){
        super(NormalizationType.BAKER_SCALING);
        this.scalingFactor = scalingFactor;
        this.multiplicationFactor = multiplicationFactor;
    }

    public BakerScalingNormalizationFilter(JsonObject o){
        super(NormalizationType.BAKER_SCALING);
        this.scalingFactor = o.getFloat("scaling_factor");
        this.multiplicationFactor = o.getFloat("multiplication_factor");
    }

    @Override
    public double[] normalize(double[] observation) {
        return Arrays.stream(observation)
                .map(this::bakerScaling)
                .toArray();
    }

    private double bakerScaling(double n){
        float negMultFact = -1 * multiplicationFactor;
        if(n < negMultFact)
            return 0.0;
        else if(n > negMultFact && n < 0)
            return Math.log(-1 * n)/(multiplicationFactor*10);
        else if(n > 0 && n < 1)
            return scalingFactor * n;
        else if(n > 1 && n < multiplicationFactor)
            return Math.log(n)/(multiplicationFactor*10) + scalingFactor;
        else if(n > multiplicationFactor)
            return 1.0;
        return 0.0;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put("type", this.getType())
                .put("scaling_factor", this.scalingFactor)
                .put("multiplication_factor", this.multiplicationFactor);
    }
}
