package entities;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Soft elliptical blob shadow drawn under characters. The texture is
 * generated in code, so no art asset is needed.
 *
 * <p>Call {@link #dispose()} once when the game shuts down.</p>
 */
public final class Shadow {
    private static Texture tex;

    private Shadow() {}

    private static void create() {
        final int size = 64;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        float c = size / 2f;
        for (int py = 0; py < size; py++) {
            for (int px = 0; px < size; px++) {
                float d = (float) Math.sqrt((px + 0.5f - c) * (px + 0.5f - c)
                    + (py + 0.5f - c) * (py + 0.5f - c)) / c;
                float a = d >= 1f ? 0f : (1f - d * d) * 0.45f;   // soft edge, max 45% opaque
                pm.drawPixel(px, py, ((int) (a * 255)) & 0xFF);   // black with alpha
            }
        }
        tex = new Texture(pm);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
    }

    /**
     * Draws a shadow centred on (cx, cy).
     *
     * @param cx shadow centre x in world pixels
     * @param cy shadow centre y in world pixels (about where the feet touch the floor)
     * @param w  shadow width in world pixels
     * @param h  shadow height in world pixels (keep it much smaller than w)
     */
    public static void draw(SpriteBatch batch, float cx, float cy, float w, float h) {
        if (tex == null) create();
        batch.draw(tex, cx - w / 2f, cy - h / 2f, w, h);
    }

    public static void dispose() {
        if (tex != null) { tex.dispose(); tex = null; }
    }
}
