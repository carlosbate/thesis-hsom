package micro.service;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import micro.datastreamers.DataStreamerService;
import micro.entity.HSOM;
import micro.entity.HSOMNode;
import micro.ubifactory.UbiFactoryRPCService;

import java.util.List;
import java.util.Optional;

public class HSOMCoreService implements HSOMService{

  private Vertx vertx;
  private UbiFactoryRPCService ubis;
  private DataStreamerService streamers;
  private HSOMService db;

  public HSOMCoreService(Vertx vertx, JsonObject config, Logger log) {
    this.vertx = vertx;
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

    db = new HSOMMongoService(vertx, config.getString("db.collection"), config.getString("db.address"));
  }

  @Override
  public Future<Boolean> populate() {
    return null;
  }

  @Override
  public Future<List<HSOM>> getAll() {
    Future<List<HSOM>> res = Future.future();

    db.getAll().setHandler(ar -> {
      if(ar.succeeded())
        res.complete(ar.result());
      else
        res.fail(ar.cause());
    });

    return res;
  }

  @Override
  public Future<HSOM> addHSOM(HSOM hsom) {
    Future<HSOM> res = Future.future();
    db.addHSOM(hsom).setHandler(hsomAr -> {
      if(hsomAr.succeeded())
        res.complete(hsomAr.result());
      else
        res.fail(hsomAr.cause());
    });
    return res;
  }

  @Override
  public Future<Boolean> deleteHSOM(String id) {
    //TODO: See below
    /*
        Let X be all the associated UbiSOMs
        Let Y be all the associated DataStreamers.
        Delete HSOM -> Delete X & Delete Y.

        1) Check if the HSOM exists
        2) Delete X
        3) Delete Y
        4) Delete HSOM @id
     */
    return null;
  }

  @Override
  public Future<Optional<HSOM>> getHSOM(String id) {
    Future<Optional<HSOM>> res = Future.future();

    db.getHSOM(id).setHandler(ar -> {
      if(ar.succeeded()){
        if(ar.result().isPresent())
          res.complete(ar.result());
        else
          res.complete(Optional.empty());
      }
      else
        res.fail(ar.cause());
    });

    return res;
  }

  @Override
  public Future<Optional<HSOMNode>> getNode(String hsomId, String nodeId) {
    Future<Optional<HSOMNode>> res = Future.future();

    Future<Optional<HSOMNode>> hsomFut = Future.future();
    db.getNode(hsomId, nodeId).setHandler(nodeAr -> {
      if(nodeAr.succeeded())
        if(nodeAr.result().isPresent())
          hsomFut.complete(nodeAr.result());
        else
          hsomFut.complete(Optional.empty());
      else
        hsomFut.fail(nodeAr.cause());
    });

    Future<JsonObject> ubiFut = Future.future();
    ubis.getUbiSOM(nodeId, ubiAr -> {
      if(ubiAr.succeeded())
        ubiFut.complete(ubiAr.result());
      else
        ubiFut.fail(ubiAr.cause());
    });

    CompositeFuture.join(hsomFut, ubiFut).setHandler(ar -> {
      if(ar.succeeded())
        res.complete(hsomFut.result());
      else
        res.fail(ar.cause());
    });

    return res;
  }

  @Override
  public Future<HSOMNode> addNode(String hsomId, HSOMNode node) {
    //TODO: See below
    /*
      1) Create UbiSOM
      2) Create ZipDataStreamer
      3) Create ProxyDataStreamer
      4) Update node:
        4.1) Update node ID with the ID from step 1)
        4.2) Update node streamers with streamers from step 3) and 4)
      5) Save node
      6) Update HSOM
    */
    Future<HSOMNode> res = Future.future();

    Future<JsonObject> ubiFut = Future.future();
    ubis.addUbiSOM(node.toJson(), ubiAr -> {
      if(ubiAr.succeeded())
        ubiFut.complete(ubiAr.result());
      else
        ubiFut.fail(ubiAr.cause());
    });

    Future<Optional<HSOM>> hsomFut = db.getHSOM(hsomId);

    CompositeFuture.join(ubiFut, hsomFut).setHandler( ar -> {
      if(ar.succeeded())
        if(hsomFut.result().isPresent()){
          HSOM hsom = hsomFut.result().get();
          HSOMNode newNode = new HSOMNode(ubiFut.result());
          hsom.add(newNode);
          db.addNode(hsomId, newNode).setHandler(newNodeAr -> {
            if(newNodeAr.succeeded())
              res.complete(newNode);
            else
              res.fail(newNodeAr.cause());
          });
        }
      else
        res.fail(ar.cause());
    });

    return res;
  }

  @Override
  public Future<Boolean> deleteNode(String hsomId, String nodeId) {
    //TODO: See below
    /*
      1) Get HSOM with @hsomId
      2) Get node @nodeId
      4) Get removal type @rem_type
      3) If @rem_type == default //TODO: leave for later, implement cascade first
        3.1) Delete streamers
        3.2) Connect descendants to ascendants through ProxyDataStreamers
        3.3) Delete node @nodeid
      4) If @rem_type == cascade
        4.1) Get dependent ascendant nodes
        4.2) Delete all dependent ascendant nodes
        4.3) Delete streamers
      5) Update HSOM
    */
    return null;
  }

  @Override
  public Future<Boolean> addEdge(String hsomId, String source, String target) {
    //TODO: See below
    /*
      1) Get HSOM with @hsomId
      2) Get node @source output channel
      4) Get node @target associated ZipDataStreamer input channel
      3) Create a ProxyDataStreamer from @source output channel to @target ZipDataStreamer input channel
      4) Update node @target streamers by adding the recently created ProxyDataStreamer to the streamers set
      5) Save node
      6) Update HSOM
    */
    return null;
  }
}
