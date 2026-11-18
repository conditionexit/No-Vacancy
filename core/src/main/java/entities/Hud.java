package entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Draws player status in screen space, independent of the scrolling maze.
 *
 * <p>The HUD displays remaining lives and shows the key icon after the player
 * collects the key. It owns and disposes the heart and key textures it loads.</p>
 *
 * @see Player
 * @see Key
 */
public class Hud {
    private static final float SIZE = 24f, GAP = 4f, PAD = 10f;

    private final OrthographicCamera cam = new OrthographicCamera();
    private final Texture heartTex;
    private final Texture keyTex;

    /**
     * Creates the HUD for the virtual screen size used by the game.
     *
     * @param viewW virtual viewport width in pixels
     * @param viewH virtual viewport height in pixels
     */
    public Hud(float viewW, float viewH) {
        cam.setToOrtho(false, viewW, viewH);          // (0,0) = bottom-left of the screen
        heartTex = new Texture("heart.png");
        heartTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        keyTex = new Texture("key.png");
        keyTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    /**
     * Draws the life indicators and, when owned, the key icon.
     * Call after the world batch has ended.
     *
     * @param batch sprite batch used to draw the HUD
     * @param lives current number of player lives
     * @param maxLives number of life icons to display
     * @param hasKey whether the player has collected the key
     */
    public void draw(SpriteBatch batch, int lives, int maxLives, boolean hasKey) {
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        float y = cam.viewportHeight - SIZE - PAD;    // top-left corner
        for (int i = 0; i < maxLives; i++) {
            batch.setColor(1, 1, 1, i < lives ? 1f : 0.25f);   // lost hearts are faded
            batch.draw(heartTex, PAD + i * (SIZE + GAP), y, SIZE, SIZE);
        }
        if (hasKey) {
            batch.draw(keyTex, PAD, y - SIZE - GAP, SIZE, SIZE);
        }
        batch.setColor(Color.WHITE);                  // reset tint
        batch.end();
    }

    /** Releases the textures owned by this HUD. */
    public void dispose() {
        heartTex.dispose();
        keyTex.dispose();
    }
}
