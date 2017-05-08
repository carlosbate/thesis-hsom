package micro;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.ext.mongo.MongoService;
import io.vertx.serviceproxy.ProxyHelper;
import micro.service.DataStreamerService;
import micro.verticles.DataStreamerFrontendVerticle;

public class MainDeployer extends AbstractVerticle {

    DataStreamerService service;

    @Override
    public void start() throws Exception {
        super.start();

        service = DataStreamerService.create(vertx, config().getJsonObject("persistence.service.config"));
        ProxyHelper.registerService(DataStreamerService.class, vertx, service, config().getJsonObject("persistence.service.config").getString("persistence.service.address"));

        vertx.deployVerticle(new DataStreamerFrontendVerticle(service), new DeploymentOptions().setConfig(config()));
    }
}
