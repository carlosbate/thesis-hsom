package micro.entity;

import io.vertx.core.json.JsonObject;

public interface DataStreamer {

    public String getId();

    public String getInputUrl();

    public String getOutputUrl();

    public void setInputUrl(String newInputUrl);

    public void setOutputUrl(String newOutputUrl);

    public DataStreamerType getType();

    public JsonObject toJson();

    @Override
    public String toString();

}
