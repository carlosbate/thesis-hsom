package micro.verticles;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import micro.service.DataStreamerService;

public abstract class DataStreamerVerticle extends AbstractVerticle {

    protected String id;
    protected EventBus eb;
    DataStreamerService service;

    public DataStreamerVerticle(DataStreamerService service, String baseUrl){
        this.service = service;
        this.id = baseUrl;
    }

    @Override
    public void start() throws Exception {
        super.start();
        eb = vertx.eventBus();
        initEBPub();
    }

    @Override
    public void stop() throws Exception{
        super.stop();
        service.deleteDataStreamer(this.id, voidAsyncResult -> {
            if(voidAsyncResult.succeeded())
                System.out.println(this.id + " says bye");
            else
                System.out.println(voidAsyncResult.cause());
        });
    }

    protected abstract void initEBPub();
}
