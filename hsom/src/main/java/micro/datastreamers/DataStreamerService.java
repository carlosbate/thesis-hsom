package micro.datastreamers;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.List;

@ProxyGen
public interface DataStreamerService {

    static DataStreamerService createProxy(Vertx vertx, String address){
        return (DataStreamerService) ProxyHelper.createProxy(DataStreamerService.class, vertx, address);
    }

    void getAll(Handler<AsyncResult<List<JsonObject>>> handler);

    void getDataStreamer(String id, Handler<AsyncResult<JsonObject>> handler);

    void createDataStreamer(JsonObject o, Handler<AsyncResult<JsonObject>> handler);

    void deleteDataStreamer(String id, Handler<AsyncResult<Void>> handler);

    @ProxyIgnore
    void close();

}
