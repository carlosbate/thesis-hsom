package micro.service;

import io.vertx.core.Future;
import micro.ubifactory.UbiFactoryRPCService;
import micro.utils.HSOMManager;
import micro.entity.HSOM;
import micro.entity.HSOMNode;

import java.util.List;
import java.util.Optional;

public class HSOMMapService implements HSOMService{

    private HSOMManager manager;
    private UbiFactoryRPCService ubis;
    public HSOMMapService() {
        this.manager = new HSOMManager();
    }

    @Override
    public Future<Boolean> populate(){

        Future<Boolean> result = Future.future();
        result.complete(true);
        /*
        if(manager.addHSOM(new HSOM("my first hsom")))
            result.complete(true);
        else
            result.fail("Populate addHSOM");
        */
        return result;
    }

    @Override
    public Future<List<HSOM>> getAll() {
        Future<List<HSOM>> result = Future.future();
        if(manager.isEmpty())
            result.fail("Empty");
        else{
            result.complete(manager.getAll());
        }
        return result;
    }

    @Override
    public Future<HSOM> addHSOM(HSOM hsom) {
        Future<HSOM> result = Future.future();
        if(manager.addHSOM(hsom))
            result.complete(hsom);
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
    public Future<Optional<HSOM>> getHSOM(String id) {
        Future<Optional<HSOM>> result = Future.future();
        if(manager.getHSOM(id) == null)
            result.complete(Optional.empty());
        else
            result.complete(Optional.of(manager.getHSOM(id)));
        return result;
    }

    @Override
    public Future<Optional<HSOMNode>> getNode(String hsomId, String nodeId) {
        Future<Optional<HSOMNode>> result = Future.future();
        if(manager.getHSOM(hsomId) == null)
            result.complete(Optional.empty());
        HSOMNode node = manager.getHSOM(hsomId).getNode(nodeId);
        if(node == null)
            result.complete(Optional.empty());
        else
            result.complete(Optional.of(node));
        return result;
    }

    @Override
    public Future<HSOMNode> addNode(String hsomId, HSOMNode node) {
        Future<HSOMNode> result = Future.future();
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
}
