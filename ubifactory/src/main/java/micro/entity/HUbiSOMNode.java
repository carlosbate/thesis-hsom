package micro.entity;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import micro.utils.NormalizationFilterFactory;
import micro.utils.RandomUtils;
import somlp.model.nn.ubisom.BestMatchingUnit;
import somlp.model.nn.ubisom.UbiSOM3;
import somlp.model.nn.ubisom.UbiSOMFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class HUbiSOMNode {

    private String id;
    private NormalizationFilter normFilter;
    private UbiSOM3 ubisom;
    private HitSOMCounter hitCounter;
    private UMatDistanceCalculator distanceCalculator;
    private String nodeName;
    private List<String> weightsLabels;

    public HUbiSOMNode(String id, NormalizationFilter normFilter) {
        this.id = id;
        this.normFilter = normFilter;
    }

    public HUbiSOMNode(String id){
        new HUbiSOMNode(id, new NoneNormalizationFilter());
    }

    public HUbiSOMNode(JsonObject obj){
        this.id = obj.getString("id");
        this.nodeName = obj.getString("name");
        ubisom = UbiSOMFactory.create(
                obj.getInteger("width"),
                obj.getInteger("height"),
                obj.getInteger("dim"),
                obj.getDouble("alpha_i"),
                obj.getDouble("alpha_f"),
                obj.getDouble("sigma_i"),
                obj.getDouble("sigma_f"),
                obj.getDouble("beta_value"),
                2000
        );
        NormalizationType type = NormalizationType.valueOf(obj.getJsonObject("normalization").getString("type"));
        JsonObject normObj = obj.getJsonObject("normalization");
        this.normFilter = NormalizationFilterFactory.create(type, normObj);
        this.hitCounter = new RingBufferHitSOMCounter(ubisom.getWidth(), ubisom.getHeight());
        this.weightsLabels = new ArrayList<>(ubisom.getDim());
        obj.getJsonArray("weight-labels").stream()
                .forEach(o -> weightsLabels.add((String)o));
        this.distanceCalculator = new UMatEuclideanDistance(ubisom.getMapUnsafe());
    }

    public boolean hasStarted(){
        return ubisom != null;
    }

    public String getId() {
        return id;
    }

    public JsonArray getWeightsLabels(){
        JsonArray res = new JsonArray();
        this.weightsLabels.stream().forEach(res::add);
        return res;
    }

    public JsonArray getData(){
        if(!hasStarted())
            return null;
        JsonArray res = new JsonArray();
        double [] [] [] data = ubisom.getAsDouble();
        IntStream.range(0, data.length)
                .boxed()
                .flatMap(x -> IntStream.range(0, data[0].length)
                                        .mapToObj(y -> new HUbiSOMPrototype(x,y,
                                                data[x][y],
                                                hitCounter.getHitCount(x,y),
                                                distanceCalculator.getDistance(x,y)).toJson()))
                .forEach(res::add);
        return res;
    }

    public JsonArray getHitCount(){
        JsonArray res = new JsonArray();
        double [] [] [] data = ubisom.getAsDouble();
        IntStream.range(0, data.length)
                .forEach(x -> IntStream.range(0, data[0].length)
                                    .forEach(y -> res.add(hitCounter.getHitCount(x,y))));
        return res;
    }

    public JsonArray getUMat(){
        JsonArray res = new JsonArray();
        double [] [] [] data = ubisom.getAsDouble();
        IntStream.range(0, data.length)
                .forEach(x -> IntStream.range(0, data[0].length)
                        .forEach(y -> res.add(distanceCalculator.getDistance(x,y))));
        return res;
    }

    public JsonArray getWeights(){
        JsonArray res = new JsonArray();
        double [] [] [] data = ubisom.getAsDouble();
        IntStream.range(0, data.length)
                .forEach(x -> IntStream.range(0, data[0].length)
                        .forEach(y -> res.add(RandomUtils.convertDoubleArrayToJsonArray(data[x][y]))));
        return res;
    }

    public HUbiSOMBMU feed(double[] observation){
        if(!hasStarted())
            ubisom = UbiSOMFactory.create(
                    20, //width
                    40, //height
                    observation.length, //data dimensionality
                    0.1, //eta_0
                    0.08, //eta_f
                    0.6, //sigma_0
                    0.2, //sigma_f
                    0.7, //beta
                    2000 //T
            );
        double [] normalizedObservation = normFilter.normalize(observation);
        BestMatchingUnit bmu = ubisom.bmu(normalizedObservation);
        double [] bmuPrototype = ubisom.getCodebookVectorFor(normalizedObservation);
        HUbiSOMPrototype prototype = new HUbiSOMPrototype(bmu.x, bmu.y, bmuPrototype);
        ubisom.learn(normalizedObservation);
        hitCounter.addHitCount(bmu.x, bmu.y);
        return new HUbiSOMBMU(bmu, prototype);
    }

    public void shutdown(){
        this.ubisom.shutdown();
    }

    public String toString(){
        if(!hasStarted())
            return null;
        return this.toJson().encodePrettily();
    }

    public JsonObject toJson(){
        if(!hasStarted())
            return null;
        return new JsonObject()
                .put("id", this.id)
                .put("name", this.getName())
                .put("ubisom", this.ubiSOMtoJson())
                .put("normalization", this.normFilter.toJson())
                .put("weight-labels", this.getWeightsLabels());
    }

    public int getWidth(){
        return this.ubisom.getWidth();
    }

    public int getHeight(){
        return this.ubisom.getHeight();
    }

    private String getName(){
        return this.nodeName;
    }

    private JsonObject ubiSOMtoJson(){
        return new JsonObject()
                .put("width", this.ubisom.getWidth())
                .put("height", this.ubisom.getHeight())
                .put("dim", this.ubisom.getDim())
                .put("alpha_i", this.ubisom.ALPHA_0)
                .put("alpha_f", this.ubisom.ALPHA_F)
                .put("beta_value", this.ubisom.BETA_VALUE)
                .put("sigma_i", this.ubisom.SIGMA_0)
                .put("sigma_f", this.ubisom.SIGMA_F)
                .put("numberResets", this.ubisom.getNumberResets())
                .put("current_state", this.ubisom.getStateName())
                .put("prototypes", this.getData());
    }
}
