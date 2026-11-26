package entities;

/**
 * A collectible health potion placed in the maze.
 *
 * <p>When collected by the player, it restores one life if the player's
 * health is below maximum. The potion can be dropped/reset when the level restarts.</p>
 *
 * @author Mahammad Jafarli
 * @see Player
 * @see GameScreen
 */
public class Potion {
    private final float x;
    private final float y;
    private boolean consumed;

    /**
     * Creates an unconsumed potion at a world position.
     *
     * @param x potion center x-coordinate in world pixels
     * @param y potion center y-coordinate in world pixels
     */
    public Potion(float x, float y) {
        this.x = x;
        this.y = y;
        consumed = false;
    }

    /** @return potion center x-coordinate in world pixels */
    public float getX(){
        return x;
    }

    /** @return potion center y-coordinate in world pixels */
    public float getY(){
        return y;
    }

    /** @return {@code true} if this potion has already been consumed */
    public boolean isConsumed(){
        return consumed;
    }

    /** Marks this potion as consumed so it cannot be collected again. */
    public void consume(){
        consumed = true;
    }

    /** Marks this potion as unconsumed so it reappears when the level restarts. */
    public void drop() {
        consumed = false;
    }
}
