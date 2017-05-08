package micro.verticles;

import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import micro.DataStreamerAPI;
import micro.service.DataStreamerService;
import micro.utils.UrlBuilder;

import java.util.HashSet;
import java.util.Set;

public class DataStreamerFrontendVerticle extends AbstractVerticle{

    DataStreamerService service;

    public DataStreamerFrontendVerticle(DataStreamerService service) {
        this.service = service;
    }

    @Override
    public void start() throws Exception {
        super.start();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(getAllowedHeaders())
                .allowedMethods(getAllowedMethods()));

        router.get(DataStreamerAPI.GET_ALL).handler(this::getAll);
        router.get(DataStreamerAPI.GET_ONE).handler(this::getDataStreamer);
        router.post(DataStreamerAPI.CREATE_ONE).handler(this::createDataStreamer);
        router.delete(DataStreamerAPI.DELETE_ONE).handler(this::deleteDataStreamer);
        router.patch(DataStreamerAPI.SEND_MESSAGE).handler(this::sendMessageToDataStreamer);

        vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port"), ar -> {
            if(ar.succeeded())
                System.out.println("Running");
            else
                System.out.println(ar.cause());
        });

    }

    private void getAll(RoutingContext routingContext){
        service.getAll(ar -> {
            if(ar.succeeded())
                sendJsonResponse(routingContext, ar.result().toString());
            else
                sendJsonResponse(routingContext, ar.cause().toString());
        });
    }

    private void getDataStreamer(RoutingContext routingContext){
        String id = routingContext.request().getParam("id");
        service.getDataStreamer(id, ar -> {
            if(ar.succeeded())
                sendJsonResponse(routingContext, ar.result().toString());
            else
                sendJsonResponse(routingContext, ar.cause().toString());
        });
    }

    private void createDataStreamer(RoutingContext routingContext){
        JsonObject newObj = routingContext.getBodyAsJson();
        service.createDataStreamer(newObj, ar -> {
            if(ar.succeeded())
                sendJsonResponse(routingContext, ar.result().toString());
            else
                sendJsonResponse(routingContext, ar.cause().toString());
        });
    }

    private void deleteDataStreamer(RoutingContext routingContext){
        String id = routingContext.request().getParam("id");
        vertx.eventBus().publish(UrlBuilder.createKillUrl(id), "kill");
        service.deleteDataStreamer(id, ar -> {
            if(ar.succeeded())
                routingContext.response().setStatusCode(204).end();
            else
                sendJsonResponse(routingContext, ar.cause().toString());
        });
    }

    private void sendMessageToDataStreamer(RoutingContext routingContext){
        String id = routingContext.request().getParam("id");
        JsonObject message = routingContext.getBodyAsJson();
        vertx.eventBus().publish(UrlBuilder.createInUrl(id), message);
        routingContext.response().setStatusCode(200).end();
    }

    private void sendJsonResponse(RoutingContext routingContext, String response){
        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(response);
    }

    private Set<String> getAllowedHeaders(){
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        return allowHeaders;
    }

    private Set<HttpMethod> getAllowedMethods(){
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        return allowMethods;
    }

    @Override
    public void stop() throws Exception {
        service.close();
        super.stop();
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
