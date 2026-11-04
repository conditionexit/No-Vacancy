package entities;

public class Trap extends GameEntity {

    private final float x;
    private final float y;
    private final int damage;

    public Trap(float x, float y, int damage) {
        super(x, y);
        if (damage <= 0) {
            throw new IllegalArgumentException("damage must be greater than 0");
        }
        this.x = x;
        this.y = y;
        this.damage = damage;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }
}
