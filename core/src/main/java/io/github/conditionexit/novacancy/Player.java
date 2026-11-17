package io.github.conditionexit.novacancy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * The player-controlled character. Reads WASD input, moves with wall
 * collision (sliding along walls), and plays a walking animation for each
 * of the four facing directions.
 *
 * @author Fuad
 */
public class Player extends GameObject {
    static final float SPEED = 250f;
    static final float W = 32f, H = 66f;                  // sprite size
    static final float HB_X = 6f, HB_W = 20f, HB_H = 60f; // hitbox

    GameMap map;
    Animation<TextureRegion>[] walk = new Animation[4];
    float stateTime;
    int dir = 0;
    boolean moving;

    /**
     * Creates the player and loads its four walking animations from the map's tileset.
     *
     * @param map the map used for wall collision and animation loading
     * @param x   starting world x of the sprite's bottom-left corner
     * @param y   starting world y of the sprite's bottom-left corner
     */
    public Player(GameMap map, float x, float y) {
        super(x, y, W, H);
        this.map = map;
        walk[0] = map.tiledAnim("player", 3);    // down
        walk[1] = map.tiledAnim("player", 7);    // left
        walk[2] = map.tiledAnim("player", 11);   // right
        walk[3] = map.tiledAnim("player", 15);   // up
    }

    /**
     * Reads input, updates the facing direction and animation time, and moves
     * the player. Each axis is tested separately so the player slides along walls.
     *
     * @param dt seconds since the previous frame
     */
    @Override
    public void update(float dt) {
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

        // movement (separate axes = slide along walls)
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) { dx = dx / len * SPEED * dt; dy = dy / len * SPEED * dt; }
        if (free(x + dx, y)) x += dx;
        if (free(x, y + dy)) y += dy;
    }

    /**
     * Draws the current animation frame for the facing direction.
     *
     * @param batch the batch to draw with; must already be begun
     */
    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion frame = walk[dir].getKeyFrame(stateTime);
        batch.draw(frame, x, y, W, H);
    }

    /**
     * Returns the player's hitbox, which is narrower than the sprite.
     *
     * @return a new rectangle covering the hitbox
     */
    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + HB_X, y, HB_W, HB_H);
    }

    // true if the hitbox at (px, py) touches no wall tile
    r
}
