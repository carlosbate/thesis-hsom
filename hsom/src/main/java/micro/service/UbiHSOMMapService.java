package micro.service;

import io.vertx.core.Future;
import micro.ubifactory.UbiFactoryRPCService;
import micro.utils.UbiHSOMManager;
import micro.entity.UbiHSOM;
import micro.entity.UbiHSOMNode;

import java.util.List;
import java.util.Optional;

public class UbiHSOMMapService implements UbiHSOMService {

    private UbiHSOMManager manager;
    private UbiFactoryRPCService ubis;
    public UbiHSOMMapService() {
        this.manager = new UbiHSOMManager();
    }

    @Override
    public Future<Boolean> populate(){

        Future<Boolean> result = Future.future();
        result.complete(true);
        /*
        if(manager.addHSOM(new UbiHSOM("my first hsom")))
            result.complete(true);
        else
            result.fail("Populate addHSOM");
        */
        return result;
    }

    @Override
    public Future<List<UbiHSOM>> getAll() {
        Future<List<UbiHSOM>> result = Future.future();
        if(manager.isEmpty())
            result.fail("Empty");
        else{
            result.complete(manager.getAll());
        }
        return result;
    }

    @Override
    public Future<UbiHSOM> addHSOM(UbiHSOM ubiHsom) {
        Future<UbiHSOM> result = Future.future();
        if(manager.addHSOM(ubiHsom))
            result.complete(ubiHsom);
        else
            result.fail("Fail addHSOM");
        return result;
    }

    @Override
    public Future<Boolean> deleteHSOM(String id) {
        Future<Boolean> result = Future.future();
        if(manager.removeHSOM(id))
            result.complete(true);
        else
            result.fail("Fail removeHSOM");
        return result;
    }

    @Override
    public Future<Optional<UbiHSOM>> getHSOM(String id) {
        Future<Optional<UbiHSOM>> result = Future.future();
        if(manager.getHSOM(id) == null)
            result.complete(Optional.empty());
        else
            result.complete(Optional.of(manager.getHSOM(id)));
        return result;
    }

    @Override
    public Future<Optional<UbiHSOMNode>> getNode(String hsomId, String nodeId) {
        Future<Optional<UbiHSOMNode>> result = Future.future();
        if(manager.getHSOM(hsomId) == null)
            result.complete(Optional.empty());
        UbiHSOMNode node = manager.getHSOM(hsomId).getNode(nodeId);
        if(node == null)
            result.complete(Optional.empty());
        else
            result.complete(Optional.of(node));
        return result;
    }

    @Override
    public Future<UbiHSOMNode> addNode(String hsomId, UbiHSOMNode node) {
        Future<UbiHSOMNode> result = Future.future();
        if(manager.getHSOM(hsomId).getNode(node.getId()) != null)
            result.fail("Node already exists");
        if(manager.getHSOM(hsomId).add(node)){
            result.complete(node);
        }
        else
            result.fail("Fail addNode");
        return result;
    }

    @Override
    public Future<Boolean> deleteNode(String hsomId, String nodeId) {
        Future<Boolean> result = Future.future();
        if(manager.getHSOM(hsomId) == null)
            result.fail("Fail getHSOM");
        if(manager.getHSOM(hsomId).getNode(nodeId) == null)
            result.fail("Fail getNode");
        if(manager.getHSOM(hsomId).remove(nodeId))
            result.complete(true);
        else
            result.fail("Fail remove");
        return result;
    }

    @Override
    public Future<Boolean> addEdge(String hsomId, String source, String target) {
        Future<Boolean> result = Future.future();
        if(manager.getHSOM(hsomId) == null)
            result.fail("Fail getHSOM");
        if(manager.getHSOM(hsomId).getNode(source) == null)
            result.fail("Fail getNode source");
        if(manager.getHSOM(hsomId).getNode(target) == null)
            result.fail("Fail getNode target");
        if(manager.getHSOM(hsomId).connect(source,target))
            result.complete(true);
        else
            result.fail("Fail addEdge");
        return result;
    }

    @Override
    public Future<Boolean> updateHSOM(UbiHSOM ubiHsom) {
        return null;
    }
}
