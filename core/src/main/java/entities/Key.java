package entities;

public class Key extends GameEntity {

    private final float x;
    private final float y;
    private boolean collected;

    public Key(float x, float y) {
        super(x, y);
        this.x = x;
        this.y = y;
        this.collected = false;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }
}
