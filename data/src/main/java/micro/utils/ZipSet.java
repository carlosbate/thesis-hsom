package micro.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ZipSet{

  private Set<String> order;

  public ZipSet() {
    order = new LinkedHashSet<>();
  }

  public Set<String> getSet(){
    return this.order;
  }

  public boolean add(String s){
    return order.add(s);
  }

  public boolean remove(String s){
    return order.remove(s);
  }

  public int size(){
    return order.size();
  }

  public boolean contains(String s){
    return order.contains(s);
  }

  public boolean reOrder(String oldPub, String newPub){
    List<String> tmp = new LinkedList<>();
    if(!order.contains(oldPub) && !order.contains(newPub))
      return false;
    for(String s : order)
      if (s.equalsIgnoreCase(oldPub))
        tmp.add(newPub);
      else if(s.equalsIgnoreCase(newPub))
        tmp.add(oldPub);
      else
        tmp.add(s);
      order = new LinkedHashSet<>(tmp);
      return true;
  }

  public JsonArray toJson(){
    JsonArray res = new JsonArray();
    for(String s : order)
      res.add(s);
    return res;
  }

  public String toString(){
    return order.toString();
  }

}
