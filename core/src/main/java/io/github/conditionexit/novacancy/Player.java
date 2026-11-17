package io.github.conditionext.novacancy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player {
    public static final float W = 32f, H = 66f;                  // sprite size
    static final float SPEED = 250f;

    static final float HB_X = 6f, HB_W = 20f;   // hitbox, measured from the sprite's bottom-left
    static final float HB_Y = 6f, HB_H = 60f;   // HB_Y = transparent pixels under the feet

    public float x, y;                                           // sprite bottom-left

    private final Maze maze;

    @SuppressWarnings("unchecked")
    private final Animation<TextureRegion>[] walk = new Animation[4];   // 0 down, 1 left, 2 right, 3 up
    private float stateTime;
    private int dir = 0;
    private boolean moving;

    private final Sound stepSound;
    private long stepId = -1;
    private boolean stepPlaying = false;

    public Player(Maze maze) {
        this.maze = maze;

        walk[0] = maze.animation("player", 3);    // down
        walk[1] = maze.animation("player", 7);    // left
        walk[2] = maze.animation("player", 11);   // right
        walk[3] = maze.animation("player", 15);   // up

        stepSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/stepSound.mp3"));

        respawn();
    }

    /** Puts the player back at the maze's spawn point. */
    public void respawn() {
        x = maze.playerSpawn.x - W / 2;
        y = maze.playerSpawn.y;
    }

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

        // movement (separate axes = slide along walls)
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) { dx = dx / len * SPEED * dt; dy = dy / len * SPEED * dt; }
        if (free(x + dx, y)) x += dx;
        if (free(x, y + dy)) y += dy;
    }

    private void updateStepSound() {
        if (moving && !stepPlaying) {
            stepId = stepSound.loop(0.5f);
            stepPlaying = true;
        } else if (!moving && stepPlaying) {
            stepSound.stop(stepId);
            stepPlaying = false;
        }
    }

    public void draw(SpriteBatch batch) {
        batch.draw(walk[dir].getKeyFrame(stateTime), x, y, W, H);
    }

    /** Row of tiles the player's feet are on (used for draw order). */
    public int feetRow() {
        return (int) ((y + HB_Y) / maze.tileHeight());
    }

    // true if the hitbox at (px, py) touches no wall tile
    private boolean free(float px, float py) {
        float left = px + HB_X,  right = left + HB_W;
        float bottom = py + HB_Y, top = bottom + HB_H;
        return !maze.isSolid(left, bottom)  && !maze.isSolid(right, bottom)
            && !maze.isSolid(left, top)     && !maze.isSolid(right, top);
    }

    public void dispose() {
        stepSound.dispose();
    }

    public static final int MAX_LIVES = 3;
    private int lives = MAX_LIVES;
    private float hitCooldown = 0f;     // invincibility time after a hit

    /** Returns true if the hit landed (false while invincible). */
    public boolean takeDamage() {
        if (hitCooldown > 0) return false;
        lives--;
        hitCooldown = 1.5f;
        return true;
    }

    public void heal()       { lives = Math.min(lives + 1, MAX_LIVES); }
    public boolean isDead()  { return lives <= 0; }
    public int getLives()    { return lives; }

    public void reset() {
        lives = MAX_LIVES;
        hitCooldown = 0f;
        respawn();
    }
}
