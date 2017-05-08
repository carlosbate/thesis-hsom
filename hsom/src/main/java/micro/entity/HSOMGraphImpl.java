package micro.entity;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.*;

public class HSOMGraphImpl implements HSOMGraph {

    private DirectedGraph<String, DefaultEdge> guts;

    public HSOMGraphImpl(){
        guts = new SimpleDirectedGraph<>(DefaultEdge.class);
    }

    public HSOMGraphImpl(JsonObject o){
        guts = convertJOToGraph(o);
    }

    private DirectedGraph<String, DefaultEdge> convertJOToGraph(JsonObject o){
        DirectedGraph<String, DefaultEdge> res = new SimpleDirectedGraph<>(DefaultEdge.class);
        JsonArray nodes = o.getJsonArray("nodes");
        JsonArray edges = o.getJsonArray("edges");
        nodes.stream().forEach(elem -> {
            String s = ((String) elem);
            res.addVertex(s);
        });
        edges.stream().forEach(elem -> {
            JsonObject edge = ((JsonObject) elem);
            String source = edge.getString("source");
            String target = edge.getString("target");
            res.addEdge(source, target);
        });
        return res;
    }

    @Override
    public boolean add(String... ids) {
        if(this.contains(ids))
            return false;
        for(String id : ids)
            guts.addVertex(id);
        return true;
    }

    @Override
    public boolean add(Set<String> ids) {
        if(this.contains(ids))
            return false;
        for(String id : ids)
            guts.addVertex(id);
        return true;
    }

    @Override
    public boolean connect(String idX, String idY) {
        if(this.contains(idX, idY)) {
            guts.addEdge(idX, idY);
            return true;
        }
        return false;
    }

    @Override
    public boolean connect(String... ids) {
        if((ids.length % 2) == 0){
            for (int i = 0; i < ids.length - 1; i = i + 2)
                this.guts.addEdge(ids[i], ids[i+1]);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(String id) {
        return remove(id, false);
    }

    @Override
    public boolean remove(String id, boolean cascade){
        if(cascade)
            return cascadeRemoval(id);
        else
            return defaultRemoval(id);
    }

    public boolean contains(String... ids){
        for(String id : ids)
            if(guts.containsVertex(id))
                return true;
        return false;
    }

    private boolean contains(Set<String> ids){
        for(String id : ids)
            if(guts.containsVertex(id))
                return true;
        return false;
    }

    private boolean defaultRemoval(String id){
        if(guts.containsVertex(id)){
            Set<String> inVertex = getIncomingVertices(id);
            Set<String> outVertex = getOutgoingVertices(id);
            for(String in : inVertex)
                for(String out : outVertex)
                    guts.addEdge(in,out);
            guts.removeVertex(id);
            return true;
        }
        return false;
    }

    private boolean cascadeRemoval(String id) {
        if(guts.containsVertex(id)) {
            Set<String> vertexs = getDependencies(id);
            vertexs.forEach((vertex) -> guts.removeVertex(vertex));
            guts.removeVertex(id);
            return true;
        }
        return false;
    }

    private Set<String> getIncomingVertices(String id){
        Set<DefaultEdge> incoming = guts.incomingEdgesOf(id);
        Set<String> inVertex = new HashSet<>(incoming.size());
        incoming.forEach((edge) -> inVertex.add(guts.getEdgeSource(edge)));
        return inVertex;
    }

    private Set<String> getOutgoingVertices(String id){
        Set<DefaultEdge> outgoing = guts.outgoingEdgesOf(id);
        Set<String> outVertex = new HashSet<>(outgoing.size());
        outgoing.forEach( (edge) -> outVertex.add(guts.getEdgeTarget(edge)));
        return outVertex;
    }

    private Set<String> getOutgoingVertices(Set<String> ids){
        Set<String> res = new HashSet<>();
        for(String id : ids)
            res.addAll(getOutgoingVertices(id));
        return res;
    }

    @Override
    public Set<String> getDependencies(String id){
        Set<String> dependencies = getOutgoingVertices(id);
        Set<String> res = new HashSet<>();
        res.addAll(dependencies);
        while(hasDependencies(dependencies)){
            dependencies = getOutgoingVertices(dependencies);
            res.addAll(dependencies);
        }
        return res;
    }

    @Override
    public Set<String> getIncoming(String id) {
        return getIncomingVertices(id);
    }

    @Override
    public Set<String> getOutgoing(String id) {
        return getOutgoingVertices(id);
    }

    private boolean hasDependencies(Set<String> ids){
        int depCount = 0;
        for(String id : ids)
            if(hasDependencies(id))
                depCount++;
        return depCount > 0;
    }

    private boolean hasDependencies(String id){
        return !guts.outgoingEdgesOf(id).isEmpty();
    }

    @Override
    public int numberVertexs() {
        return guts.vertexSet().size();
    }

    @Override
    public int numberEdges() {
        return guts.edgeSet().size();
    }


    @Override
    public String toString() {
        return toJson().encode();
    }

    @Override
    public JsonObject toJson() {
        return graphToJson();
    }

    private JsonObject graphToJson(){
        JsonObject j = new JsonObject();
        j.put("nodes", allNodesToJson());
        j.put("edges", allEdgesToJson());
        return j;
    }

    private JsonArray allNodesToJson(){
        JsonArray jNodes = new JsonArray();
        int i = 0;
        for(String node: guts.vertexSet())
            jNodes.add(node);
        return jNodes;
    }

    private JsonObject nodeToJson(String node, int id) {
        JsonObject jNode = new JsonObject();
        jNode.put("id", id);
        return jNode;
    }


    private JsonArray allEdgesToJson(){
        JsonArray jEdges = new JsonArray();
        for(DefaultEdge edge : guts.edgeSet())
            jEdges.add(edgeToJson(edge));
        return jEdges;
    }

    private JsonObject edgeToJson(DefaultEdge edge){
        JsonObject jEdge = new JsonObject();
        jEdge.put("source", guts.getEdgeSource(edge));
        jEdge.put("target", guts.getEdgeTarget(edge));
        return jEdge;
    }
}