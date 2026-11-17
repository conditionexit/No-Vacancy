package entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Controllable maze-escape character, including movement, presentation, and
 * short-lived combat state.
 *
 * <p>The player reads the keyboard directly during {@link #update(float)}:
 * W/A/S/D select a movement vector, which is normalized before speed and
 * delta time are applied. Normalization makes diagonal movement no faster
 * than movement along one axis. The resulting displacement is split into
 * small increments before collision checks, reducing the chance that a long
 * frame moves the hitbox through a thin wall. Horizontal and vertical
 * movement are then accepted separately so that a blocked axis can slide
 * along an available one.</p>
 *
 * <p>This class owns its footstep {@link Sound}; callers must invoke
 * {@link #dispose()} when the game is finished. It relies on its associated
 * {@link Maze} for animated player frames, spawning, tile dimensions, and
 * collision queries.</p>
 *
 * @see Maze
 * @see Enemy
 */
public class Player {
    /** Rendered sprite width and height in world pixels. */
    public static final float W = 32f, H = 66f;
    static final float SPEED = 250f;

    static final float HB_X = 6f, HB_W = 20f;   // hitbox, measured from the sprite's bottom-left
    static final float HB_Y = 6f, HB_H = 60f;   // HB_Y = transparent pixels under the feet

    /** Sprite bottom-left position in maze world coordinates. */
    public float x, y;

    private final Maze maze;

    @SuppressWarnings("unchecked")
    private final Animation<TextureRegion>[] walk = new Animation[4];   // 0 down, 1 left, 2 right, 3 up
    private float stateTime;
    private int dir = 0;
    private boolean moving;

    private final Sound stepSound;
    private long stepId = -1;
    private boolean stepPlaying = false;

    /**
     * Creates a player linked to a maze and places it at that maze's player
     * spawn point.
     *
     * <p>The constructor obtains the four directional animations from the
     * map's {@code player} tileset and loads the footstep sound. The sound is
     * subsequently owned by this player and must be released with
     * {@link #dispose()}.</p>
     *
     * @param maze maze that supplies collision, spawn, tile-size, and
     *             animation data
     */
    public Player(Maze maze) {
        this.maze = maze;

        walk[0] = maze.animation("player", 3);    // down
        walk[1] = maze.animation("player", 7);    // left
        walk[2] = maze.animation("player", 11);   // right
        walk[3] = maze.animation("player", 15);   // up

        stepSound = Gdx.audio.newSound(
            Gdx.files.internal("Sounds/stepSound.mp3")
        );

        respawn();
    }

    /**
     * Puts the player back at the maze's player spawn point.
     *
     * <p>The spawn object's x-coordinate is treated as the sprite centre,
     * whereas its y-coordinate is used as the sprite bottom. This matches the
     * map-placement convention used by the game.</p>
     */
    public void respawn() {
        x = maze.playerSpawn.x - W / 2;
        y = maze.playerSpawn.y;
    }

    /**
     * Advances player input, animation, footsteps, invincibility, and
     * collision-aware movement by one rendered frame.
     *
     * <p>Movement uses W/A/S/D. The input vector is normalized before being
     * multiplied by {@link #SPEED} and {@code dt}, so diagonal movement
     * retains the intended speed. A displacement is divided into eight-pixel
     * substeps; each substep tests the rectangular hitbox against the maze.
     * Testing x and y independently intentionally permits wall sliding rather
     * than rejecting the entire diagonal move when only one axis is blocked.</p>
     *
     * <p>The method also reduces the post-hit cooldown, advances directional
     * walking animation only while moving, and starts or stops the looping
     * footstep sound to match movement state.</p>
     *
     * @param dt elapsed time since the previous frame, in seconds
     */
    public void update(float dt) {
        hitCooldown = Math.max(0f, hitCooldown - dt);
        // input
        float dx = (Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0);
        float dy = (Gdx.input.isKeyPressed(Input.Keys.W) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.S) ? 1 : 0);
        moving = dx != 0 || dy != 0;

        // facing direction + animation time
        if (Gdx.input.isKeyPressed(Input.Keys.W))      dir = 3;
        else if (Gdx.input.isKeyPressed(Input.Keys.S)) dir = 0;
        else if (Gdx.input.isKeyPressed(Input.Keys.A)) dir = 1;
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) dir = 2;

        if (moving) stateTime += dt; else stateTime = 0;

        updateStepSound();

        float len = (float) Math.sqrt(dx * dx + dy * dy);

        if (len > 0) {
            dx = dx / len * SPEED * dt;
            dy = dy / len * SPEED * dt;
        }

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

        // movement (separate axes = slide along walls)
        //float len = (float) Math.sqrt(dx * dx + dy * dy);
        //if (len > 0) { dx = dx / len * SPEED * dt; dy = dy / len * SPEED * dt; }
        //if (free(x + dx, y)) x += dx;
        //if (free(x, y + dy)) y += dy;
    }

    /**
     * Synchronizes the looping footstep sound with the current movement flag.
     *
     * <p>The returned sound-instance identifier is retained so that only this
     * player's loop is stopped when movement ends.</p>
     */
    private void updateStepSound() {
        if (moving && !stepPlaying) {
            stepId = stepSound.loop(0.5f);
            stepPlaying = true;
        } else if (!moving && stepPlaying) {
            stepSound.stop(stepId);
            stepPlaying = false;
        }
    }

    /**
     * Draws the current directional animation frame at the player's world
     * position.
     *
     * @param batch active sprite batch configured with the game camera's
     *              projection matrix
     */
    public void draw(SpriteBatch batch) {
        batch.draw(walk[dir].getKeyFrame(stateTime), x, y, W, H);
    }

    /**
     * Returns the tile row occupied by the player's feet for painter-style
     * draw ordering.
     *
     * <p>Using the hitbox's bottom offset, rather than the sprite origin,
     * keeps the player visually behind walls that are above their feet.</p>
     *
     * @return zero-based maze tile row containing the player's feet
     */
    public int feetRow() {
        return (int) ((y + HB_Y) / maze.tileHeight());
    }

    // true if the hitbox at (px, py) touches no wall tile
//    private boolean free(float px, float py) {
//        float left = px + HB_X,  right = left + HB_W;
//        float bottom = py + HB_Y, top = bottom + HB_H;
//        return !maze.isSolid(left, bottom)  && !maze.isSolid(right, bottom)
//            && !maze.isSolid(left, top)     && !maze.isSolid(right, top);
//
//    }

    /**
     * Tests whether the player's collision rectangle may occupy a proposed
     * sprite position.
     *
     * <p>The hitbox is narrower than the artwork and starts above transparent
     * pixels below the feet. This makes collision reflect the character's
     * walkable footprint rather than the full sprite rectangle.</p>
     *
     * @param px proposed sprite bottom-left x-coordinate
     * @param py proposed sprite bottom-left y-coordinate
     * @return {@code true} when the translated hitbox is within the maze and
     *         intersects no wall tile
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
     * Releases the footstep sound allocated by this player.
     *
     * <p>This does not dispose maze-owned animation textures.</p>
     */
    public void dispose() {
        stepSound.dispose();
    }

    public static final int MAX_LIVES = 3;
    private int lives = MAX_LIVES;
    private float hitCooldown = 0f;     // invincibility time after a hit

    /**
     * Applies one enemy hit unless the player is still invincible from a
     * previous successful hit.
     *
     * <p>A successful hit removes one life and starts a 1.5-second cooldown.
     * The cooldown is decremented by {@link #update(float)}, preventing
     * overlapping enemies from draining all lives in one frame.</p>
     *
     * @return {@code true} if a life was removed; {@code false} if the
     *         invincibility cooldown was active
     */
    public boolean takeDamage() {
        if (hitCooldown > 0) return false;
        lives--;
        hitCooldown = 1.5f;
        return true;
    }

    /**
     * Restores one life without exceeding {@link #MAX_LIVES}.
     */
    public void heal()       { lives = Math.min(lives + 1, MAX_LIVES); }

    /**
     * Reports whether all player lives have been lost.
     *
     * @return {@code true} when the life count is zero or below
     */
    public boolean isDead()  { return lives <= 0; }

    /**
     * Returns the current number of remaining lives.
     *
     * @return current life count
     */
    public int getLives()    { return lives; }

    /**
     * Restores the initial life count, clears temporary invincibility, and
     * respawns the player.
     */
    public void reset() {
        lives = MAX_LIVES;
        hitCooldown = 0f;
        respawn();
    }
}
