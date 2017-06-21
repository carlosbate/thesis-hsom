package micro.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import micro.entity.DBPullerType;

public class DBPuller {

    private JDBCClient jdbc;

    private String dbName;

    private String selectors;

    private long seqTimeStamp;

    private long maxTimeStamp;

    private DBPullerType type;

    public DBPuller(Vertx vertx, JsonObject config, String dbName, String selectors, DBPullerType type){
        this.dbName = dbName;
        jdbc = JDBCClient.createShared(vertx, config, dbName);
        this.selectors = selectors;
        if(selectors.equalsIgnoreCase(""))
            this.selectors = "*";
        this.seqTimeStamp = 0;
        this.maxTimeStamp = -1;
        this.type = type;
    }

    private Future<Long> getMaxOfTimestamp(){
        Future<Long> longResult = Future.future();
        jdbc.getConnection(ar -> {
            if(ar.failed())
                longResult.fail(ar.cause());
            else{
                SQLConnection con = ar.result();
                con.query("SELECT max(timestamp) FROM " + dbName + ";", res -> {
                    if(res.succeeded()){
                            longResult.complete(res.result().getResults().get(0).getLong(0));
                    }
                    else{
                        System.out.println();
                        System.out.println();
                        System.out.println(res.cause());
                        System.out.println();
                        System.out.println();
                        longResult.fail(res.cause());
                    }
                });
                con.close();
            }
        });
        return longResult;
    }

    public Future<JsonObject> getSequentialData(){
        Future<JsonObject> data = Future.future();
        if(maxTimeStamp < 0){
            getMaxOfTimestamp().setHandler(longAr -> {
                if(longAr.succeeded())
                    maxTimeStamp = longAr.result();
                else
                    data.fail(longAr.cause());
            });
        }
        if(seqTimeStamp == maxTimeStamp){
            getMaxOfTimestamp().setHandler(longAr -> {
                if(longAr.succeeded()){
                    maxTimeStamp = longAr.result();
                    if(seqTimeStamp == maxTimeStamp){
                        System.out.println("END OF TIME SERIES");
                        System.out.println("END OF TIME SERIES");
                        System.out.println("END OF TIME SERIES");
                        System.out.println("END OF TIME SERIES");
                        data.fail("out of data");
                    }
                }
                else
                    data.fail(longAr.cause());
            });
        }
        jdbc.getConnection(ar -> {
            if(ar.failed())
                data.fail("Couldn't connect with the database");
            else{
                SQLConnection con = ar.result();
                con.query("SELECT " + selectors + " FROM " + dbName + " WHERE timestamp = " + seqTimeStamp + " ORDER BY RANDOM();", res -> {
                    if(res.succeeded()){
                        seqTimeStamp++;
                        if(res.result().getResults().size() > 0)
                            data.complete(res.result().toJson());
                        else
                            data.fail("empty tuple");
                    }
                    else
                        data.fail("Query failed");
                });
                con.close();
            }
        });
        return data;
    }

    public Future<JsonObject> getRandomData(int resLimit){
        Future<JsonObject> data = Future.future();
        jdbc.getConnection(ar -> {
            if(ar.failed())
                data.fail("Couldn't connect with the database");
            else{
                SQLConnection con = ar.result();
                con.query("SELECT " + selectors + " FROM " + dbName + " ORDER BY RANDOM() LIMIT " + resLimit + ";", res -> {
                    if(res.succeeded())
                        data.complete(res.result().toJson());
                    else
                        data.fail("Query failed");
                });
                con.close();
            }
        });
        return data;
    }

    public DBPullerType getType() {
        return this.type;
    }

    public void close(){
        jdbc.close();
    }

}
