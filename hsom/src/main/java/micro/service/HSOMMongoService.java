package micro.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.mongo.MongoService;
import micro.datastreamers.DataStreamerFactory;
import micro.datastreamers.DataStreamerService;
import micro.entity.HSOM;
import micro.entity.HSOMNode;
import micro.ubifactory.UbiFactoryRPCService;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HSOMMongoService implements HSOMService{

    private Vertx vertx;
    private String COLLECTION;
    MongoService mongo;
    //UbiFactoryRPCService ubis;
    //DataStreamerService streamers;

    public HSOMMongoService(Vertx vertx, String collection, String address) {
        this.vertx = vertx;
        this.COLLECTION = collection;
        mongo = MongoService.createEventBusProxy(this.vertx, address);/*
        ubis = UbiFactoryRPCService.createProxy(vertx, config.getString("ubifactory.address"));
        ubis.getAll(ar -> {
            if(ar.succeeded())
                log.info("Successfully connected with UbiFactory service");
            else
                log.error("Couldn't connected with UbiFactory service, reason: " + ar.cause());
        });

        streamers = DataStreamerService.createProxy(vertx, config.getString("datastreamers.address"));
        streamers.getAll(ar -> {
            if(ar.succeeded())
                log.info("Successfully connected with DataStreamers service");
            else
                log.error("Couldn't connected with DataStreamers service, reason: " + ar.cause());
        });
        */
    }

    private <T> Handler<AsyncResult<T>> resultHandler(Consumer<T> consumer){
        return res -> {
            if(res.succeeded())
                consumer.accept(res.result());
            else
                res.cause();
        };
    }

    @Override
    public Future<Boolean> populate() {
        return null;
    }

    @Override
    public Future<List<HSOM>> getAll() {
        Future<List<HSOM>> res = Future.future();
        mongo.find(COLLECTION, new JsonObject(), ar ->{
            if(ar.succeeded())
                res.complete(ar.result().stream().map(HSOM::new).collect(Collectors.toList()));
            else
                res.fail(ar.cause());
        });
        return res;
    }

    @Override
    public Future<HSOM> addHSOM(HSOM hsom) {
        JsonObject o = hsom.toJson();
        Future<HSOM> res = Future.future();
        mongo.insert(COLLECTION, o, ar ->{
            if(ar.succeeded()){
                mongo.replaceDocuments(COLLECTION, new JsonObject().put("_id", ar.result()), o.put("id", ar.result()), mongoClientUpdateResultAsyncResult -> {
                    if(mongoClientUpdateResultAsyncResult.succeeded()){
                        hsom.setId(ar.result());
                        res.complete(hsom);
                    }
                    else
                        res.fail(mongoClientUpdateResultAsyncResult.cause());
                });
            }
            else
                res.fail(ar.cause());
        });
        return res;
    }

    @Override
    public Future<Boolean> deleteHSOM(String id) {
        Future<Boolean> res = Future.future();
        mongo.removeDocument(COLLECTION, new JsonObject().put("_id", id), ar -> {
           if(ar.succeeded())
               res.complete(true);
           else
               res.fail(ar.cause());
        });
        return res;
    }

    @Override
    public Future<Optional<HSOM>> getHSOM(String id) {
        Future<Optional<HSOM>> res = Future.future();
        mongo.findOne(COLLECTION, new JsonObject().put("_id", id), null, ar -> {
            if(ar.succeeded())
                res.complete(Optional.of(new HSOM(ar.result())));
            else
                res.complete(Optional.empty());
        });
        return res;
    }

    @Override
    public Future<Optional<HSOMNode>> getNode(String hsomId, String nodeId) {
        Future<Optional<HSOMNode>> res = Future.future();
        this.getHSOM(hsomId).setHandler(hsomAr -> {
            if(hsomAr.succeeded()){
                if(hsomAr.result().isPresent())
                    res.complete(Optional.of(hsomAr.result().get().getNode(nodeId)));
                else
                    res.complete(Optional.empty());
            }
            else
                res.fail(hsomAr.cause());
        });
        return res;
    }

    @Override
    public Future<HSOMNode> addNode(String hsomId, HSOMNode node) {
        Future<HSOMNode> res = Future.future();
        this.getHSOM(hsomId).setHandler(hsomAr -> {
            if(hsomAr.succeeded()){
                if(hsomAr.result().isPresent()){
                    HSOM h = hsomAr.result().get();
                    h.add(node);
                    updateHSOM(h, booleanAsyncResult -> {
                        if(booleanAsyncResult.succeeded())
                            res.complete(h.getNode(node.getId()));
                        else
                            res.fail(booleanAsyncResult.cause());
                    });
                }
                else
                    res.fail(hsomAr.cause());
            }
            else
                res.fail(hsomAr.cause());
        });
        return res;
    }

    @Override
    public Future<Boolean> deleteNode(String hsomId, String nodeId) {
        //TODO: RPC request
        /*
            1) Check if @nodeId exists
            2) Delete the corresponding UbiSOM
            3) Delete all the associated DataStreamers
         */
        Future<Boolean> res = Future.future();
        this.getHSOM(hsomId).setHandler(hsomAr -> {
            if(hsomAr.succeeded()){
                if(hsomAr.result().isPresent()){
                    HSOM h = hsomAr.result().get();
                    /*
                        Get the edges
                        Delete node and ubisom
                        Delete edges and DataStreamers
                        Create new edges and DataStreamers
                     */
                    h.remove(nodeId);
                    updateHSOM(h, booleanAsyncResult -> {
                        if(booleanAsyncResult.succeeded())
                            res.complete(true);
                        else
                            res.fail(booleanAsyncResult.cause());
                    });
                    res.complete(true);
                }
                else
                    res.fail(hsomAr.cause());
            }
            else
                res.fail(hsomAr.cause());
        });
        return res;
    }

    @Override
    public Future<Boolean> addEdge(String hsomId, String source, String target) {
        Future<Boolean> res = Future.future();
        this.getHSOM(hsomId).setHandler(hsomAr -> {
            if(hsomAr.succeeded())
                if(hsomAr.result().isPresent()){
                    HSOM h = hsomAr.result().get();
                    h.connect(source, target);
                    updateHSOM(h, booleanAsyncResult -> {
                        if(booleanAsyncResult.succeeded())
                            res.complete(true);
                        else
                            res.fail(booleanAsyncResult.cause());
                    });
                }
                else
                    res.fail(hsomAr.cause());
            else
                res.fail(hsomAr.cause());
        });
        return res;
        /*
        Future<Boolean> res = Future.future();
        this.getHSOM(hsomId).setHandler(hsomAr -> {
            if(hsomAr.succeeded())
                if(hsomAr.result().isPresent()) {
                    HSOM h = hsomAr.result().get();
                    handleHasUbi(source).setHandler(resultHandler(hasSource -> {
                        if (hasSource)
                            handleHasUbi(target).setHandler(resultHandler(hasTarget -> {
                                if (hasTarget)
                                    handleCreateStreamer(new JsonObject()).setHandler(resultHandler(newStreamer -> {
                                        //TODO: change Future<Boolean> to Future<JsonObject>???
                                        h.connect(source, target);
                                        updateHSOM(h, booleanAsyncResult -> {
                                            if(booleanAsyncResult.succeeded())
                                                res.complete(true);
                                            else
                                                res.fail(booleanAsyncResult.cause());
                                        });
                                        res.complete(false);
                                    }));
                                else
                                    res.complete(false);
                            }));
                        else
                            res.complete(false);
                    }));
                }
                else
                    res.complete(false);
        });
        return res;
         */
    }
    /*
    Future<JsonObject> handleCreateStreamer(JsonObject o){
        Future<JsonObject> res = Future.future();
        streamers.createDataStreamer(o, ar -> {
           if(ar.succeeded())
               res.complete(ar.result());
           else
               res.fail(ar.cause());
        });
        return res;
    }

    Future<Boolean> handleHasUbi(String id){
        Future<Boolean> res = Future.future();
        ubis.getUbiSOM(id, ar -> {
            if(ar.succeeded())
                if(ar.result().getString("id").equals(id))
                    res.complete(true);
                else
                    res.complete(false);
            else
                res.fail(ar.cause());
        });
        return res;
    }
    */

    private void updateHSOM(HSOM hsom, Handler<AsyncResult<JsonObject>> handler){
        //TODO: Not sure what to do here...
        JsonObject query = new JsonObject().put("_id", hsom.getId());
        JsonObject update = new JsonObject().put("$set", hsom.toJson());
        mongo.updateCollection(COLLECTION, query, update, ar -> {
           if(ar.succeeded()){
               this.getHSOM(hsom.getId()).setHandler(oar -> {
                   if(oar.succeeded())
                       if(oar.result().isPresent())
                           handler.handle(Future.succeededFuture(oar.result().get().toJson()));
                   else
                       handler.handle(Future.failedFuture(oar.cause()));
               });
           }
           else
               handler.handle(Future.failedFuture(ar.cause()));
        });
    }
}
