package micro.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class DBPuller {

    private JDBCClient jdbc;

    private String dbName;

    public DBPuller(Vertx vertx, JsonObject config, String dbName){
        this.dbName = dbName;
        jdbc = JDBCClient.createShared(vertx, config, dbName);
    }

    public Future<JsonObject> getRandomData(int resLimit){
        Future<JsonObject> data = Future.future();
        jdbc.getConnection(ar -> {
            if(ar.failed())
                data.fail("Couldn't connect with the database");
            else{
                SQLConnection con = ar.result();
                con.query("SELECT * FROM " + dbName + " ORDER BY RANDOM() LIMIT " + resLimit + ";", res -> {
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

    public void close(){
        jdbc.close();
    }

}
