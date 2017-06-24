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
import micro.UbiHSOMRESTAPI;
import micro.entity.UbiHSOM;
import micro.entity.UbiHSOMNode;
import micro.entity.UbiSOM;
import micro.service.UbiUbiHSOMCoreService;
import micro.service.UbiHSOMService;
import micro.utils.JsonUtils;
import micro.utils.RandomUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class UbiHSOMVerticle extends AbstractVerticle{

    private static Logger log = LoggerFactory.getLogger(UbiHSOMVerticle.class);
    private UbiHSOMService service;

    /*
    private void initService(){
        final String serviceType = config().getString("service.type");
        log.info("Service type: " + serviceType);
        switch (serviceType) {
            case "hashmap":
                service = new UbiHSOMMapService();
                break;
            case "jdbc":
                //TODO
                break;
            case "mongodb":
                //TODO
                break;
            default:
                service = new UbiHSOMMapService();
        }

        final String mode = config().getString("service.mode");
        switch (mode){
            case "test":
                service.populate().setHandler(res -> {
                    if(res.result())
                        log.info("Populate completed.");
                    else
                        log.info("Populate failed.");
                });
                break;
            case "prod":
                //TODO what to do when its in production. Not sure what to do here tho xD
                break;
            default:
                //TODO what to do when in its dev mode. Not sure what to do here tho xD
        }
    }
    */

    @Override
    public void start() throws Exception {
        super.start();
        //initService();
        service = new UbiUbiHSOMCoreService(vertx, config(), log);

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

        router.route().handler(BodyHandler.create());
        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
                .allowedMethods(allowMethods));

        // Routes. The actual API is defined in UbiHSOMRESTAPI.java file.
        router.get(UbiHSOMRESTAPI.GET_ALL).handler(this::getAllHSOM);
        router.post(UbiHSOMRESTAPI.ADD_HSOM).handler(this::addHSOM);
        router.delete(UbiHSOMRESTAPI.DELETE_HSOM).handler(this::deleteHSOM);
        router.get(UbiHSOMRESTAPI.GET_HSOM).handler(this::getHSOM);
        router.get(UbiHSOMRESTAPI.GET_NODE).handler(this::getNode);
        router.post(UbiHSOMRESTAPI.ADD_NODE).handler(this::addNode);
        router.delete(UbiHSOMRESTAPI.DELETE_NODE).handler(this::deleteNode);
        router.post(UbiHSOMRESTAPI.ADD_EDGE).handler(this::connectNode);

        vertx.createHttpServer().requestHandler(router::accept).listen(port, ar -> {
            if(ar.succeeded())
                log.info("UbiHSOM service deployed on http://localhost:" + port + ".");
            else
                log.info("Failed to deploy UbiHSOM service...\n" + ar.cause());
        });
    }

    @Override
    public void stop() throws Exception {

    }

    /**
     *  This method serves as a wrapper for the rest of the handlers.
     *  It recieves the current routing context along with a consumer object.
     *  Since most of the API is defined as Futures (to ensure an async behavior) this method purpose is to ensure
     *  the methods called from service are/aren't succeeded and if they are it passes to the @consumer variable the
     *  actual service result. Which is, e.g, in the following Future<Boolean> the result is Boolean. So a Boolean
     *  variable is passed to the lambda expression defined on @consumer.
     *  This save us time because its not needed to check if the Future was/wasn't succeeded, we just need to care
     *  if there is/isn't a value.
     *
     * @param routingContext
     * @param consumer
     * @param <T>
     * @return
     */
    private <T> Handler<AsyncResult<T>> resultHandler(RoutingContext routingContext, Consumer<T> consumer){
        return res -> {
            if(res.succeeded())
                consumer.accept(res.result());
            else
                serviceUnavailable(routingContext);
        };
    }

    /**
     *  This method is responsible to retrieve all the existing UbiHSOM models.
     *
     * @method GET
     * @path /hsoms
     * @param routingContext
     */
    private void getAllHSOM(RoutingContext routingContext) {
        service.getAll().setHandler(resultHandler(routingContext, res -> {
            if(res != null){
                final String encoded = res.toString();
                routingContext.request().response()
                        .putHeader("content-type", "application/json")
                        .end(encoded);
            }
            else
                serviceUnavailable(routingContext);
        }));
    }

    /**
     *  This method is responsible to add a new UbiHSOM entity.
     *
     * @method POST
     * @path /hsoms
     * @param routingContext
     */
    private void addHSOM(RoutingContext routingContext){
        JsonObject newHSOM = routingContext.getBodyAsJson();
        final String hsomName = newHSOM.getString("name");
        final UbiHSOM ubiHsom = new UbiHSOM(hsomName);
        service.addHSOM(ubiHsom).setHandler(resultHandler(routingContext, res -> {
            if(res != null){
                final String encoded = res.toJson().encodePrettily();
                routingContext.response()
                        .setStatusCode(201)
                        .putHeader("content-type", "application/json")
                        .end(encoded);
            }
            else
                serviceUnavailable(routingContext);
        }));
    }

    /**
     *  This method is responsible to delete the UbiHSOM entity with the id = :id
     *
     * @method DELETE
     * @path /hsoms/:id
     * @param routingContext
     */
    private void deleteHSOM(RoutingContext routingContext){
        final String hsomID = routingContext.request().getParam("id");
        if(!RandomUtils.isIntParsable(hsomID)){
            sendError(400, routingContext.response());
            return;
        }
        service.deleteHSOM(hsomID).setHandler(resultHandler(routingContext, res -> {
            if(res)
                routingContext.response()
                        .setStatusCode(204)
                        .end();
            else
                serviceUnavailable(routingContext);
        }));
    }

    /**
     * This method is responsible to return the UbiHSOM entity with the id = :id
     *
     * @method GET
     * @path /hsoms/:id
     * @param routingContext
     */
    private void getHSOM(RoutingContext routingContext){
        final String hsomID = routingContext.request().getParam("id");
        service.getHSOM(hsomID).setHandler(resultHandler(routingContext, res -> {
            if(res.isPresent()){
                final String encoded = res.get().toJson().encodePrettily();
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(encoded);
            }
            else
                notFound(routingContext);
        }));
    }

    /**
     *  This method is responsible to return the node with the node_id = :nodeid from the UbiHSOM with the id = :id
     *
     * @method GET
     * @path /hsoms/:id/nodes/:nodeid
     * @param routingContext
     */
    private void getNode(RoutingContext routingContext){
        final String hsomID = routingContext.request().getParam("id");
        final String nodeID = routingContext.request().getParam("nodeid");
        service.getNode(hsomID, nodeID).setHandler(resultHandler(routingContext, res -> {
            if(res.isPresent()){
                final String encoded = res.get().toJson().encodePrettily();
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(encoded);
            }
            else
                notFound(routingContext);
        }));
    }

    /**
     *  This method is responsible to add a new node to the UbiHSOM with the id = :id
     *
     * @method POST
     * @path /hsoms/:id/nodes
     * @param routingContext
     */
    private void addNode(RoutingContext routingContext){
        final String hsomID = routingContext.request().getParam("id");
        JsonObject newNode = routingContext.getBodyAsJson();
        UbiSOM model = new UbiSOM(newNode.getJsonObject("model"));
        List<String> order = JsonUtils.convertJaToList(newNode.getJsonArray("order"));
        long timer = newNode.getLong("timer");
        UbiHSOMNode node = new UbiHSOMNode(model, order, timer);
        service.addNode(hsomID, node).setHandler(resultHandler(routingContext, res -> {
            if(res != null){
                final String encoded = res.toJson().encodePrettily();
                routingContext.response()
                        .setStatusCode(201)
                        .putHeader("content-type", "application/json")
                        .end(encoded);
            }
            else
                serviceUnavailable(routingContext);
        }));
    }

    /**
     *  This method is responsible to delete the node with the node_id = :nodeid off the UbiHSOM with the id = :id
     *
     * @method DELETE
     * @path /hsoms/:id/nodes/:nodeid
     * @param routingContext
     */
    private void deleteNode(RoutingContext routingContext) {
        final String hsomID = routingContext.request().getParam("id");
        final String nodeID = routingContext.request().getParam("nodeid");
        if(!RandomUtils.isIntParsable(hsomID, nodeID)){
            sendError(400, routingContext.response());
            return;
        }
        service.deleteNode(hsomID, nodeID).setHandler(resultHandler(routingContext, res -> {
            if(res)
                routingContext.response()
                        .setStatusCode(204)
                        .end();
            else
                serviceUnavailable(routingContext);
        }));
    }

    /**
     *  This method is responsible to add an edge to the UbiHSOM with the id = :id.
     *  By doing so, it connects two nodes.
     *
     * @method POST
     * @path /hsoms/:id/edges
     * @param routingContext
     */
    private void connectNode(RoutingContext routingContext) {
        final String hsomID = routingContext.request().getParam("id");
        JsonObject newEdge = routingContext.getBodyAsJson();
        final String source = newEdge.getString("source");
        final String target = newEdge.getString("target");
        if(source == null || target == null){
            notFound(routingContext);
            return;
        }
        service.addEdge(hsomID, source, target).setHandler(resultHandler(routingContext, res -> {
            if(res)
                routingContext.response()
                        .setStatusCode(201)
                        .end();
            else
                serviceUnavailable(routingContext);
        }));
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
