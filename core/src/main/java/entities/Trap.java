package entities;

/**
 * A positioned hazard that deals a fixed amount of damage when triggered.
 *
 * <p>This class stores trap data; collision detection and applying damage are
 * handled by the game logic.</p>
 */
public class Trap {

    private final float x;
    private final float y;
    private final int damage;

    /**
     * Creates a trap with a positive damage value.
     *
     * @param x trap x-coordinate in world pixels
     * @param y trap y-coordinate in world pixels
     * @param damage damage dealt when triggered; must be greater than zero
     * @throws IllegalArgumentException if {@code damage} is not positive
     */
    public Trap(float x, float y, int damage) {
        if (damage <= 0) {
            throw new IllegalArgumentException("damage must be greater than 0");
        }
        this.x = x;
        this.y = y;
        this.damage = damage;
    }

    /** @return trap x-coordinate in world pixels */
    public float getX() {
        return x;
    }

    /** @return trap y-coordinate in world pixels */
    public float getY() {
        return y;
    }

    /** @return damage dealt when the trap is triggered */
    public int getDamage() {
        return damage;
    }
}
