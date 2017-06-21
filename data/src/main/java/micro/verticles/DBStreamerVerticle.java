package micro.verticles;

import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;
import micro.entity.DBPullerType;
import micro.entity.DBStreamer;
import micro.service.DBPuller;
import micro.service.DataStreamerService;
import micro.utils.DBPullerFactory;

public class DBStreamerVerticle extends DataStreamerVerticle {

    private DBStreamer streamer;
    private DBPuller db;
    private long timerID;
    private DBPullerType pullerType;

    public DBStreamerVerticle(DataStreamerService service, DBStreamer streamer){
        super(service, streamer.getId());
        this.streamer = streamer;
        this.id = streamer.getId();
    }

    @Override
    public void start() throws Exception {
        super.start();
        System.out.println(streamer.getDbName());
        System.out.println(streamer.getSelectors());
        System.out.println(streamer.getPullingType());
        db = DBPullerFactory.create(this.getVertx(), streamer.getDbName(), streamer.getSelectors(), streamer.getPullingType());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        vertx.cancelTimer(timerID);
        db.close();
    }

    protected void initEBPub() {
        timerID = vertx.setPeriodic(streamer.getTimer(), h -> {
            switch (streamer.getPullingType()){
                case SEQUENTIAL:
                    db.getSequentialData().setHandler(this::parseResultsAndSend);
                    break;
                case RANDOM:
                    db.getRandomData(streamer.getRandomness()).setHandler(this::parseResultsAndSend);
                    break;
            }
        });
    }

    private void parseResultsAndSend(AsyncResult<JsonObject> ar){
        if(ar.succeeded()){
            JsonObject res = new JsonObject();
            res.put("id", streamer.getId());
            res.put("data", ar.result().getJsonArray("results"));
            vertx.eventBus().publish(this.streamer.getOutputUrl(), res);
        }
    }

}
