package micro.entity;

import io.vertx.core.json.JsonObject;

import java.util.Set;

/**
 * Created by Rubito on 18/08/2016.
 */
public interface UbiHSOMGraph {

    /**
     *  Adds n nodes to the graph.
     */
    public boolean add(String... ids);;
    public boolean add(Set<String> ids);

    /**
     *  Connects the node X with Y (Adds an edge)
     */
    public boolean connect(String idX, String idY);

    /**
     *  Connects an array of nodes in the following fashion: (a1,a2)(b1,b2)
     *  @Pre: The array length needs to be even.
     */
    public boolean connect(String... ids);

    /**
     *  Removes the given node X. By default that node is removed and the incoming edges of X are reconnected to the
     *  corresponding vertex of the outgoing edges of X.
     *  If the flag cascade is set to true, all nodes that depends on X, either directly or indirectly, are removed.
     */
    public boolean remove(String id);
    public boolean remove(String id, boolean cascade);

    /**
     *  Returns node ascendants
     */
    public Set<String> getIncoming(String id);

    /**
     * Returns node descendantes
     */
    public Set<String> getOutgoing(String id);
    /**
     *  Prints the internal graph that represents the algorithm.
     */
    public String toString();

    public Set<String> getDependencies(String id);

    /**
     * True if the vertex exists
     */
    public boolean contains(String... id);

    /**
     * Return the number of nodes
     */
    public int numberVertexs();

    /**
     * Return the number of edges
     */
    public int numberEdges();

    /**
     * Returns the representation of the graph in JSON
     */
    public JsonObject toJson();

}
