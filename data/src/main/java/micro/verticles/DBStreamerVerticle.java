package micro.verticles;

import io.vertx.core.json.JsonObject;
import micro.entity.DBStreamer;
import micro.service.DBPuller;
import micro.service.DataStreamerService;
import micro.utils.DBPullerFactory;

public class DBStreamerVerticle extends DataStreamerVerticle {

    private DBStreamer streamer;
    private DBPuller db;
    private long timerID;

    public DBStreamerVerticle(DataStreamerService service, DBStreamer streamer){
        super(service, streamer.getId());
        this.streamer = streamer;
        this.id = streamer.getId();
    }

    @Override
    public void start() throws Exception {
        super.start();
        db = DBPullerFactory.create(this.getVertx(), streamer.getDbName());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        vertx.cancelTimer(timerID);
        db.close();
    }

    protected void initEBPub() {
        timerID = vertx.setPeriodic(streamer.getTimer(), h -> {
             db.getRandomData(streamer.getRandomness()).setHandler(ar -> {
                if(ar.succeeded())
                    vertx.eventBus().publish(this.streamer.getOutputUrl(), ar.result());
                else
                    vertx.eventBus().publish(this.streamer.getOutputUrl(), new JsonObject().put("error", "Couldn't pull data from DB \n" + ar.cause()));
            });
        });
    }

}
