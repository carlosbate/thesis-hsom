package micro.utils;

import micro.entity.HUbiSOMNode;

import java.util.*;

public class UbiFactoryManager {

    private int idCount;

    private Map<Integer, HUbiSOMNode> db;

    public UbiFactoryManager() {
        db = new HashMap<Integer, HUbiSOMNode>();
        idCount = 0;
    }

    public boolean addUbiSOM(){
        HUbiSOMNode newHUbiSOMNode = new HUbiSOMNode(idCount + "");
        return db.put(idCount++, newHUbiSOMNode) == null;
    }

    public boolean deleteUbiSom(String id){
        return db.remove(Integer.parseInt(id)) != null;
    }

    public HUbiSOMNode getUbiSom(String id){ return db.get(Integer.parseInt(id)); }

    public List<HUbiSOMNode> getAll(){
        List<HUbiSOMNode> res = new LinkedList<HUbiSOMNode>();
        res.addAll(db.values());
        return res;
    }

    public boolean isEmpty(){ return idCount == 0; }
}
