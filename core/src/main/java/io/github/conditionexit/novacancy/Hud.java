package io.github.conditionext.novacancy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Screen-space overlay. Uses its own fixed camera, so it never moves with the map. */
public class Hud {
    private static final float SIZE = 24f, GAP = 4f, PAD = 10f;

    private final OrthographicCamera cam = new OrthographicCamera();
    private final Texture heartTex;

    /** viewW / viewH must match the game's viewport size (640 x 500). */
    public Hud(float viewW, float viewH) {
        cam.setToOrtho(false, viewW, viewH);          // (0,0) = bottom-left of the screen
        heartTex = new Texture("heart.png");
        heartTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    /** Call AFTER the world batch has ended. */
    public void draw(SpriteBatch batch, int lives, int maxLives) {
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        float y = cam.viewportHeight - SIZE - PAD;    // top-left corner
        for (int i = 0; i < maxLives; i++) {
            batch.setColor(1, 1, 1, i < lives ? 1f : 0.25f);   // lost hearts are faded
            batch.draw(heartTex, PAD + i * (SIZE + GAP), y, SIZE, SIZE);
        }
        batch.setColor(Color.WHITE);                  // reset tint
        batch.end();
    }

    public void dispose() {
        heartTex.dispose();
    }
}
