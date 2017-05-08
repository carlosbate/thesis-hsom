package micro.verticles;

import io.vertx.core.json.JsonObject;
import micro.entity.ProxyStreamer;
import micro.service.DataStreamerService;
import rx.Observable;
import rx.Subscription;

public class ProxyStreamerVerticle extends DataStreamerVerticle {

    private Observable<JsonObject> consumer;
    private Subscription subscription;
    private ProxyStreamer streamer;

    public ProxyStreamerVerticle(DataStreamerService service, String baseUrl, String inAddress, String outAddress) {
        super(service, baseUrl);
        this.streamer.setInputUrl(inAddress);
        this.streamer.setOutputUrl(outAddress);
    }

    public ProxyStreamerVerticle(DataStreamerService service, ProxyStreamer streamer) {
        super(service, streamer.getId());
        this.streamer = streamer;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        subscription.unsubscribe();
    }

    @Override
    protected void initEBPub() {
        consumer = eb.<JsonObject>consumer(this.streamer.getInputUrl()).bodyStream().toObservable();
        subscription = consumer.subscribe(m ->  vertx.eventBus().publish(this.streamer.getOutputUrl(), m));
    }

}
