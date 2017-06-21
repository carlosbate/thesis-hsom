package micro.utils;

import micro.entity.UbiHSOM;

import java.util.*;

public class UbiHSOMManager {

    private int idCount;
    private Map<Integer, UbiHSOM> db;

    public UbiHSOMManager() {
        db = new HashMap<>();
        idCount = 0;
    }

    public boolean addHSOM(UbiHSOM newUbiHSOM){
        UbiHSOM res = db.put(idCount, newUbiHSOM);
        newUbiHSOM.setId(idCount++ + "");
        return res == null;
    }

    public boolean removeHSOM(String id){
        UbiHSOM res = db.remove(id);
        return res != null;
    }

    public UbiHSOM getHSOM(String id){
        return db.get(id);
    }

    public List<UbiHSOM> getAll(){
        List<UbiHSOM> res = new LinkedList<>();
        res.addAll(db.values());
        return res;
    }

    public boolean isEmpty(){
        return idCount == 0;
    }

}
