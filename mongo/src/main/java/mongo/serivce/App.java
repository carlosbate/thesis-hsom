package mongo.serivce;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoService;
import io.vertx.ext.mongo.impl.MongoServiceImpl;
import io.vertx.serviceproxy.ProxyHelper;

public class App extends AbstractVerticle{

    MongoService service;

    @Override
    public void start() throws Exception {
        super.start();

        service = new MongoServiceImpl(MongoClient.createShared(vertx, config()));

        ProxyHelper.registerService(MongoService.class, vertx, service, config().getString("service.address"));
    }
}
