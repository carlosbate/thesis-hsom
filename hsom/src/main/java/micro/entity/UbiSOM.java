package micro.entity;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class UbiSOM {

    private String id;
    private String in;
    private String out;
    private String name;
    private List<String> weightLabels;
    private int width;
    private int height;
    private int dim;
    private double alphaI;
    private double alphaF;
    private double sigmaI;
    private double sigmaF;
    private double betaValue;
    private JsonObject normalization;

    public UbiSOM(JsonObject ubisom){
        if(ubisom.containsKey("id")){
            this.id = ubisom.getString("id");
            this.in = ubisom.getString("in");
            this.out = ubisom.getString("out");
        }
        this.name = ubisom.getString("name");
        this.width = ubisom.getInteger("width");
        this.height = ubisom.getInteger("height");
        this.dim = ubisom.getInteger("dim");
        this.alphaI = ubisom.getDouble("alpha_i");
        this.alphaF = ubisom.getDouble("alpha_f");
        this.sigmaI = ubisom.getDouble("sigma_i");
        this.sigmaF = ubisom.getDouble("sigma_f");
        this.betaValue = ubisom.getDouble("beta_value");
        this.normalization = ubisom.getJsonObject("normalization");
        JsonArray wLabels = ubisom.getJsonArray("weight-labels");
        weightLabels = new ArrayList<>(wLabels.size());
        wLabels.stream().forEach(e -> weightLabels.add((String)e));
    }

    public String getId() {
        return id;
    }

    public String getInputChannel() {
        return in;
    }

    public String getOutputChannel() {
        return out;
    }

    public String getName() {
        return name;
    }

    public List<String> getWeightLabels() {
        return weightLabels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDim() {
        return dim;
    }

    public double getAlphaI() {
        return alphaI;
    }

    public double getAlphaF() {
        return alphaF;
    }

    public double getSigmaI() {
        return sigmaI;
    }

    public double getSigmaF() {
        return sigmaF;
    }

    public double getBetaValue() {
        return betaValue;
    }

    private JsonArray convertLabels(){
        JsonArray res = new JsonArray();
        weightLabels.stream().forEach(res::add);
        return res;
    }

    public JsonObject toJson(){
        JsonObject res = new JsonObject();
        if(id != null){
            res.put("id",this.id);
            res.put("in", this.in);
            res.put("out", this.out);
        }
        res.put("name",this.name)
            .put("weight-labels", convertLabels())
            .put("width", this.width)
            .put("height",this.height)
            .put("dim", this.dim)
            .put("alpha_i", this.alphaI)
            .put("alpha_f", this.alphaF)
            .put("sigma_i", this.sigmaI)
            .put("sigma_f", this.sigmaF)
            .put("beta_value", this.betaValue)
            .put("normalization", this.normalization);
        return res;
    }

}
