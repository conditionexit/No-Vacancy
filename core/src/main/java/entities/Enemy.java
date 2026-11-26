package entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/** An enemy that chases the player nearby and returns to its spawn. */
public class Enemy {
    public static final float W = 32f, H = 58f;     // sprite size
    static final float SPEED = 60f;
    static final float CHASE_RANGE = 200f;
    static final float STOP_DIST = 4f;              // stops chasing when this close
    private final Maze maze;

    public float x, y;                              // sprite bottom-left
    private final float homeX, homeY;
    private final Texture tex;                      // shared texture, owned (and disposed) by GameScreen


    // For initial testing, we are using a rectangle slightly narrower than the sprite
    private static final float HITBOX_OFFSET_X = 6f;
    private static final float HITBOX_OFFSET_Y = 6f;
    private static final float HITBOX_WIDTH = 20f;
    private static final float HITBOX_HEIGHT = 46f;

    /** Creates an enemy. The texture is shared and owned by GameScreen. */
    public Enemy(float x, float y, Texture tex, Maze maze) {
        this.x = x;
        this.y = y;
        this.homeX = x;
        this.homeY = y;
        this.tex = tex;
        this.maze = maze;
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

    /** Chases the player in range; otherwise returns home. */
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

    /** Draws the enemy using the shared texture. */
    public void draw(SpriteBatch batch) {
        batch.draw(tex, x, y, W, H);
    }
}
