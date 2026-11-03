package io.github.conditionexit.novacancy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/*

Main entry point for the No-Vacancy game.
 *
 * <p>This class manages the basic lifecycle of the game using libGDX.
 * It initializes the resources needed for rendering, draws the game
 * content every frame, and releases resources when the game closes.</p>
 *
 * <p>Currently, this class displays a sample image.
 * As development progresses, it will be integrated with the
 * game's actual screens and gameplay systems.</p>
 *
 * @author Mahammad
 * @author Fuad
 * @author Islam
 * @author Zarifa
 * @author Yusif
 * @version 0.1
 * @since 0.1
 * @see ApplicationAdapter

 */

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;

    /**
     * Initializes the game when the application starts.
     *
     * <p>Creates the SpriteBatch used for rendering and loads
     * the temporary image from the game's assets.</p>
     */

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
    }

    /**
     * Renders the game content once per frame.
     *
     * <p>Clears the screen, starts the rendering batch,
     * draws the temporary image, and finishes the batch.</p>
     */

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();
    }

    /**
     * Releases resources when the application closes.
     *
     * <p>Disposing of libGDX resources prevents native memory leaks.</p>
     */

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
