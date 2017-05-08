package micro.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import micro.UbiFactoryRESTAPI;
import micro.service.UbiFactoryRPCService;
import micro.utils.RandomUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class UbiFactoryFrontendVerticle extends AbstractVerticle{

    private static Logger log = LoggerFactory.getLogger(UbiFactoryFrontendVerticle.class);
    private UbiFactoryRPCService service;

    /*
    private void initService(){
        final String serviceType = config().getString("service.type");
        log.info("Service type: " + serviceType);
        switch (serviceType) {
            case "hashmap":
                service = new UbiFactoryMapService();
                break;
            case "jdbc":
                //TODO
                break;
            case "mongodb":
                service = new UbiFactoryMongoService(vertx, config());
                break;
            default:
                service = new UbiFactoryMapService();
                break;
        }
    }
    */

    public UbiFactoryFrontendVerticle(UbiFactoryRPCService service) {
        this.service = service;
    }

    @Override
    public void start() throws Exception {
        super.start();
        //initService();
        final int port = config().getInteger("http.port");

        Router router = Router.router(vertx);

        //CORS related stuff
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);

        router.route().handler(BodyHandler.create());
        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
                .allowedMethods(allowMethods));

        // Routes. The actual API is defined in UbiFactoryRESTAPI.java file.
        router.get(UbiFactoryRESTAPI.GET_ALL).handler(this::getAll);
        router.post(UbiFactoryRESTAPI.ADD_UBI).handler(this::addUbiSOM);
        router.get(UbiFactoryRESTAPI.GET_UBI).handler(this::getUbiSOM);
        router.delete(UbiFactoryRESTAPI.DELETE_UBI).handler(this::deleteUbiSOM);
        router.patch(UbiFactoryRESTAPI.FEED_UBI).handler(this::feedUbiSOM);
        router.get(UbiFactoryRESTAPI.GET_UBI_DATA).handler(this::getUbiSOMData);
        router.get(UbiFactoryRESTAPI.GET_UBI_HITCOUNTS).handler(this::getUbiSOMHitCount);
        router.get(UbiFactoryRESTAPI.GET_UBI_UMAT).handler(this::getUbiSOMUMat);
        router.get(UbiFactoryRESTAPI.GET_UBI_WEIGHTS).handler(this::getUbiSOMWeights);

        vertx.createHttpServer().requestHandler(router::accept).listen(port, ar -> {
            if(ar.succeeded())
                log.info("UbiFactory service deployed on http://localhost:" + port + ".");
            else
                log.info("Failed to deploy UbiFactory service...\n" + ar.cause());
        });

    }

    private void getAll(RoutingContext routingContext){
        service.getAll(res -> {
            log.info("UbiFactory getAll");
            if(res != null){
                final String encoded = res.result().toString();
                routingContext.request().response()
                        .putHeader("content-type", "application/json")
                        .end(encoded);
            }
            else
                serviceUnavailable(routingContext);
        });
    }

    private void addUbiSOM(RoutingContext routingContext){
        JsonObject o = routingContext.getBodyAsJson();
        log.info("UbiFactory addUbiSOM");
        service.addUbiSOM(o, res ->
            routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json")
                .end(res.result().encodePrettily())
        );
    }

    private void getUbiSOM(RoutingContext routingContext){
        String ubiSOMID = routingContext.request().getParam("id");
        service.getUbiSOM(ubiSOMID, res -> {
            final String encoded = res.result().encodePrettily();
            routingContext.response()
                    .putHeader("content-type", "application/json")
                    .end(encoded);
        });
    }

    private void deleteUbiSOM(RoutingContext routingContext){
        final String ubiSOMID = routingContext.request().getParam("id");
        service.undeployUbiSOM(ubiSOMID, res -> {
            if(res.result())
                routingContext.response()
                    .setStatusCode(204)
                    .end();
            else
                serviceUnavailable(routingContext);
        });
    }

    private void feedUbiSOM(RoutingContext routingContext){
        final String ubiSOMID = routingContext.request().getParam("id");
        JsonObject o = routingContext.getBodyAsJson();
        o.put("id", ubiSOMID);
        service.feedUbiSOM(o , res -> {
            if(res.result())
                routingContext.response()
                        .setStatusCode(202)
                        .end("ok");
            else
                serviceUnavailable(routingContext);
        });
    }

    private void getUbiSOMData(RoutingContext routingContext){
        String ubiSOMID = routingContext.request().getParam("id");
        service.getUbiSOMData(ubiSOMID, res -> {
            if(res.succeeded()){
                final String encoded = res.result().encode();
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(encoded);
            }
            else
                notFound(routingContext);
        });
    }

    private void getUbiSOMHitCount(RoutingContext routingContext){
        String ubiSOMID = routingContext.request().getParam("id");
        service.getHitCount(ubiSOMID, res -> {
            if(res.succeeded()){
                final String encoded = res.result().encode();
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(encoded);
            }
            else
                notFound(routingContext);
        });
    }

    private void getUbiSOMUMat(RoutingContext routingContext){
        String ubiSOMID = routingContext.request().getParam("id");
        service.getUMat(ubiSOMID, res -> {
            if(res.succeeded()){
                final String encoded = res.result().encode();
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(encoded);
            }
            else
                notFound(routingContext);
        });
    }

    private void getUbiSOMWeights(RoutingContext routingContext){
        String ubiSOMID = routingContext.request().getParam("id");
        service.getWeights(ubiSOMID, res -> {
            if(res.succeeded()){
                final String encoded = res.result().encode();
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(encoded);
            }
            else
                notFound(routingContext);
        });
    }

    private JsonObject getQueryString(String query){
        return RandomUtils.convertQueryString(query);
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    private void badRequest(RoutingContext context) {
        context.response().setStatusCode(400).end();
    }

    private void notFound(RoutingContext context) {
        context.response().setStatusCode(404).end();
    }

    private void serviceUnavailable(RoutingContext context) {
        context.response().setStatusCode(503).end();
    }
}
