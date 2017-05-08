package micro.service;

import io.vertx.core.Future;
import micro.entity.HUbiSOMNode;

import java.util.List;
import java.util.Optional;

public interface UbiFactoryService {

    //GET /ubis
    Future<List<HUbiSOMNode>> getAll();

    //POST /ubis
    Future<Boolean> addUbiSOM(HUbiSOMNode ubisom);

    //GET /ubis/:id
    Future<Optional<HUbiSOMNode>> getUbiSOM(String id);

    //DELETE /ubis/:id
    Future<Boolean> deleteUbiSOM(String id);

    //PATCH /ubis/:id
    Future<Boolean> feedUbiSOM(String id, double [][] data);

    //TODO temporary (I think)
    //Future<Optional<JsonArray>> getUbiSOMData(String id);

}
