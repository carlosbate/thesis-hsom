package micro.ubifactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.List;

public interface UbiFactoryRPCService {

    static UbiFactoryRPCService createProxy(Vertx vertx, String address){
        return (UbiFactoryRPCService) ProxyHelper.createProxy(UbiFactoryRPCService.class, vertx, address);
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

    void close();

}
