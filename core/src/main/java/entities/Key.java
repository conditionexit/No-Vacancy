package entities;

/**
 * A collectible key placed in a maze.
 *
 * <p>The key keeps its world position and whether the player has collected
 * it. The game loop uses this state to hide the key and show it in the HUD.</p>
 *
 * @author Yusif
 * @see Maze
 * @see Main
 */
public class Key {
    private final float x;
    private final float y;
    private boolean collected;

    /**
     * Creates an uncollected key at a world position.
     *
     * @param x key center x-coordinate in world pixels
     * @param y key center y-coordinate in world pixels
     */
    public Key(float x, float y) {
        this.x = x;
        this.y = y;
        this.collected = false;
    }

    /** @return key center x-coordinate in world pixels */
    public float getX() {
        return x;
    }

    /** @return key center y-coordinate in world pixels */
    public float getY() {
        return y;
    }

    /** @return {@code true} once the player has collected this key */
    public boolean isCollected() {
        return collected;
    }

    /** Marks this key as collected. Repeated calls have no additional effect. */
    public void collect() {
        collected = true;
    }

    /** Marks it as not collected for restaring levels. */
    public void drop() {collected = false;}
}
