package micro.entity;

import com.google.common.collect.EvictingQueue;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import micro.utils.Tuple;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RingBufferHitSOMCounter extends HitSOMCounter {

    private static final int BMU_LIMIT = 5;
    private int [][] counter;
    private EvictingQueue<Tuple> hitBuffer;

    public RingBufferHitSOMCounter(int width, int height) {
        super(HitSOMCounterType.RING_BUFFER);
        this.counter = new int[width][height];
        this.hitBuffer =  EvictingQueue.create(3000);
    }

    public RingBufferHitSOMCounter(int width, int height, int size) {
        super(HitSOMCounterType.RING_BUFFER);
        this.counter = new int[width][height];
        this.hitBuffer =  EvictingQueue.create(size);
    }

    @Override
    public void addHitCount(int protoX, int protoY) {
        if(!containsTuple(protoX,protoY)) {
            if (this.hitBuffer.remainingCapacity() == 0)
                decrementCounter(this.hitBuffer.peek().getX(), this.hitBuffer.peek().getY());
            this.hitBuffer.add(new Tuple(protoX, protoY));
        }
        this.counter[protoX][protoY]++;
    }

    @Override
    public int getHitCount(int protoX, int protoY) {
        return this.counter[protoX][protoY];
        /*
        return (int) this.hitBuffer.stream()
            .filter(t -> t.equals(protoX, protoY))
            .count();
            */
    }

    public List<Object> getLastBMUs(){
        return this.hitBuffer.stream().limit(BMU_LIMIT).collect(Collectors.toList());
    }

    @Override
    public List<Object> getLastBMUs(int n) {
        return hitBuffer.stream().limit(n).collect(Collectors.toList());
    }

    private void decrementCounter(int protoX, int protoY){
        if(!(counter[protoX][protoY]==0))
            counter[protoX][protoY]--;
    }

    private boolean containsTuple(int protoX, int protoY){
        return this.hitBuffer.contains(new Tuple(protoX, protoY));
    }

    private JsonArray bufferToJson(){
        JsonArray res = new JsonArray();
        hitBuffer.stream().map(Tuple::toJson).forEach(res::add);
        return res;
    }

    private JsonArray counterToJson(){
        JsonArray res = new JsonArray();
        IntStream.range(0, counter.length)
                .boxed()
                .flatMap(i -> IntStream.range(0, counter[0].length)
                                    .mapToObj(j -> new JsonObject()
                                                        .put("x",i)
                                                        .put("y",j)
                                                        .put("count", counter[i][j])
                                    ))
                .forEach(res::add);
        return res;
    }

    @Override
    public String toString() {
        return this.toJson().encode();
    }

    public JsonObject toJson(){
        return new JsonObject()
                .put("counter", counterToJson())
                .put("buffer", this.bufferToJson());
    }
}
