package micro.entity;

import io.vertx.core.json.JsonObject;

/**
 *  This class is the representation of a data stream.
 *  It's defined by an origin and its output, the latter is represented by an URL.
 *  Using the PubSub model, the URL is used to publish incoming data to the event bus topic which is equal to the URL.
 *  Since the origin of the data can be almost anything, this class has to be extended and implemented to meet the data origin requirements and further simulate it creating an event bus proxy between the origin and the underlying system.
 */
public abstract class DataStreamerImpl implements DataStreamer{

    /**
     * The database given Id. This is also the event bus publish address.
     */
    protected String id;

    protected DataStreamerType type;

    protected DataStreamChannel channel;

    public DataStreamerImpl(String id, DataStreamerType type){
        this.id = id;
        this.type = type;
        this.channel = new DataStreamChannel(id);
    }

    public String getId() {
        return id;
    }

    public String getInputUrl(){ return this.channel.in(); }

    public String getOutputUrl(){
        return this.channel.out();
    }

    public void setInputUrl(String newInputUrl) {this.channel.setInputUrl(newInputUrl);}

    public void setOutputUrl(String newOutputUrl) {this.channel.setOutputUrl(newOutputUrl);}

    public DataStreamerType getType(){
        return this.type;
    }

    public abstract JsonObject toJson();

    @Override
    public String toString() {
        return this.toJson().encode();
    }
}
