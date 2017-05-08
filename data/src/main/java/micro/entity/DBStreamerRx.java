package micro.entity;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import rx.Observable;

public class DBStreamerRx {

    private JDBCClient jdbc;

    public DBStreamerRx(Vertx vertx, JsonObject config, String dbName){
        JDBCClient.createShared(vertx, config, dbName);
    }

    public Observable<ResultSet> getRandomData(String dataName){
        return getRandomXData(dataName, 5);
    }

    public Observable<ResultSet> getRandomXData(String dataName, int resLimit){
        /*
        jdbc.getConnectionObservable().subscribe(conn -> {
            Observable<ResultSet> res = conn.queryObservable("SELECT * FROM "+ dataName +" ORDER BY LIMIT " + resLimit + ";");
            res.subscribe(resultSet -> ResultSet::toJson, null, conn::close);
        });
        */
        return jdbc.getConnectionObservable()
                .flatMap(conn -> conn.queryObservable("SELECT * FROM "+ dataName +" ORDER BY LIMIT " + resLimit + ";"))
                .asObservable();
    }

}
