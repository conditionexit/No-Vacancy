package entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A maze-bound hostile character that pursues the player within a limited
 * detection radius and otherwise returns to its original spawn location.
 *
 * <p>Each enemy shares a texture supplied and owned by {@link Main}; it must
 * therefore not dispose that texture. Movement uses Euclidean distance to
 * produce a direction vector of unit length, then limits one frame's travel
 * to the remaining distance to avoid overshooting its target. Like
 * {@link Player}, it uses a smaller collision hitbox, short movement
 * substeps, and independent axis checks so collision remains reliable and
 * permits sliding along walls.</p>
 *
 * @see Player
 * @see Maze
 */
public class Enemy {
    public static final float W = 32f, H = 58f;     // sprite size
    static final float SPEED = 60f;
    static final float CHASE_RANGE = 200f;
    static final float STOP_DIST = 4f;              // stops chasing when this close
    private final Maze maze;

    public float x, y;                              // sprite bottom-left
    private final float homeX, homeY;
    private final Texture tex;                      // shared texture, owned (and disposed) by Main


    // For initial testing, we are using a rectangle slightly narrower than the sprite
    private static final float HB_X = 6f;
    private static final float HB_Y = 6f;
    private static final float HB_W = 20f;
    private static final float HB_H = 46f;

    /**
     * Creates an enemy at its home position.
     *
     * @param x initial sprite bottom-left x-coordinate; also the x-coordinate
     *          to which the enemy returns when the player is out of range
     * @param y initial sprite bottom-left y-coordinate; also the y-coordinate
     *          to which the enemy returns when the player is out of range
     * @param tex shared texture used to render this enemy; ownership remains
     *            with the caller, normally {@link Main}
     * @param maze maze used for hitbox collision checks
     */
    public Enemy(float x, float y, Texture tex, Maze maze) {
        this.x = x;
        this.y = y;
        this.homeX = x;
        this.homeY = y;
        this.tex = tex;
        this.maze = maze;
    }

    /**
     * Computes the Euclidean distance from this enemy's sprite origin to a
     * world position.
     *
     * <p>The same distance is used for detection, stopping thresholds, and
     * normalization of a movement direction, ensuring those decisions use a
     * consistent measure.</p>
     *
     * @param px target x-coordinate in world pixels
     * @param py target y-coordinate in world pixels
     * @return straight-line distance to the target, in world pixels
     */
    public float distanceTo(float px, float py) {
        float dx = px - x, dy = py - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Tests whether the enemy's narrower collision hitbox may occupy a
     * proposed sprite position.
     *
     * @param px proposed sprite bottom-left x-coordinate
     * @param py proposed sprite bottom-left y-coordinate
     * @return {@code true} if the hitbox fits inside the maze without touching
     *         a wall tile
     */
    private boolean free(float px, float py) {
        return maze.isAreaFree(
            px + HB_X,
            py + HB_Y,
            HB_W,
            HB_H
        );
    }

    /**
     * Updates chase or return-to-home movement for one frame.
     *
     * <p>If the player is closer than {@link #CHASE_RANGE}, the enemy moves
     * toward the player but stops inside {@link #STOP_DIST}. The stopping
     * distance prevents jitter and repeated attempts to occupy the exact same
     * point. When the player leaves the detection range, the enemy instead
     * returns to its recorded home position, with a small tolerance to avoid
     * imperceptible correction movement at the destination.</p>
     *
     * @param dt elapsed time since the previous frame, in seconds
     * @param playerX player's sprite-origin x-coordinate in world pixels
     * @param playerY player's sprite-origin y-coordinate in world pixels
     */
    public void update(float dt, float playerX, float playerY) {
        float d = distanceTo(playerX, playerY);
        if (d < CHASE_RANGE) {
            if (d > STOP_DIST) {
                moveToward(playerX, playerY, d, dt);
            }
        } else {
            float dHome = distanceTo(homeX, homeY);
            if (dHome > 2f) {
                moveToward(homeX, homeY, dHome, dt);
            }
        }
    }

    /**
     * Moves toward a target while respecting speed, collision, and the
     * remaining straight-line distance.
     *
     * <p>The per-frame travel is capped at {@code dist}, which prevents a
     * large delta time from carrying the enemy past the target. The resulting
     * vector is split into eight-pixel increments, then each x and y
     * component is accepted independently when its hitbox remains free.</p>
     *
     * @param tx target x-coordinate in world pixels
     * @param ty target y-coordinate in world pixels
     * @param dist already-computed Euclidean distance to the target
     * @param dt elapsed time since the previous frame, in seconds
     */
    private void moveToward(float tx, float ty, float dist, float dt) {
        if (dist <= 0f) return;

        float movement = Math.min(SPEED * dt, dist);

        float dx = (tx - x) / dist * movement;
        float dy = (ty - y) / dist * movement;

        float maxStep = 8f;

        int steps = Math.max(
            1,
            (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / maxStep)
        );

        float stepX = dx / steps;
        float stepY = dy / steps;

        for (int i = 0; i < steps; i++) {
            if (free(x + stepX, y)) x += stepX;
            if (free(x, y + stepY)) y += stepY;
        }
    }

    /**
     * Returns the row used to interleave this enemy with maze rows during
     * drawing.
     *
     * @param tileHeight height of a maze tile in world pixels
     * @return zero-based row containing the enemy sprite origin
     */
    public int row(float tileHeight) {
        return (int) (y / tileHeight);
    }

    /**
     * Draws this enemy with the shared texture.
     *
     * @param batch active sprite batch configured with the game camera's
     *              projection matrix
     */
    public void draw(SpriteBatch batch) {
        batch.draw(tex, x, y, W, H);
    }
}
