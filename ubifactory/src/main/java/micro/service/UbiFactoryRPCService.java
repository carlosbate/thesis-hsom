package micro.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

@ProxyGen
public interface UbiFactoryRPCService {

    static UbiFactoryRPCService create(Vertx vertx, JsonObject config){
        return new UbiFactoryRPCServiceImpl(vertx, config);
    }

    static UbiFactoryRPCService createProxy(Vertx vertx, String address){
        return new UbiFactoryRPCServiceVertxEBProxy(vertx, address);
    }

    void getAll(Handler<AsyncResult<List<JsonObject>>> handler);

    void addUbiSOM(JsonObject o, Handler<AsyncResult<JsonObject>> handler);

    void getUbiSOM(String id, Handler<AsyncResult<JsonObject>> handler);

    void deleteUbiSOM(String id, Handler<AsyncResult<Boolean>> handler);

    void undeployUbiSOM(String id, Handler<AsyncResult<Boolean>> handler);

    void feedUbiSOM(JsonObject o, Handler<AsyncResult<Boolean>> handler);

    void getUbiSOMData(String id, Handler<AsyncResult<JsonObject>> handler);

    void getHitCount(String id, Handler<AsyncResult<JsonArray>> handler);

    void getUMat(String id, Handler<AsyncResult<JsonArray>> handler);

    void getWeights(String id, Handler<AsyncResult<JsonArray>> handler);

    @ProxyIgnore
    void close();

}
