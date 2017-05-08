package micro.utils;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import micro.service.DBPuller;

public class DBPullerFactory {

    public static DBPuller create(Vertx vertx, String dbName){
        DBPuller res = new DBPuller(vertx, buildSQLiteConfig(dbName),dbName);
        return res;
    }

    private static JsonObject buildSQLiteConfig(String dbName){
        String url = "jdbc:sqlite:C:/sqlite3/" + dbName + ".db";
        JsonObject config = new JsonObject();
        config.put("url", url);
        config.put("driver_class", "org.sqlite.JDBC");
        return config;
    }

}
