package micro.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;
import micro.entity.*;
import micro.utils.DataStreamerFactory;
import micro.utils.UrlBuilder;
import micro.verticles.*;

import java.util.List;
import java.util.stream.Collectors;

public class DataStreamerServiceImpl implements DataStreamerService {

    private Vertx vertx;
    private JsonObject config;
    private MongoService db;
    private static String COLLECTION;

    public DataStreamerServiceImpl(Vertx vertx, JsonObject config){
        this.vertx = vertx;
        this.config = config;
        this.db = MongoService.createEventBusProxy(this.vertx, config.getString("db.service.address"));
        this.COLLECTION = config.getString("db.service.collection");
    }

    @Override
    public void getAll(Handler<AsyncResult<List<JsonObject>>> handler) {
        JsonObject all = new JsonObject();
        db.find(COLLECTION, all, ar -> {
            if(ar.succeeded())
                handler.handle(Future.succeededFuture(ar.result().stream().map(this::convertId).collect(Collectors.toList())));
            else
                handler.handle(Future.failedFuture(ar.cause()));
        });
    }

    @Override
    public void getDataStreamer(String id, Handler<AsyncResult<JsonObject>> handler) {
        JsonObject query = new JsonObject();
        query.put("_id", id);
        db.findOne(COLLECTION, query, null, ar -> {
            if(ar.succeeded()){
                if(ar.result() != null)
                    handler.handle(Future.succeededFuture(convertId(ar.result())));
                else
                    handler.handle(Future.failedFuture("null value"));
            }
            else
                handler.handle(Future.failedFuture(ar.cause()));
        });
    }

    @Override
    public void createDataStreamer(JsonObject o, Handler<AsyncResult<JsonObject>> handler) {
        db.insert(COLLECTION, o, ar -> {
            if(ar.succeeded()) {
                o.put("id", ar.result());
                db.replaceDocuments(COLLECTION, new JsonObject().put("_id", ar.result()), DataStreamerFactory.create(o).toJson(), mongoClientUpdateResultAsyncResult -> {
                    if(mongoClientUpdateResultAsyncResult.succeeded())
                        deployStreamer(DataStreamerFactory.create(o)).setHandler(stringAsyncResult -> {
                            if(stringAsyncResult.succeeded())
                                handler.handle(Future.succeededFuture(DataStreamerFactory.create(o).toJson()));
                            else
                                deleteDataStreamer(ar.result(), voidAsyncResult -> handler.handle(Future.failedFuture(mongoClientUpdateResultAsyncResult.cause())));
                        });
                    else
                        deleteDataStreamer(ar.result(), voidAsyncResult -> handler.handle(Future.failedFuture(mongoClientUpdateResultAsyncResult.cause())));
                });
            }
           else
               handler.handle(Future.failedFuture(ar.cause()));
        });
    }

    @Override
    public void deleteDataStreamer(String id, Handler<AsyncResult<Void>> handler) {
        JsonObject query = new JsonObject();
        query.put("_id", new JsonObject().put("$oid", id));
        db.removeDocument(COLLECTION, query, ar -> {
            if(ar.succeeded())
                handler.handle(Future.succeededFuture());
            else
                handler.handle(Future.failedFuture(ar.cause()));
        });
    }

    @Override
    public void close(){
        this.db.close();
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

    private Future<String> deployStreamer(DataStreamer m){
        Future<String> res = Future.future();
        DataStreamerType type = m.getType();
        switch (type){
            case DB:
                vertx.deployVerticle(new DBStreamerVerticle(DataStreamerService.create(this.vertx, this.config), (DBStreamer) m), ar -> {
                    if(ar.succeeded())
                        res.complete(ar.result());
                    else
                        res.fail(ar.cause());
                });
                break;
            case MERCURIUS:
                vertx.deployVerticle(new MercuriusStreamerVerticle(DataStreamerService.create(this.vertx, this.config), (MercuriusStreamer) m), ar -> {
                    if(ar.succeeded())
                        res.complete(ar.result());
                    else
                        res.fail(ar.cause());
                });
                break;
            case ECHO:
                vertx.deployVerticle(new EchoStreamerVerticle(DataStreamerService.create(this.vertx, this.config), (EchoStreamer) m), ar -> {
                    if(ar.succeeded())
                        res.complete(ar.result());
                    else
                        res.fail(ar.cause());
                });
                break;
            case PROXY:
                vertx.deployVerticle(new ProxyStreamerVerticle(DataStreamerService.create(this.vertx, this.config), (ProxyStreamer) m), ar -> {
                    if(ar.succeeded())
                        res.complete(ar.result());
                    else
                        res.fail(ar.cause());
                });
                break;
            case ZIP:
                vertx.deployVerticle(new ZipStreamerVerticle(DataStreamerService.create(this.vertx, this.config), (ZipStreamer) m), ar -> {
                    if(ar.succeeded())
                        res.complete(ar.result());
                    else
                        res.fail(ar.cause());
                });
                break;
        }
        return res;
    }

}
