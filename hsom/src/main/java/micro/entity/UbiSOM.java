package micro.entity;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class UbiSOM {

    private String id;
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

    public UbiSOM(String id, String name, List<String> weightLabels, int width, int height, int dim, double alphaI, double alphaF, double sigmaI, double sigmaF, double betaValue, JsonObject normalization) {
        this.id = id;
        this.name = name;
        this.weightLabels = weightLabels;
        this.width = width;
        this.height = height;
        this.dim = dim;
        this.alphaI = alphaI;
        this.alphaF = alphaF;
        this.sigmaI = sigmaI;
        this.sigmaF = sigmaF;
        this.betaValue = betaValue;
        this.normalization = normalization;
    }

    public UbiSOM(JsonObject ubisom){
        this.id = ubisom.getString("id");
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

    public static JsonObject createUbiSOM(String id, String name, List<String> weightLabels, int width, int height, int dim, double alphaI, double alphaF, double sigmaI, double sigmaF, double betaValue, JsonObject normalization){
        return new JsonObject()
            .put("id",id)
            .put("name",name)
            .put("weight-labels", weightLabels)
            .put("width", width)
            .put("height",height)
            .put("dim", dim)
            .put("alpha_i", alphaI)
            .put("alpha_f", alphaF)
            .put("sigma_i", sigmaI)
            .put("sigma_f", sigmaF)
            .put("beta_value", betaValue)
            .put("normalization", normalization);
    }

    private JsonArray convertLabels(){
        JsonArray res = new JsonArray();
        weightLabels.stream().forEach(res::add);
        return res;
    }

    public JsonObject toJson(){
        return new JsonObject()
                .put("id",this.id)
                .put("name",this.name)
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
    }

}
