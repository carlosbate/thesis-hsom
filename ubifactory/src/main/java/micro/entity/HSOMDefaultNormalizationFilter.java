package micro.entity;

import io.vertx.core.json.JsonObject;

import java.util.stream.IntStream;

public class HSOMDefaultNormalizationFilter extends NormalizationFilter{

  private int maxX;
  private int minX;
  private int maxY;
  private int minY;

  public HSOMDefaultNormalizationFilter(int maxX, int maxY){
    super(NormalizationType.DEFAULT);
    this.maxX = maxX;
    this.minX = 1;
    this.maxY = maxY;
    this.minY = 1;
  }

  @Override
  public double[] normalize(double[] observation) {
    double [] normalized = new double[observation.length];
    IntStream.range(0, observation.length)
        .forEach(i -> calculateNorm(i, observation, normalized));
    return normalized;
  }

  private void calculateNorm(int i, double[] observation, double [] normalized){
    if(isEven(i))
      normalized[i] = normalization(observation[i], maxX, minX);
    else
      normalized[i] = normalization(observation[i], maxY, minY);
  }

  private double normalization(double d, int max, int min){

    return (d - min)/(max - min);
  }

  private boolean isEven(int number){
    return number % 2 == 0;
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject()
        .put("type", this.getType())
        .put("max-x", this.maxX)
        .put("min-x", this.minX)
        .put("max-y", this.maxY)
        .put("min-y", this.minY);
  }

}
