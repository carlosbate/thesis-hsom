package micro.entity;

import io.vertx.core.json.JsonObject;

public class DataStreamChannel{

    private String inputUrl;
    private String outputUrl;

    public DataStreamChannel(String baseUrl){
        this.inputUrl = baseUrl + "-in";
        this.outputUrl = baseUrl + "-out";
    }

    public String in() {
        return inputUrl;
    }

    public String out() {
        return outputUrl;
    }

    public void setOutputUrl(String newOutputUrl){
        this.outputUrl = newOutputUrl;
    }

    public void setInputUrl(String inputUrl){
        this.inputUrl = inputUrl;
    }

    public JsonObject toJson(){
        return new JsonObject()
                .put("in", this.inputUrl)
                .put("out", this.outputUrl);
    }

    @Override
    public String toString() {
        return this.toJson().encode();
    }
}
