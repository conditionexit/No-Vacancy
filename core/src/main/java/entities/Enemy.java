package entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * A Chirurgeons enemy that chases a nearby player and otherwise returns home.
 *
 * <p>Walking uses four animations from {@code assets/chirurgeons.tsx}.
 * Each animation contains four pictures from the sprite sheet. The enemy
 * chooses a direction, advances a timer, and draws the picture for that time.</p>
 *
 * <p>Each enemy has its own direction and timer. The picture textures belong
 * to the map and are shared, so enemies do not need a dispose method.</p>
 */
public class Enemy {
    public static final float W = 32f, H = 58f;     // sprite size
    static final float SPEED = 60f;
    static final float CHASE_RANGE = 200f;
    static final float STOP_DIST = 4f;              // stops chasing when this close
    private final Maze maze;

    public float x, y;                              // sprite bottom-left
    private final float homeX, homeY;
    /** Walking loops indexed as 0 down, 1 left, 2 right, and 3 up. */
    @SuppressWarnings("unchecked")
    private final Animation<TextureRegion>[] walk = new Animation[4];
    /** Index of the facing direction; kept when the enemy stops moving. */
    private int dir = 0; // 0 down, 1 left, 2 right, 3 up
    /** Seconds spent walking since the last stop; used to select a frame. */
    private float stateTime;


    // For initial testing, we are using a rectangle slightly narrower than the sprite
    private static final float HITBOX_OFFSET_X = 6f;
    private static final float HITBOX_OFFSET_Y = 6f;
    private static final float HITBOX_WIDTH = 20f;
    private static final float HITBOX_HEIGHT = 46f;

    /**
     * Places an enemy at its home position and obtains its four walking loops.
     *
     * <p>The tileset numbers 3, 7, 11, and 15 identify animation definitions,
     * not frame counts. For example, tile 3 lists frames 0, 1, 2, and 3 for
     * walking down. Each frame lasts 120 milliseconds, or 0.12 seconds.</p>
     *
     * @param x starting sprite bottom-left x-coordinate in world pixels
     * @param y starting sprite bottom-left y-coordinate in world pixels
     * @param maze loaded maze containing the chirurgeons tileset and walls
     */
    public Enemy(float x, float y, Maze maze) {
        this.x = x;
        this.y = y;
        this.homeX = x;
        this.homeY = y;
        this.maze = maze;

        // Each four-frame walking loop is defined in chirurgeons.tsx.
        walk[0] = maze.animation("chirurgeons", 3);    // direction down
        walk[1] = maze.animation("chirurgeons", 7);    // direction left
        walk[2] = maze.animation("chirurgeons", 11);   // direction right
        walk[3] = maze.animation("chirurgeons", 15);   // direction up
    }

    /** Returns the distance from the enemy to a position. */
    public float distanceTo(float px, float py) {
        float dx = px - x, dy = py - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /** Checks whether the enemy hitbox fits at this position. */
    private boolean free(float px, float py) {
        return maze.isAreaFree(
            px + HITBOX_OFFSET_X,
            py + HITBOX_OFFSET_Y,
            HITBOX_WIDTH,
            HITBOX_HEIGHT
        );
    }

    /**
     * Moves toward the player when nearby, or toward the spawn when far away.
     *
     * <p>The position is saved before movement. Subtracting that old position
     * afterward gives the actual movement, including any wall sliding. This
     * lets the walking animation face the way the enemy really travelled.</p>
     *
     * @param dt time since the previous update, in seconds
     * @param playerX player x-coordinate used as the chase target
     * @param playerY player y-coordinate used as the chase target
     */
    public void update(float dt, float playerX, float playerY) {
        float oldX = x;
        float oldY = y;

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

        updateAnimation(dt, x - oldX, y - oldY);
    }

    /**
     * Chooses the walking direction and advances the animation clock.
     *
     * <p>No movement resets the clock to zero, displaying the first frame of
     * the current direction. An axis must have at least 20 percent more
     * movement to take over the facing direction. Near a diagonal, the enemy
     * keeps its previous axis, avoiding rapid changes between two views.
     * Negative x is left, positive x is right, negative y is down, and positive
     * y is up. {@link Math#abs(float)} compares distances without signs.</p>
     *
     * <p>Adding dt advances the clock in seconds. The looping animation uses
     * that time to select a frame and starts again after the fourth frame.</p>
     *
     * @param dt elapsed time in seconds
     * @param dx actual horizontal movement during this update
     * @param dy actual vertical movement during this update
     */
    private void updateAnimation(float dt, float dx, float dy) {
        if (dx == 0 && dy == 0) {
            stateTime = 0;
            return;
        }

        // Keep the previous axis near diagonals so the picture does not flicker.
        boolean horizontal = dir == 1 || dir == 2;
        if (Math.abs(dx) > Math.abs(dy) * 1.2f) {
            horizontal = true;
        } else if (Math.abs(dy) > Math.abs(dx) * 1.2f) {
            horizontal = false;
        }

        if (horizontal) {
            if (dx < 0) dir = 1; // left
            else dir = 2;        // right
        } else {
            if (dy < 0) dir = 0; // down
            else dir = 3;        // up
        }

        stateTime += dt;
    }

    /** Moves in small steps to prevent crossing walls and allow sliding. */
    private void moveToward(float tx, float ty, float dist, float dt) {
        if (dist <= 0f) return;

        float movement = Math.min(SPEED * dt, dist);
        Vector2 delta = Movement.scaleDirection(tx - x, ty - y, movement);
        Vector2 next = Movement.moveWithSlide(x, y, delta.x, delta.y, this::free);
        x = next.x;
        y = next.y;
    }

    /** Returns the map row used for drawing. */
    public int row(float tileHeight) {
        return (int) (y / tileHeight);
    }

    /**
     * Draws one picture from the selected walking loop at the enemy position.
     *
     * <p>{@code walk[dir]} selects a direction. Its {@code getKeyFrame}
     * method selects a picture using stateTime. W and H control the displayed
     * size, independently of the picture's size in the sprite sheet.</p>
     *
     * @param batch active sprite batch using the world camera projection
     */
    public void draw(SpriteBatch batch) {
        // batch.draw(tex, x, y, W, H); // Old static enemy drawing.
        batch.draw(walk[dir].getKeyFrame(stateTime), x, y, W, H);
    }
}
