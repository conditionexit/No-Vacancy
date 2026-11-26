package entities;

/**
 * Draws the current spike frame for each visible trap.
 * Hidden traps return a frame index of -1 and are skipped.
 *
 * @author Zarifa
 */

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TrapRenderer {
    private static final int FRAMES = 4;

    private final Texture sheet = new Texture("trap_spikes.png");
    private final TextureRegion[] frames;

    public TrapRenderer() {
        TextureRegion[][] split = TextureRegion.split(sheet, 32, 32);
        frames = new TextureRegion[FRAMES];
        System.arraycopy(split[0], 0, frames, 0, FRAMES);
    }

    public void draw(SpriteBatch batch, Iterable<Trap> traps) {
        for (Trap trap : traps) {
            int frame = trap.frameIndex();
            if (frame < 0) continue;

            batch.draw(frames[frame], trap.getX(), trap.getY(),
                trap.getWidth(), trap.getHeight());
        }
    }

    public void dispose() {
        sheet.dispose();
    }
}
