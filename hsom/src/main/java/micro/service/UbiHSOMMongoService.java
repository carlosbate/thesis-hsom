package micro.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;
import micro.entity.UbiHSOM;
import micro.entity.UbiHSOMNode;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UbiHSOMMongoService implements UbiHSOMService {

    private Vertx vertx;
    private String COLLECTION;
    MongoService mongo;
    //UbiFactoryRPCService ubis;
    //DataStreamerService streamers;

    public UbiHSOMMongoService(Vertx vertx, String collection, String address) {
        this.vertx = vertx;
        this.COLLECTION = collection;
        mongo = MongoService.createEventBusProxy(this.vertx, address);
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
    public Future<List<UbiHSOM>> getAll() {
        Future<List<UbiHSOM>> res = Future.future();
        mongo.find(COLLECTION, new JsonObject(), ar ->{
            if(ar.succeeded())
                res.complete(ar.result().stream().map(UbiHSOM::new).collect(Collectors.toList()));
            else
                res.fail(ar.cause());
        });
        return res;
    }

    @Override
    public Future<UbiHSOM> addHSOM(UbiHSOM ubiHsom) {
        JsonObject o = ubiHsom.toJson();
        Future<UbiHSOM> res = Future.future();
        mongo.insert(COLLECTION, o, ar ->{
            if(ar.succeeded()){
                mongo.replaceDocuments(COLLECTION, new JsonObject().put("_id", ar.result()), o.put("id", ar.result()), mongoClientUpdateResultAsyncResult -> {
                    if(mongoClientUpdateResultAsyncResult.succeeded()){
                        ubiHsom.setId(ar.result());
                        res.complete(ubiHsom);
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
    public Future<Optional<UbiHSOM>> getHSOM(String id) {
        Future<Optional<UbiHSOM>> res = Future.future();
        mongo.findOne(COLLECTION, new JsonObject().put("_id", id), null, ar -> {
            if(ar.succeeded())
                res.complete(Optional.of(new UbiHSOM(ar.result())));
            else
                res.complete(Optional.empty());
        });
        return res;
    }

    @Override
    public Future<Optional<UbiHSOMNode>> getNode(String hsomId, String nodeId) {
        Future<Optional<UbiHSOMNode>> res = Future.future();
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
    public Future<UbiHSOMNode> addNode(String hsomId, UbiHSOMNode node) {
        Future<UbiHSOMNode> res = Future.future();
        this.getHSOM(hsomId).setHandler(hsomAr -> {
            if(hsomAr.succeeded()){
                if(hsomAr.result().isPresent()){
                    UbiHSOM h = hsomAr.result().get();
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
                    UbiHSOM h = hsomAr.result().get();
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
            if (hsomAr.succeeded())
                if (hsomAr.result().isPresent()) {
                    UbiHSOM h = hsomAr.result().get();
                    System.out.println();
                    System.out.println();
                    System.out.println("ADD EDGE");
                    System.out.println("source" + source);
                    System.out.println("target" + target);
                    System.out.println(h.connect(source, target));
                    System.out.println();
                    System.out.println();
                    updateHSOM(h, booleanAsyncResult -> {
                        if (booleanAsyncResult.succeeded())
                            res.complete(true);
                        else
                            res.fail(booleanAsyncResult.cause());
                    });
                } else
                    res.fail(hsomAr.cause());
            else
                res.fail(hsomAr.cause());
        });
        return res;
    }

    public void updateHSOM(UbiHSOM ubiHsom, Handler<AsyncResult<JsonObject>> handler){
        JsonObject oid = new JsonObject().put("$oid", ubiHsom.getId());
        JsonObject query = new JsonObject().put("_id", oid);
        JsonObject update = new JsonObject().put("$set", ubiHsom.toJson());
        mongo.updateCollection(COLLECTION, query, update, ar -> {
           if(ar.succeeded()){
               this.getHSOM(ubiHsom.getId()).setHandler(oar -> {
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

    @Override
    public Future<Boolean> updateHSOM(UbiHSOM ubiHsom) {
        Future<Boolean> res = Future.future();
        this.updateHSOM(ubiHsom, ar -> {
            if(ar.succeeded())
                res.complete(true);
            else
                res.fail(ar.cause());
        });
        return res;
    }
}
