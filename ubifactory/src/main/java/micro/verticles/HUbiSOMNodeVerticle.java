package micro.verticles;

import com.sun.org.apache.xpath.internal.SourceTree;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import micro.entity.DataStreamChannel;
import micro.entity.HSOMDefaultNormalizationFilter;
import micro.entity.HUbiSOMBMU;
import micro.entity.HUbiSOMNode;
import micro.service.UbiFactoryRPCService;
import micro.utils.RandomUtils;
import micro.utils.UrlBuilder;
import rx.Subscription;

import java.util.Arrays;

public class HUbiSOMNodeVerticle extends AbstractVerticle{

    DataStreamChannel channel;
    EventBus eb;
    HUbiSOMNode node;
    Subscription inputSubscription;
    Subscription getDataSubscription;
    Subscription getHitCountSubscription;
    Subscription getUMatSubscription;
    Subscription getWeightsSubscription;
    Subscription getStopSubscription;
    UbiFactoryRPCService service;
    HSOMDefaultNormalizationFilter normalizator;
    //TODO NodeSnapshotService snapshots;

    public HUbiSOMNodeVerticle(UbiFactoryRPCService service, JsonObject o){
        this.node = new HUbiSOMNode(o);
        this.channel = new DataStreamChannel(this.node.getId());
        this.service = service;
        this.normalizator = new HSOMDefaultNormalizationFilter(node.getWidth(), node.getHeight());
        //TODO this.snapshots = snapshots;
    }

    @Override
    public void start() throws Exception {
        super.start();
        eb = vertx.eventBus();

        inputSubscription = eb.<JsonObject>consumer(channel.in())
                .bodyStream()
                .toObservable()
                .subscribe(this::feedAndPropagate);

        getDataSubscription = eb.<Boolean>consumer(UrlBuilder.createGetDataUrl(this.node.getId()))
                .toObservable()
                .subscribe(this::sendData);

        getHitCountSubscription = eb.<Boolean>consumer(UrlBuilder.createGetDataHitCountUrl(this.node.getId()))
                .toObservable()
                .subscribe(this::sendHitCount);

        getUMatSubscription = eb.<Boolean>consumer(UrlBuilder.createGetDataUMatUrl(this.node.getId()))
                .toObservable()
                .subscribe(this::sendUMat);

        getWeightsSubscription = eb.<Boolean>consumer(UrlBuilder.createGetDataWeightsUrl(this.node.getId()))
                .toObservable()
                .subscribe(this::sendWeights);

        getStopSubscription = eb.<Boolean>consumer(UrlBuilder.createStopUrl(this.node.getId()))
                .toObservable()
                .subscribe(this::stopMe);

        //TODO from X to X, save snapshot in DB. Still have to create the collection hubisomnode_snapshots and the respective service.
        //TODO How much is X? Is it static, dynamic? Configurable? What?
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        service.deleteUbiSOM(node.getId(), booleanAsyncResult -> {
            if(booleanAsyncResult.succeeded())
                System.out.println(node.getId() + "says goodbye...");
            else
                System.out.println(booleanAsyncResult.cause());
        });
        inputSubscription.unsubscribe();
        getDataSubscription.unsubscribe();
        getHitCountSubscription.unsubscribe();
        getUMatSubscription.unsubscribe();
        getWeightsSubscription.unsubscribe();
        getStopSubscription.unsubscribe();
    }

    /**
     * For now this method is really simple, only works with a double [][] array which are observations.
     * Gets a JsonObject as paramenter, gets the observations and feeds the ubisom model.
     * Returns a JsonObject with an array of the BMUs associated with the inputed observations.
     * @param data - e.g. { "data": [[0.1,0.11], [0.2,0.22],[0.3,0.33]] }
     * @return JsonObject
     * {
     *   "bmus": [
     *     {
     *      "x":X,
     *      "y":Y,
     *      "prototype":{"weights": [0.1, 0.2, 0.3, ...], "label": "LabelName"}
     *     },
     *     { ... }
     *   ]
     * }
     */
    private JsonObject processData(JsonObject data){
        JsonArray res = new JsonArray();
        Arrays.stream(RandomUtils.convertObservations(data))
                .forEach(obs -> {
                    HUbiSOMBMU bmu = node.feed(obs);
                    res.add(bmu.toJson());
                });
        return new JsonObject()
            .put("id", this.node.getId())
            .put("data", res);
    }

    /**
     * Publishes the results of @processData through the event bus.
     * @param data - JsonObject with the following format e.g. { "data": [[0.1,0.11], [0.2,0.22],[0.3,0.33]] }
     */
    private void feedAndPropagate(JsonObject data){
        Arrays.stream(RandomUtils.convertObservations(data))
            .forEach(obs -> {
                HUbiSOMBMU bmu = node.feed(obs);
                JsonObject output = new JsonObject()
                    .put("id", this.node.getId())
                    .put("data", new JsonArray().add(normalizeOutput(bmu)));
                eb.publish(channel.out(), output);
            });
    }

    private JsonArray normalizeOutput(HUbiSOMBMU bmu){
        double [] aux = new double[2];
        aux [0] = bmu.getCoordX();
        aux [1] = bmu.getCoordY();
        return RandomUtils.convertDoubleArrayToJsonArray(normalizator.normalize(aux));
    }

    private void sendData(Message<Boolean> message){
        if(message.body())
            message.reply(this.node.toJson());
        else
            message.reply(new JsonObject()
                    .put("weight-labels", this.node.getWeightsLabels())
                    .put("data", this.node.getData()));
    }

    private void sendHitCount(Message<Boolean> message){
        message.reply(this.node.getHitCount());
    }

    private void sendUMat(Message<Boolean> message){
        message.reply(this.node.getUMat());
    }

    private void sendWeights(Message<Boolean> message){
        message.reply(this.node.getWeights());
    }

    private void stopMe(Message<Boolean> message){
        System.out.println("stopMe");
        message.reply(this.deploymentID());
    }

}
