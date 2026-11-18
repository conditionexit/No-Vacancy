package io.github.conditionexit.novacancy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Simple enemy from the original game implementation.
 *
 * <p>The enemy follows its player when nearby and returns to its starting
 * position otherwise. It shares its texture with the caller.</p>
 *
 * @see Player
 * @see GameObject
 */
public class Enemy extends GameObject {
    static final float EW=32f, EH=58f;

    float sx, sy;
    Player player;
    Texture tex;

    /**
     * Creates an enemy at its starting position.
     *
     * @param x initial world x-coordinate of the sprite's bottom-left corner
     * @param y initial world y-coordinate of the sprite's bottom-left corner
     * @param player player the enemy follows
     * @param tex shared texture used to draw this enemy
     */
    public Enemy(float x, float y, Player player, Texture tex) {
        super(x, y, EW, EH);
        this.sx = x;
        this.sy = y;
        this.player = player;
        this.tex = tex;
    }

    /** Updates pursuit or return-to-start movement for one frame. */
    @Override
    public void update(float dt) {
        float px = player.getX() - x, py = player.getY() - y;
        float dPlayer = (float) Math.sqrt(px*px+py*py);
        if (dPlayer < 200f && dPlayer>4f) {
            x += px / dPlayer * 60f * dt;
            y += py / dPlayer * 60f * dt;
        }
        else {
            float hx = sx - x, hy = sy - y;
            float dHome = (float) Math.sqrt(hx * hx + hy * hy);
            if (dHome > 2f) {
                x += hx / dHome * 60f * dt;
                y += hy / dHome * 60f * dt;
            }
        }
    }

    /** Draws the enemy using its shared texture. */
    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(tex, x, y, EW, EH);
    }
}
