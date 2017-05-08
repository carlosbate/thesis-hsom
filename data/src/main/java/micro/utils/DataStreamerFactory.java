package micro.utils;

import io.vertx.core.json.JsonObject;
import micro.entity.*;

public class DataStreamerFactory {

    public static DataStreamer create(JsonObject o){
        DataStreamerType type = DataStreamerType.valueOf(o.getString("type"));
        switch (type){
            case DB:
                return new DBStreamer(o);
            case ECHO:
                return new EchoStreamer(o);
            case MERCURIUS:
                return new MercuriusStreamer(o);
            case PROXY:
                return new ProxyStreamer(o);
            case ZIP:
                return new ZipStreamer(o);
            default:
                return null;
        }
    }

}
