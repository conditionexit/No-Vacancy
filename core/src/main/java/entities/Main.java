package entities;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * libGDX application adapter that coordinates the maze-escape game loop.
 *
 * <p>The adapter creates the map, player, enemies, HUD, rendering resources,
 * and camera. Every {@link #render()} call advances gameplay using libGDX
 * delta time, resolves enemy contact through {@link Player#takeDamage()},
 * resets a player who has exhausted all lives, tracks the player with a
 * clamped camera, and draws the scene in depth-aware row order. Ground is
 * rendered first; enemy and player sprites are interleaved with wall and
 * decor rows so foreground map tiles can cover actors.</p>
 *
 * <p>The shared enemy texture is owned and disposed here. {@link Maze},
 * {@link Player}, and {@link Hud} own their respective resources and are
 * released from {@link #dispose()}.</p>
 *
 * @author Fuad Jafarli
 * @implNote Originally developed by Fuad Jafarli; subsequently modified by
 *           Mahammad and Islam.
 * @see Maze
 * @see Player
 * @see Enemy
 */
public class Main extends ApplicationAdapter {
    OrthographicCamera camera = new OrthographicCamera();
    Viewport viewport = new FitViewport(640, 500, camera);
    SpriteBatch batch;

    Maze maze;
    Player player;
    Array<Enemy> enemies = new Array<>();
    Texture enemyTex;

    /**
     * Initializes game-owned resources after libGDX creates the application.
     *
     * <p>The method loads the TMX level, creates a player at its spawn,
     * creates one enemy for every enemy-spawn object, and creates the HUD.
     * Enemy spawn points are converted from map points to sprite bottom-left
     * coordinates by subtracting half the sprite size. Nearest filtering keeps
     * the shared enemy texture appropriate for pixel-art rendering.</p>
     */
    @Override
    public void create() {
        batch = new SpriteBatch();

        maze = new Maze("level1.tmx");
        if(!maze.isLoaded()){
            return;
        }
        player = new Player(maze);

        enemyTex = new Texture("enemy.png");
        enemyTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        for (Vector2 s : maze.enemySpawns) {
            enemies.add(new Enemy(
                s.x - Enemy.W / 2,
                s.y - Enemy.H / 2,
                enemyTex,
                maze
            ));
        }

        hud = new Hud(640, 500);
    }
    Hud  hud;
    /**
     * Updates the fit viewport when the application window changes size.
     *
     * <p>{@link FitViewport} maintains the 640 by 500 virtual-world aspect
     * ratio, adding letterboxing when necessary instead of stretching the
     * game view.</p>
     *
     * @param w new back-buffer width in pixels
     * @param h new back-buffer height in pixels
     */
    @Override
    public void resize(int w, int h) { viewport.update(w, h); }

    /**
     * Advances one frame of gameplay and renders the world and HUD.
     *
     * <p>All movement receives the frame delta time, making its speed largely
     * independent of frame rate. Enemy contact is measured using the existing
     * origin-to-origin distance threshold; {@link Player#takeDamage()} then
     * enforces the player's invincibility cooldown. A dead player is reset
     * immediately after interaction processing.</p>
     *
     * <p>Rendering clears the frame, renders ground, then iterates map rows
     * from top to bottom. Enemies and the player are inserted at their rows
     * before the walls/decor row is drawn. The HUD is drawn after the world so
     * it remains screen-facing and unobscured.</p>
     */
    @Override
    public void render() {
        // Clear every frame first, so a failed map load produces a black window.
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // create() returned before constructing player/enemies/HUD.
        if (maze == null || !maze.isLoaded()) {
            return;
        }

        float dt = Gdx.graphics.getDeltaTime();

        // update
        player.update(dt);

        // update
        for (Enemy e : enemies) {
            e.update(dt, player.x, player.y);
            if (e.distanceTo(player.x, player.y) < 24f) player.takeDamage();
        }
        if (player.isDead()) player.reset();

        updateCamera();

        maze.renderGround(camera);                                // floor

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        int feetRow = player.feetRow();
        for (int row = maze.rows() - 1; row >= 0; row--) {        // top to bottom
            for (Enemy e : enemies) {
                if (e.row(maze.tileHeight()) == row) e.draw(batch);
            }
            if (row == feetRow) player.draw(batch);               // player between rows
            maze.drawRow(batch, row);
        }
        batch.end();
        hud.draw(batch, player.getLives(), Player.MAX_LIVES);
    }

    /**
     * Centres the camera on the player as far as map bounds permit.
     *
     * <p>Clamping the target centre to half a viewport width/height from each
     * edge prevents empty space outside the maze from becoming visible while
     * still allowing normal tracking in the map interior.</p>
     */
    private void updateCamera() {
        camera.position.set(
            MathUtils.clamp(player.x + Player.W / 2, camera.viewportWidth / 2, maze.pixelWidth() - camera.viewportWidth / 2),
            MathUtils.clamp(player.y + Player.H / 2, camera.viewportHeight / 2, maze.pixelHeight() - camera.viewportHeight / 2), 0);
        camera.update();
    }

    /**
     * Releases all resources owned by the application and its game objects.
     *
     * <p>The enemy texture is disposed once here because every
     * {@link Enemy} shares it; enemies themselves do not own disposable
     * resources.</p>
     */
    @Override
    public void dispose() {
        // A failed map load returns early from create(), so some fields can be null.
        if (maze != null) maze.dispose();
        if (player != null) player.dispose();
        if (batch != null) batch.dispose();
        if (enemyTex != null) enemyTex.dispose();
        if (hud != null) hud.dispose();
    }


}
