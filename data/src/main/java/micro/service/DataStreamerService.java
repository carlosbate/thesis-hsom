package micro.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import micro.entity.DataStreamer;

import java.util.List;

@ProxyGen
public interface DataStreamerService {

    static DataStreamerService create(Vertx vertx, JsonObject config){
        return new DataStreamerServiceImpl(vertx, config);
    }

    static DataStreamerService createProxy(Vertx vertx, String address){
        return new DataStreamerServiceVertxEBProxy(vertx, address);
    }

    void getAll(Handler<AsyncResult<List<JsonObject>>> handler);

    void getDataStreamer(String id, Handler<AsyncResult<JsonObject>> handler);

    void createDataStreamer(JsonObject o, Handler<AsyncResult<JsonObject>> handler);

    void deleteDataStreamer(String id, Handler<AsyncResult<Void>> handler);

    @ProxyIgnore
    void close();

}
