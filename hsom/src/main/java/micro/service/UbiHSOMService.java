package micro.service;

import io.vertx.core.Future;
import micro.entity.UbiHSOM;
import micro.entity.UbiHSOMNode;

import java.util.List;
import java.util.Optional;

public interface UbiHSOMService {

    //Helper method
    Future<Boolean> populate();

    // GET /hsoms
    Future<List<UbiHSOM>> getAll();

    //POST /hsoms
    Future<UbiHSOM> addHSOM(UbiHSOM ubiHsom);

    //DELETE /hsoms/:id
    Future<Boolean> deleteHSOM(String id);

    //GET /hsoms/:id
    Future<Optional<UbiHSOM>> getHSOM(String id);

    //GET /hsoms/:id/nodes/:nodeid
    Future<Optional<UbiHSOMNode>> getNode(String hsomId, String nodeId);

    //POST /hsoms/:id/nodes
    Future<UbiHSOMNode> addNode(String hsomId, UbiHSOMNode node);

    //DELETE /hsoms/:id/nodes/:nodeid
    Future<Boolean> deleteNode(String hsomId, String nodeId);

    //POST /hsoms/:id/edges
    Future<Boolean> addEdge(String hsomId, String source, String target);

    Future<Boolean> updateHSOM(UbiHSOM ubiHsom);

}
