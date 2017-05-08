package micro.utils;

import io.vertx.core.json.JsonObject;

public class Tuple {
    private int x;
    private int y;

    public Tuple(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return this.toJson().encode();
    }

    public boolean equals(int x, int y) {
        return this.x == x && this.y == y;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Tuple == false)
            return false;
        Tuple t = (Tuple) obj;
        return this.equals(t.getX(), t.getY());
    }

    public JsonObject toJson(){
        return new JsonObject()
                .put("x", this.x)
                .put("y", this.y);
    }
}
