package micro.entity;

import java.util.List;

public abstract class HitSOMCounter {
    private HitSOMCounterType type;

    public HitSOMCounter(HitSOMCounterType type) {
        this.type = type;
    }

    public abstract void addHitCount(int protoX, int protoY);
    public abstract int getHitCount(int protoX, int protoY);
    public abstract List<Object> getLastBMUs(int n);
}
