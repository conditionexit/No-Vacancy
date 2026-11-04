package io.github.conditionexit.novacancy.model;

public class Key {

    private final float x;
    private final float y;
    private boolean collected;

    public Key(float x, float y) {
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
