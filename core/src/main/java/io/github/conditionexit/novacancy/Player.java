package io.github.conditionext.novacancy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Player extends GameObject {
    static final float SPEED = 250f;
    static final float W = 32f, H = 66f;                  // sprite size
    static final float HB_X = 6f, HB_W = 20f, HB_H = 60f; // hitbox

    GameMap map;
    Animation<TextureRegion>[] walk = new Animation[4];
    float stateTime;
    int dir = 0;
    boolean moving;

    public Player(GameMap map, float x, float y) {
        super(x, y, W, H);
        this.map = map;
        walk[0] = map.tiledAnim("player", 3);    // down
        walk[1] = map.tiledAnim("player", 7);    // left
        walk[2] = map.tiledAnim("player", 11);   // right
        walk[3] = map.tiledAnim("player", 15);   // up
    }

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

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion frame = walk[dir].getKeyFrame(stateTime);
        batch.draw(frame, x, y, W, H);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + HB_X, y, HB_W, HB_H);
    }

    // true if the hitbox at (px, py) touches no wall tile
    boolean free(float px, float py) {
        return !map.solid(px + HB_X, py) && !map.solid(px + HB_X + HB_W, py)
            && !map.solid(px + HB_X, py + HB_H) && !map.solid(px + HB_X + HB_W, py + HB_H);
    }
}
