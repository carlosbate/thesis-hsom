package micro.service;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import micro.datastreamers.DataStreamerFactory;
import micro.datastreamers.DataStreamerService;
import micro.datastreamers.ProxyDataStreamer;
import micro.datastreamers.ZipDataStreamer;
import micro.entity.UbiHSOM;
import micro.entity.UbiHSOMNode;
import micro.entity.UbiSOM;
import micro.ubifactory.UbiFactoryRPCService;

import java.util.List;
import java.util.Optional;

public class UbiUbiHSOMCoreService implements UbiHSOMService {

  private Vertx vertx;
  private UbiFactoryRPCService ubis;
  private DataStreamerService streamers;
  private UbiHSOMService db;

  public UbiUbiHSOMCoreService(Vertx vertx, JsonObject config, Logger log) {
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

    db = new UbiHSOMMongoService(vertx, config.getString("db.collection"), config.getString("db.address"));
  }

  @Override
  public Future<Boolean> populate() {
    return null;
  }

  @Override
  public Future<List<UbiHSOM>> getAll() {
    Future<List<UbiHSOM>> res = Future.future();

    db.getAll().setHandler(ar -> {
      if(ar.succeeded())
        res.complete(ar.result());
      else
        res.fail(ar.cause());
    });

    return res;
  }

