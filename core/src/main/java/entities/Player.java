package entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

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
 * @see Movement
 */
public class Player {
    /** Rendered sprite width and height in world pixels. */
    public static final float W = 32f, H = 66f;
    static final float SPEED = 250f;

    static final float HITBOX_OFFSET_X = 6f, HITBOX_WIDTH = 20f;   // hitbox, measured from the sprite's bottom-left
    // HITBOX_OFFSET_Y = transparent pixels under the feet. HITBOX_HEIGHT is feet-only (top-down collision):
    // the head may overlap walls above; feetRow() keeps the draw order correct.
    static final float HITBOX_OFFSET_Y = 6f, HITBOX_HEIGHT = 14f;

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
    /** Footstep loudness (0..1) from the SFX settings; multiplied with the base footstep volume. */
    private float sfxVolume = 1f;

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
     * <p>The spawn object's x-coordinate is treated as the sprite center,
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
     * multiplied by {@link #SPEED} and {@code dt} via
     * {@link Movement#scaleDirection}, so diagonal movement retains the
     * intended speed. Substep collision resolution and axis sliding are
     * delegated to {@link Movement#moveWithSlide} using this player's
     * hitbox check ({@link #free(float, float)}).</p>
     *
     * <p>The method also reduces the post-hit cooldown, advances directional
     * walking animation only while moving, and starts or stops the looping
     * footstep sound to match movement state.</p>
     *
     * @param dt elapsed time since the previous frame, in seconds
     */

    public void update(float dt) {
        hitCooldown = Math.max(0f, hitCooldown - dt);

        Vector2 input = readMovementInput();
        moving = input.x != 0 || input.y != 0;

        updateDirection();
        updateAnimation(dt);
        updateStepSound();
        movePlayer(input.x, input.y, dt);
    }

//    public void update(float dt) {
//        hitCooldown = Math.max(0f, hitCooldown - dt);
//        // input
//        float dx = (Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0);
//        float dy = (Gdx.input.isKeyPressed(Input.Keys.W) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.S) ? 1 : 0);
//        moving = dx != 0 || dy != 0;
//
//        // facing direction + animation time
//        if (Gdx.input.isKeyPressed(Input.Keys.W))      dir = 3;
//        else if (Gdx.input.isKeyPressed(Input.Keys.S)) dir = 0;
//        else if (Gdx.input.isKeyPressed(Input.Keys.A)) dir = 1;
//        else if (Gdx.input.isKeyPressed(Input.Keys.D)) dir = 2;
//
//        if (moving) stateTime += dt; else stateTime = 0;
//
//        updateStepSound();
//
//        Vector2 delta = Movement.scaleDirection(dx, dy, SPEED * dt);
//        Vector2 next = Movement.moveWithSlide(x, y, delta.x, delta.y, this::free);
//        x = next.x;
//        y = next.y;
//    }

    /**
     * Synchronizes the looping footstep sound with the current movement flag.
     *
     * <p>The returned sound-instance identifier is retained so that only this
     * player's loop is stopped when movement ends.</p>
     */
    private void updateStepSound() {
        if (moving && !stepPlaying) {
            stepId = stepSound.loop(0.5f * sfxVolume);
            stepPlaying = true;
        } else if (!moving && stepPlaying) {
            stepSound.stop(stepId);
            stepPlaying = false;
        }
    }

    private Vector2 readMovementInput() {
        float dx = 0;
        float dy = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx++;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx--;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy++;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy--;

        return new Vector2(dx, dy);
    }

    private void updateDirection() {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            dir = 3;
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            dir = 0;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            dir = 1;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            dir = 2;
        }
    }

    private void updateAnimation(float dt) {
        if (moving) {
            stateTime += dt;
        } else {
            stateTime = 0;
        }
    }

    private void movePlayer(float dx, float dy, float dt) {
        Vector2 delta = Movement.scaleDirection(dx, dy, SPEED * dt);
        Vector2 next = Movement.moveWithSlide(x, y, delta.x, delta.y, this::free);

        x = next.x;
        y = next.y;
    }

    /**
     * Draws the current directional animation frame at the player's world
     * position.
     *
     * @param batch active sprite batch configured with the game camera's
     *              projection matrix
     */
    public void draw(SpriteBatch batch) {
        Shadow.draw(batch, x + W / 2f, y + HITBOX_OFFSET_Y + 2f, 26f, 10f);

        boolean blinking = hitCooldown >0 && (int) (hitCooldown / 0.15f) % 2 == 0; // 0.15 is the blinking speed

        if(blinking) {
            batch.setColor(1f,1f,1f, 0.35f); // 0.35 is opacity
        }

        batch.draw(walk[dir].getKeyFrame(stateTime), x, y, W, H);

        batch.setColor(1f,1f,1f,1f);
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
        return (int) ((y + HITBOX_OFFSET_Y) / maze.tileHeight());
    }

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
            px + HITBOX_OFFSET_X,
            py + HITBOX_OFFSET_Y,
            HITBOX_WIDTH,
            HITBOX_HEIGHT
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

    public static final int MAX_LIVES = 6;
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
        hitCooldown = 2f; // 2 seconds before you can get hit again
        return true;
    }

    /** Removes one heart using the trap's own cooldown. */
    boolean takeTrapHit() {
        if (isDead()) return false;
        lives--;
        return true;
    }

    /**
     * Applies the effective SFX volume (master x sfx, 0..1) to the footstep sound,
     * including a footstep loop that is already playing.
     */
    void setSfxVolume(float volume) {
        sfxVolume = Math.max(0f, Math.min(1f, volume));
        if (stepPlaying) stepSound.setVolume(stepId, 0.5f * sfxVolume);
    }

    /** Stops movement audio while gameplay is paused. */
    void stopFootsteps() {
        moving = false;
        updateStepSound();
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
