package io.github.conditionext.novacancy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Enemy extends GameObject {
    static final float EW=32f, EH=58f;

    float sx, sy;
    Player player;
    Texture tex;

    public Enemy(float x, float y, Player player, Texture tex) {
        super(x, y, EW, EH);
        this.sx = x;
        this.sy = y;
        this.player = player;
        this.tex = tex;
    }

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

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(tex, x, y, EW, EH);
    }
}
