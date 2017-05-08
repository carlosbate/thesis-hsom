package micro.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;
import micro.utils.UrlBuilder;
import micro.verticles.HUbiSOMNodeVerticle;

import java.util.List;

public class UbiFactoryRPCServiceImpl implements UbiFactoryRPCService {

    private Vertx vertx;
    private JsonObject config;
    private MongoService mongo;
    private String COLLECTION;

    public UbiFactoryRPCServiceImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        this.COLLECTION = config.getString("db.collection");
        this.mongo = MongoService.createEventBusProxy(this.vertx, config.getString("db.service.address"));
    }

    @Override
    public void getAll(Handler<AsyncResult<List<JsonObject>>> handler) {
        mongo.find(COLLECTION, new JsonObject(), ar -> {
            if(ar.succeeded())
                handler.handle(Future.succeededFuture(ar.result()));
            else
                handler.handle(Future.failedFuture(ar.cause()));
        });
    }

    @Override
    public void addUbiSOM(JsonObject o, Handler<AsyncResult<JsonObject>> handler) {
        mongo.insert(COLLECTION, o, ar-> {
            if(ar.succeeded()){
                o.put("id", ar.result());
                o.put("in", UrlBuilder.createInUrl(ar.result())).put("out", UrlBuilder.createOutUrl(ar.result()));
                mongo.replaceDocuments(COLLECTION, new JsonObject().put("_id", ar.result()), o, mongoClientUpdateResultAsyncResult -> {
                   if(mongoClientUpdateResultAsyncResult.succeeded())
                       vertx.deployVerticle(new HUbiSOMNodeVerticle(UbiFactoryRPCService.create(this.vertx, this.config),o), res -> {
                           if (res.succeeded())
                               handler.handle(Future.succeededFuture(o));
                           else
                               this.deleteUbiSOM(ar.result(), booleanAsyncResult -> handler.handle(Future.failedFuture(res.cause())));
                       });
                   else
                       this.deleteUbiSOM(ar.result(), booleanAsyncResult -> handler.handle(Future.failedFuture(mongoClientUpdateResultAsyncResult.cause())));
                });
            }
            else{
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void getUbiSOM(String id, Handler<AsyncResult<JsonObject>> handler) {
        mongo.findOne(COLLECTION, new JsonObject().put("_id", id), null, ar -> {
           if(ar.succeeded())
               handler.handle(Future.succeededFuture(convertId(ar.result())));
           else
               handler.handle(Future.failedFuture(ar.cause()));
        });
    }

    @Override
    public void deleteUbiSOM(String id, Handler<AsyncResult<Boolean>> handler) {
        JsonObject query = new JsonObject();
        query.put("_id", new JsonObject().put("$oid", id));
        mongo.removeDocument(COLLECTION, query, ar -> {
           if(ar.succeeded())
               handler.handle(Future.succeededFuture(true));
           else
               handler.handle(Future.failedFuture(ar.cause()));
        });
    }

    @Override
    public void undeployUbiSOM(String id, Handler<AsyncResult<Boolean>> handler) {
        getUbiSOM(id, res -> {
            if(res.succeeded()){
                this.vertx.eventBus().send(UrlBuilder.createStopUrl(id), true, ar -> {
                    if(ar.succeeded()){
                        vertx.undeploy((String)ar.result().body(), voidAsyncResult ->
                                handler.handle(Future.succeededFuture(true))
                        );
                    }
                    else
                        handler.handle(Future.failedFuture(res.cause()));
                });
                handler.handle(Future.succeededFuture(true));
            }
            else
                handler.handle(Future.failedFuture(res.cause()));
        });
    }

    @Override
    public void feedUbiSOM(JsonObject o, Handler<AsyncResult<Boolean>> handler) {
        String id = o.getString("id");
        getUbiSOM(id, res -> {
            if(res.succeeded()){
                this.vertx.eventBus().publish(UrlBuilder.createInUrl(id), o);
                handler.handle(Future.succeededFuture(true));
            }
            else
                handler.handle(Future.failedFuture(res.cause()));
        });
    }

    @Override
    public void getUbiSOMData(String id, Handler<AsyncResult<JsonObject>> handler) {
        getUbiSOM(id, res -> {
            if(res.succeeded())
                this.vertx.eventBus().<JsonObject>send(UrlBuilder.createGetDataUrl(id), false, ar -> {
                    if(ar.succeeded())
                        handler.handle(Future.succeededFuture(ar.result().body()));
                    else
                        handler.handle(Future.failedFuture(ar.cause()));
                });
            else
                handler.handle(Future.failedFuture(res.cause()));
        });
    }

    @Override
    public void getHitCount(String id, Handler<AsyncResult<JsonArray>> handler) {
        getUbiSOM(id, res -> {
            if(res.succeeded())
                this.vertx.eventBus().<JsonArray>send(UrlBuilder.createGetDataHitCountUrl(id), false, ar -> {
                    if(ar.succeeded())
                        handler.handle(Future.succeededFuture(ar.result().body()));
                    else
                        handler.handle(Future.failedFuture(ar.cause()));
                });
            else
                handler.handle(Future.failedFuture(res.cause()));
        });
    }

    @Override
    public void getUMat(String id, Handler<AsyncResult<JsonArray>> handler) {
        getUbiSOM(id, res -> {
            if(res.succeeded())
                this.vertx.eventBus().<JsonArray>send(UrlBuilder.createGetDataUMatUrl(id), false, ar -> {
                    if(ar.succeeded())
                        handler.handle(Future.succeededFuture(ar.result().body()));
                    else
                        handler.handle(Future.failedFuture(ar.cause()));
                });
            else
                handler.handle(Future.failedFuture(res.cause()));
        });
    }

    @Override
    public void getWeights(String id, Handler<AsyncResult<JsonArray>> handler) {
        getUbiSOM(id, res -> {
            if(res.succeeded())
                this.vertx.eventBus().<JsonArray>send(UrlBuilder.createGetDataWeightsUrl(id), false, ar -> {
                    if(ar.succeeded())
                        handler.handle(Future.succeededFuture(ar.result().body()));
                    else
                        handler.handle(Future.failedFuture(ar.cause()));
                });
            else
                handler.handle(Future.failedFuture(res.cause()));
        });
    }

    private void replaceJsonKey(JsonObject o, String newKey){
        String value = o.getString("_id");
        o.remove("_id");
        o.put(newKey, value);
    }

    private JsonObject convertId(JsonObject o){
        o.put("_id", o.getJsonObject("_id").getString("$oid"));
        replaceJsonKey(o, "id");
        return o;
    }

    @Override
    public void close() {
        this.mongo.close();
    }
}
