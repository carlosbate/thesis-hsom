package micro.entity;

import somlp.model.nn.ubisom.Prototype;

public abstract class UMatDistanceCalculator {

    static final int DEFAULT_RADIUS = 4;
    UMatDistanceCalculatorType type;
    Prototype[][] prototypes;

    public UMatDistanceCalculator(UMatDistanceCalculatorType type, Prototype[][] prototypes) {
        this.type = type;
        this.prototypes = prototypes;
    }

    public double getDistance(int protoX, int protoY){
        return this.getDistance(protoX,protoY,this.DEFAULT_RADIUS);
    }

    public abstract double getDistance(int protoX, int protoY, int radius);
}