  @Override
  public Future<UbiHSOM> addHSOM(UbiHSOM ubiHsom) {
    Future<UbiHSOM> res = Future.future();
    db.addHSOM(ubiHsom).setHandler(hsomAr -> {
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
        Delete UbiHSOM -> Delete X & Delete Y.

        1) Check if the UbiHSOM exists
        2) Delete X
        3) Delete Y
        4) Delete UbiHSOM @id
     */
    return null;
  }

  @Override
  public Future<Optional<UbiHSOM>> getHSOM(String id) {
    Future<Optional<UbiHSOM>> res = db.getHSOM(id);
    /*
    Future<Optional<UbiHSOM>> res = Future.future();
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
    */
    return res;
  }

  @Override
  public Future<Optional<UbiHSOMNode>> getNode(String hsomId, String nodeId) {
    Future<Optional<UbiHSOMNode>> res = Future.future();

    Future<Optional<UbiHSOMNode>> hsomFut = db.getNode(hsomId, nodeId);

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

  /**
    The algorithm bellow depicts the @addNode method:
      1) Create UbiSOM
      2) Create ZipDataStreamer
      3) Create ProxyDataStreamer
      4) Update node:
        4.1) Update node ID with the ID from step 1)
        4.2) Update node streamers with streamers from step 3) and 4)
      5) Save node
      6) Update UbiHSOM
  */
  @Override
  public Future<UbiHSOMNode> addNode(String hsomId, UbiHSOMNode node) {
    Future<UbiHSOMNode> res = Future.future();
    Future<JsonObject> ubiFut = Future.future();
    ubis.addUbiSOM(node.getModel().toJson(), ubiAr -> {
      if(ubiAr.succeeded())
        ubiFut.complete(ubiAr.result());
      else
        ubiFut.fail(ubiAr.cause());
    });

    Future<JsonObject> zipDsFut = Future.future();
    List<String> order = node.getOrder();
    long timer = node.getTimer();
    JsonObject zipDsRequest = DataStreamerFactory.createZipperPostRequest(order,timer);
    streamers.createDataStreamer(zipDsRequest, dsAr -> {
      if(dsAr.succeeded()){
        zipDsFut.complete(dsAr.result());
      }
      else
        zipDsFut.fail(dsAr.cause());
    });

    Future<UbiHSOM> hsomFut = Future.future();
    db.getHSOM(hsomId).setHandler(hsomAr -> {
      if(hsomAr.succeeded()){
        if(hsomAr.result().isPresent())
          hsomFut.complete(hsomAr.result().get());
        else
          hsomFut.fail("Wrong UbiHSOM identifier");
      }
      else
        hsomFut.fail(hsomAr.cause());
    });

    CompositeFuture.join(ubiFut, zipDsFut, hsomFut).setHandler( ar -> {
      if(ar.succeeded()){
        UbiSOM newUbi = new UbiSOM(ubiFut.result());
        ZipDataStreamer newZip = new ZipDataStreamer(zipDsFut.result());
        JsonObject proxyDsRequest = DataStreamerFactory.createProxyPostRequest(newZip.getOutputChannel(), newUbi.getInputChannel());
        streamers.createDataStreamer(proxyDsRequest, dsAr -> {
          if(dsAr.succeeded()){
            ProxyDataStreamer newProxy = new ProxyDataStreamer(dsAr.result());
            UbiHSOM ubiHsom = hsomFut.result();
            UbiHSOMNode newNode = new UbiHSOMNode(newUbi.getId(), newUbi, order, timer, newZip.getId());
            newNode.addStreamer(newZip.getId());
            newNode.addStreamer(newProxy.getId());
            ubiHsom.add(newNode);
            db.addNode(hsomId, newNode).setHandler(newNodeAr -> {
              if(newNodeAr.succeeded()){
                res.complete(newNode);
              }
              else
                res.fail(newNodeAr.cause());
            });
          }
          else
            res.fail(dsAr.cause());
        });
      }
      else
        res.fail(ar.cause());
    });

    return res;
  }

  public Future<JsonObject> addDataSource(JsonObject o){
    //TODO: Similar to the addNode method but this one adds a DataSourceDataStream (which is also a TODO :) )
    /*
      The DBDataStreamer is a type o DataSourceDataStream.
      Ideally one should create an interface for that type of class and implement it however it suits best.
      For example, the DBDataStreamer reads from the database and emulates a live data streamer based on offline data.
     */
    return null;
  }

  @Override
  public Future<Boolean> deleteNode(String hsomId, String nodeId) {
    //TODO: See below
    /*
      1) Get UbiHSOM with @hsomId
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
      5) Update UbiHSOM
    */
    return null;
  }

  //TODO: See below
  /**
    1) Get UbiHSOM with @hsomId
    2) Get node @source output channel
    4) Get node @target associated ZipDataStreamer input channel
    3) Create a ProxyDataStreamer from @source output channel to @target ZipDataStreamer input channel
    4) Update node @target streamers by adding the recently created ProxyDataStreamer to the streamers set
    5) Save node
    6) Update UbiHSOM
  */
  @Override
  public Future<Boolean> addEdge(String hsomId, String sourceId, String targetId) {

    Future<Boolean> res = Future.future();

    Future<UbiHSOM> hsomFut = Future.future();
    db.getHSOM(hsomId).setHandler(hsomFutAr -> {
      if(hsomFutAr.succeeded()){
        if(hsomFutAr.result().isPresent())
          hsomFut.complete(hsomFutAr.result().get());
        else
          hsomFut.fail("Wrong UbiHSOM node identifier");
      }
      else
        hsomFut.fail(hsomFutAr.cause());
    });

    Future<UbiHSOMNode> sourceFut = Future.future();
    db.getNode(hsomId, sourceId).setHandler(sourceFutAr -> {
      if(sourceFutAr.succeeded()){
        if(sourceFutAr.result().isPresent())
          sourceFut.complete(sourceFutAr.result().get());
        else
          sourceFut.fail("Wrong source node identifier");
      }
      else
        sourceFut.fail(sourceFutAr.cause());
    });

    Future<UbiHSOMNode> targetFut = Future.future();
    db.getNode(hsomId, targetId).setHandler(targetFutAr -> {
      if(targetFutAr.succeeded()){
        if(targetFutAr.result().isPresent())
          targetFut.complete(targetFutAr.result().get());
        else
          targetFut.fail("Wrong target node identifier");
      }
      else
        targetFut.fail(targetFutAr.cause());
    });

    CompositeFuture.join(hsomFut, sourceFut, targetFut).setHandler(ar -> {
      if(ar.succeeded()){
        UbiHSOMNode sourceNode = sourceFut.result();
        UbiHSOMNode targetNode = targetFut.result();

        streamers.getDataStreamer(targetNode.getZipperId(), zipperAr -> {
          if(ar.succeeded()){
            ZipDataStreamer targetZipper = new ZipDataStreamer(zipperAr.result());
            JsonObject proxyDsRequest = DataStreamerFactory.createProxyPostRequest(sourceNode.getOutputChannel(), targetZipper.getInputChannel());
            streamers.createDataStreamer(proxyDsRequest, proxyAr -> {
              if(proxyAr.succeeded()){
                UbiHSOM h = hsomFut.result();
                ProxyDataStreamer proxyDs = new ProxyDataStreamer(proxyAr.result());
                h.getNode(sourceId).addConsumer(targetNode.getId());
                h.getNode(targetId).addStreamer(proxyDs.getId());
                h.connect(sourceId, targetId);
                db.updateHSOM(h).setHandler(finalAr -> {
                  if(finalAr.succeeded())
                    res.complete(true);
                  else
                    res.fail(finalAr.cause());
                });
              }
              else
                res.fail(proxyAr.cause());
            });
          }
          else
            res.fail(zipperAr.cause());
        });
      }
      else
        res.fail(ar.cause());
    });

    return res;
  }

  @Override
  public Future<Boolean> updateHSOM(UbiHSOM ubiHsom) {
    return null;
  }
}
