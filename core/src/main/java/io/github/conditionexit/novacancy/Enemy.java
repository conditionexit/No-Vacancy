package io.github.conditionext.novacancy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Enemy {
    public static final float W = 32f, H = 58f;     // sprite size
    static final float SPEED = 60f;
    static final float CHASE_RANGE = 200f;
    static final float STOP_DIST = 4f;              // stops chasing when this close

    public float x, y;                              // sprite bottom-left
    private final float homeX, homeY;
    private final Texture tex;                      // shared texture, owned (and disposed) by Main

    public Enemy(float x, float y, Texture tex) {
        this.x = x;
        this.y = y;
        this.homeX = x;
        this.homeY = y;
        this.tex = tex;
    }

    public float distanceTo(float px, float py) {
        float dx = px - x, dy = py - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public void update(float dt, float playerX, float playerY) {
        float d = distanceTo(playerX, playerY);
        if (d < CHASE_RANGE && d > STOP_DIST) {
            moveToward(playerX, playerY, d, dt);
        } else {
            float dHome = distanceTo(homeX, homeY);
            if (dHome > 2f) moveToward(homeX, homeY, dHome, dt);
        }
    }

    private void moveToward(float tx, float ty, float dist, float dt) {
        x += (tx - x) / dist * SPEED * dt;
        y += (ty - y) / dist * SPEED * dt;
    }

    /** Row of tiles the enemy is on (used for draw order). */
    public int row(float tileHeight) {
        return (int) (y / tileHeight);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(tex, x, y, W, H);
    }
}
