package micro;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.serviceproxy.ProxyHelper;
import micro.service.UbiFactoryRPCService;
import micro.verticles.UbiFactoryFrontendVerticle;

public class MainDeployer extends AbstractVerticle {

    UbiFactoryRPCService service;

    @Override
    public void start() throws Exception {
        super.start();

        service = UbiFactoryRPCService.create(vertx, config().getJsonObject("db.config"));
        ProxyHelper.registerService(UbiFactoryRPCService.class, vertx, service, config().getJsonObject("db.config").getString("service.address"));

        vertx.deployVerticle(new UbiFactoryFrontendVerticle(service), new DeploymentOptions().setConfig(config()));
    }
}
