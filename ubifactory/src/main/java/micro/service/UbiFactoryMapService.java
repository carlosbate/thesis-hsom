package micro.service;

import io.vertx.core.Future;
import micro.entity.HUbiSOMNode;
import micro.utils.UbiFactoryManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UbiFactoryMapService implements UbiFactoryService{

    private UbiFactoryManager manager;

    public UbiFactoryMapService(){ this.manager = new UbiFactoryManager(); };

    public Future<List<HUbiSOMNode>> getAll() {
        Future<List<HUbiSOMNode>> result = Future.future();
        if(manager.isEmpty())
            result.fail("Empty");
        else
            result.complete(manager.getAll());
        return result;
    }

    public Future<Boolean> addUbiSOM(HUbiSOMNode ubisom) {
        Future<Boolean> result = Future.future();
        if(manager.addUbiSOM())
            result.complete(true);
        else
            result.fail("Faill addUbiSOM");
        return result;
    }

    public Future<Optional<HUbiSOMNode>> getUbiSOM(String id) {
        Future<Optional<HUbiSOMNode>> result = Future.future();
        if(manager.getUbiSom(id) == null || !manager.getUbiSom(id).hasStarted())
            result.complete(Optional.empty());
        else
            result.complete(Optional.of(manager.getUbiSom(id)));
        return result;
    }

    public Future<Boolean> deleteUbiSOM(String id) {
        Future<Boolean> result = Future.future();
        if(manager.deleteUbiSom(id))
            result.complete(true);
        else
            result.fail("Fail deleteUbiSOM");
        return result;
    }

    public Future<Boolean> feedUbiSOM(String id, double[][] data) {
        Future<Boolean> result = Future.future();
        if(manager.getUbiSom(id) == null)
            result.fail("404 feedUbiSOM");
        else{
            HUbiSOMNode ubisom = manager.getUbiSom(id);
            System.out.println("\n About to feed : " + data.length * data[0].length + "\n");
            System.out.println("\n Data size: " + data.length + "\n");
            for(double [] observation : data) {
                System.out.println("Fed: " + Arrays.toString(observation));
                ubisom.feed(observation);
            }
            System.out.println("\n YUMMI \n");
            result.complete(true);
        }
        return result;
    }
    /*
    public Future<Optional<JsonArray>> getUbiSOMData(int id){
        Future<Optional<JsonArray>> result = Future.future();
        if(manager.getUbiSom(id) == null || !manager.getUbiSom(id).hasStarted())
            result.complete(Optional.empty());
        else
            result.complete(Optional.of(manager.getUbiSom(id).getData()));
        return result;
    }
    */

}
