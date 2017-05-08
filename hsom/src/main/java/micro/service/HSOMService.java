package micro.service;

import io.vertx.core.Future;
import micro.entity.HSOM;
import micro.entity.HSOMNode;

import java.util.List;
import java.util.Optional;

public interface HSOMService {

    //Helper method
    Future<Boolean> populate();

    // GET /hsoms
    Future<List<HSOM>> getAll();

    //POST /hsoms
    Future<HSOM> addHSOM(HSOM hsom);

    //DELETE /hsoms/:id
    Future<Boolean> deleteHSOM(String id);

    //GET /hsoms/:id
    Future<Optional<HSOM>> getHSOM(String id);

    //GET /hsoms/:id/nodes/:nodeid
    Future<Optional<HSOMNode>> getNode(String hsomId, String nodeId);

    //POST /hsoms/:id/nodes
    Future<HSOMNode> addNode(String hsomId, HSOMNode node);

    //DELETE /hsoms/:id/nodes/:nodeid
    Future<Boolean> deleteNode(String hsomId, String nodeId);

    //POST /hsoms/:id/edges
    Future<Boolean> addEdge(String hsomId, String source, String target);

}
