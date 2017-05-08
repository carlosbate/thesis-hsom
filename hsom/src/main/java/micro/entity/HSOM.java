package micro.entity;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HSOM {

    private String id;
    private String name;
    private HSOMGraph guts;
    private Map<String, HSOMNode> nodes;

    public HSOM(String name) {
        this.name = name;
        guts = new HSOMGraphImpl();
        nodes = new HashMap<>();
    }

    public HSOM(JsonObject o){
        this.id = o.getString("id");
        this.name = o.getString("name");
        this.nodes = convertNodesToHashMap(o.getJsonArray("nodes"));
        this.guts = new HSOMGraphImpl(o.getJsonObject("graph"));
    }

    private Map<String,HSOMNode> convertNodesToHashMap(JsonArray nodes) {
        Map<String, HSOMNode> res = new HashMap<>(nodes.size());
        nodes.stream().forEach(o -> {
            JsonObject node = ((JsonObject)o);
            String id = node.getString("id");
            HSOMNode newNode = new HSOMNode(node);
            res.put(id, newNode);
        });
        return res;
    }

    public String getId(){ return this.id; }

    public void setId(String id) { this.id = id;}

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }


    public HSOMGraph getGuts(){
        return this.guts;
    }

    public Map<String, HSOMNode> getNodes(){
        return this.nodes;
    }

    public boolean add(String id){
        if(!nodes.containsKey(id))
            return guts.add(id) && (nodes.put(id, new HSOMNode(id))) != null;
        return false;
    }

    public boolean add(HSOMNode node){
        String id = node.getId();
        if(!nodes.containsKey(id))
            return guts.add(id) && (nodes.put(id, node)) == null;
        return false;
    }

    public HSOMNode getNode(String id){
        return nodes.get(id);
    }

    public boolean connect(String idX, String idY){
        if(nodes.containsKey(idX) && nodes.containsKey(idY))
            return  guts.connect(idX, idY) &&
                    nodes.get(idX).addConsumer(idY);
        return false;
    }

    public boolean remove(String id){
        return remove(id, false);
    }

    public boolean remove(String id, boolean cascade){
        if(nodes.containsKey(id))
            if(cascade)
                return cascadeRemoval(id);
            else
                return defaultRemoval(id);
        else
            return false;
    }

    private boolean cascadeRemoval(String id){
        return guts.getDependencies(id).stream().allMatch(this::removeDataSources) &&
                guts.getDependencies(id).stream().allMatch(this::removeCascadingNode) &&
                removeDataSources(id) &&
                guts.remove(id, true) &&
                nodes.remove(id) != null;
    }

    private boolean removeDataSources(String id){
        if(nodes.containsKey(id)){
            guts.getIncoming(id).forEach(asc -> updateCascAsc(asc, id));
            return true;
        }
        return false;
    }

    private void updateCascAsc(String asc, String current){
        HSOMNode ascNode = nodes.get(asc);
        ascNode.removeConsumer(current);
    }

    private boolean removeCascadingNode(String id){
        return nodes.remove(id) != null;
    }

    private boolean defaultRemoval(String id){
        if(isRoot(id))
            return guts.remove(id) && (nodes.remove(id) != null);
        return removeFromAsc(id) &&
                guts.remove(id) &&
                (nodes.remove(id) != null);
    }

    private boolean removeFromAsc(String id){
        if(nodes.containsKey(id) && hasAsc(id)){
            guts.getIncoming(id).forEach(asc -> updateAsc(asc, id));
            return true;
        }
        return false;
    }

    public List<String> getAscNodes(String id){
        List<String> res = new LinkedList<>();
        res.addAll(guts.getDependencies(id));
        return res;
    }

    private void updateAsc(String asc, String current){
        HSOMNode ascNode = nodes.get(asc);
        ascNode.removeConsumer(current);
        ascNode.addConsumers(guts.getOutgoing(current));
    }

    private boolean hasAsc(String id){
        return guts.getIncoming(id).size() > 0;
    }

    private boolean isRoot(String id){ return guts.getIncoming(id).size() == 0; }

    public String toString(){
        return toJson().encode();
    }

    public JsonObject toJson(){
        JsonObject res = new JsonObject();
        if(this.id != null)
            res.put("id", this.id);
        res.put("name", this.name);
        res.put("nodes", this.nodesToJson());
        res.put("graph", this.graphJson());
        return res;
    }

    private JsonArray nodesToJson(){
        JsonArray jNodes = new JsonArray();
        nodes.values().forEach(node -> jNodes.add(node.toJson()));
        return jNodes;
    }

    public JsonObject graphJson(){
        return this.guts.toJson();
    }

}
