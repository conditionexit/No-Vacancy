package entities;

public class Potion {
    private final float x;
    private final float y;
    private boolean consumed;

    public Potion(float x, float y) {
        this.x = x;
        this.y = y;
        consumed = false;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public boolean isConcumed(){
        return consumed;
    }

    public void consume(){
        consumed = true;
    }

    /** Marks it as not collected for restaring levels. */
    public void drop() {consumed = false;}
}
