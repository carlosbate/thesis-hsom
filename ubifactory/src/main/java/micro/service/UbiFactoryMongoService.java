package micro.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;
import micro.entity.HUbiSOMNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class UbiFactoryMongoService implements UbiFactoryService{

    private static final String COLLECTION = "ubisomdb";
    private Vertx vertx;
    private final MongoService service;

    public UbiFactoryMongoService(Vertx vertx, JsonObject config){
        this.vertx = vertx;
        this.service = MongoService.createEventBusProxy(this.vertx, config.getString("db.service.address"));
    }

    public Future<List<HUbiSOMNode>> getAll() {
        Future<List<HUbiSOMNode>> result = Future.future();
        JsonObject query = new JsonObject();
        service.find(COLLECTION, query, ar -> {
            if(ar.succeeded()){
                List<HUbiSOMNode> res = new LinkedList<HUbiSOMNode>();
                ar.result().stream().forEach(json -> res.add(new HUbiSOMNode(json)));
                result.complete(res);
            }
            else {
                result.fail(ar.cause());
            }
        });
        return result;
    }

    public Future<Boolean> addUbiSOM(HUbiSOMNode ubisom) {
        Future<Boolean> result = Future.future();
        JsonObject newObj = ubisom.toJson();
        service.save(COLLECTION, newObj, ar -> {
            if(ar.succeeded())
                //TODO deploy ubisom instance
                result.complete(true);
            else
                result.fail(ar.cause());
        });
        return result;
    }

    public Future<Optional<HUbiSOMNode>> getUbiSOM(String id) {
        Future<Optional<HUbiSOMNode>> result = Future.future();
        JsonObject query = new JsonObject().put("id", id);
        service.findOne(COLLECTION, query, null, ar -> {
            if(ar.succeeded())
                if(ar.result() == null)
                    result.complete(Optional.empty());
                else
                    result.complete(Optional.of(new HUbiSOMNode(ar.result())));
            else
                result.fail(ar.cause());
        });
        return result;
    }

    public Future<Boolean> deleteUbiSOM(String id) {
        Future<Boolean> result = Future.future();
        JsonObject query = new JsonObject().put("_id", id);
        service.removeDocument(COLLECTION, query, ar -> {
            if(ar.succeeded())
                result.complete(true);
            else
                result.fail(ar.cause());
        });
        return result;
    }

    @Override
    public Future<Boolean> feedUbiSOM(String id, double[][] data) {
        return null;
    }

}
