package micro.utils;

import micro.entity.HSOM;

import java.util.*;

public class HSOMManager {

    private int idCount;
    private Map<Integer, HSOM> db;

    public HSOMManager() {
        db = new HashMap<>();
        idCount = 0;
    }

    public boolean addHSOM(HSOM newHSOM){
        HSOM res = db.put(idCount, newHSOM);
        newHSOM.setId(idCount++ + "");
        return res == null;
    }

    public boolean removeHSOM(String id){
        HSOM res = db.remove(id);
        return res != null;
    }

    public HSOM getHSOM(String id){
        return db.get(id);
    }

    public List<HSOM> getAll(){
        List<HSOM> res = new LinkedList<>();
        res.addAll(db.values());
        return res;
    }

    public boolean isEmpty(){
        return idCount == 0;
    }

}
