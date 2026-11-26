package entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.function.IntPredicate;

/**
 * Runs the gameplay screen and draws the maze, characters and HUD.
 * The enemy texture is shared and disposed by this screen.
 *
 * @author Fuad Jafarli
 * @implNote Modified by Mahammad and Islam; key and HUD feedback added by Yusif.
 * @implNote Potion collection and health restoration added by Mahammad.
 */
public class GameScreen extends ScreenAdapter {
    OrthographicCamera camera = new OrthographicCamera();
    Viewport viewport = new FitViewport(640, 500, camera);
    SpriteBatch batch;

    Maze maze;
    Player player;
    Array<Enemy> enemies = new Array<>();
    Texture enemyTex;
    Key key;
    Texture keyTexture;
    Potion potion;
    Texture potionTexture;
    private ShapeRenderer trapRenderer;
    private IntPredicate trapDamageReceiver;
    private boolean paused;
    private boolean applicationPaused;

    private final Main game;
    private final String levelFile;

    /** Creates a gameplay screen for the selected level. */
    public GameScreen(Main game, String levelFile) {
        this.game = game;
        this.levelFile = levelFile;
    }

    /** Loads the level and creates its game objects. */
    @Override
    public void show() {
        batch = new SpriteBatch();
        game.audio().playMusic();

        maze = new Maze(levelFile);
        if(!maze.isLoaded()){
            return;
        }

        player = new Player(maze);
        trapDamageReceiver = amount -> player.takeTrapHit();
        trapRenderer = new ShapeRenderer();

        if (maze.keySpawns.size > 0) {
            Vector2 spawn = maze.keySpawns.first();
            key = new Key(spawn.x, spawn.y);
            keyTexture = new Texture("key.png");
            keyTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }

        if(maze.potionSpawns.size > 0){
            Vector2 potionSpawn = maze.potionSpawns.first();
            potion = new Potion(potionSpawn.x, potionSpawn.y);
            potionTexture = new Texture("potions/health potion 64 x 64.png");
            potionTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }

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
    /** Updates the viewport on window resize. */
    @Override
    public void resize(int w, int h) { viewport.update(w, h); }

    /** Updates the game and draws one frame. */
    @Override
    public void render(float delta) {
        // Clear every frame first, so a failed map load produces a black window.
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMenu();
            return;
        }

        // create() returned before constructing player/enemies/HUD.
        if (maze == null || !maze.isLoaded()) {
            return;
        }

        float dt = Gdx.graphics.getDeltaTime();
        player.setSfxVolume(game.audio().sfxVolume());

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            paused = !paused;
            if (paused) player.stopFootsteps();
        }

        if (!paused && !applicationPaused) {
            updateSimulation(dt);
        }

        updateCamera();

        maze.renderGround(camera);

        renderTraps();
        renderWorld();

        hud.draw(batch, player.getLives(), Player.MAX_LIVES, key != null && key.isCollected());
    }

    /** Draws every trap as a solid red rectangle using the {@link ShapeRenderer}. */
    private void renderTraps(){
        if (!maze.traps.isEmpty()) {
            trapRenderer.setProjectionMatrix(camera.combined);
            trapRenderer.begin(ShapeRenderer.ShapeType.Filled);
            trapRenderer.setColor(0.85f, 0.15f, 0.1f, 1f);
            for (Trap trap : maze.traps) {
                trapRenderer.rect(trap.getX(), trap.getY(), trap.getWidth(), trap.getHeight());
            }
            trapRenderer.end();
        }
    }

    /**
     * Draws items, enemies, the player and wall/decor rows in depth order.
     *
     * <p>Rows are iterated top-to-bottom. Within each row, collectible items
     * and enemy sprites are drawn first, then the player at its feet row,
     * and finally the wall/decor layer so foreground tiles overlap actors.</p>
     */
    private void renderWorld(){
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        int feetRow = player.feetRow();
        for (int row = maze.rows() - 1; row >= 0; row--) {        // top to bottom
            if (key != null && !key.isCollected()
                && (int) (key.getY() / maze.tileHeight()) == row) {
                batch.draw(keyTexture, key.getX() - 12, key.getY() - 12, 24, 24);
            }

            if(potion != null && !potion.isConsumed()
                && (int) (potion.getY() / maze.tileHeight()) == row) {
                batch.draw(potionTexture, potion.getX() - 12, potion.getY() - 12, 24, 24);
            }

            for (Enemy e : enemies) {
                if (e.row(maze.tileHeight()) == row) e.draw(batch);
            }
            if (row == feetRow) player.draw(batch);               // player between rows
            maze.drawRow(batch, row);
        }

        batch.end();
    }

    /**
     * Runs one tick of game logic: player movement, enemy AI and contact
     * damage, trap hits, item collection and death/restart handling.
     *
     * @param dt frame delta time in seconds
     */
    private void updateSimulation(float dt){
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) restartLevel();
        player.update(dt);
        updateEnemies(dt);
        updateTraps(dt);
        collectItems();

        if (player.isDead()) restartLevel();
    }

    private void updateEnemies(float dt) {
        for (Enemy enemy : enemies) {
            enemy.update(dt, player.x, player.y);
            if (enemy.distanceTo(player.x, player.y) < 24f) player.takeDamage();
        }
    }

    private void updateTraps(float dt) {
        for (Trap trap : maze.traps) {
            trap.update(dt, player.x + Player.HITBOX_OFFSET_X, player.y + Player.HITBOX_OFFSET_Y,
                Player.HITBOX_WIDTH, Player.HITBOX_HEIGHT, trapDamageReceiver);
        }
    }

    /**
     * Resets the level to its initial state after a player death.
     *
     * <p>The player is respawned, any collected key or consumed potion is
     * dropped back to its spawn point, and all traps have their cooldowns
     * cleared. Null checks guard levels that have no key or potion.</p>
     */
    private void restartLevel() {
        player.reset();
        if (key != null) key.drop();        // levels are not required to contain a key or potion
        if (potion != null) potion.drop();
        for (Trap trap : maze.traps) trap.reset();
    }

    /** Flags the game as application-paused and silences any looping audio. */
    @Override
    public void pause() {
        applicationPaused = true;
        if (player != null) player.stopFootsteps();
    }

    /** Clears the application-paused flag so simulation resumes on the next frame. */
    @Override
    public void resume() {
        applicationPaused = false;
    }


    @Override
    public void hide() {
        if (player != null) player.stopFootsteps();
    }

    /** Keeps the camera on the player within the maze bounds. */
    private void updateCamera() {
        camera.position.set(
            MathUtils.clamp(player.x + Player.W / 2, camera.viewportWidth / 2, maze.pixelWidth() - camera.viewportWidth / 2),
            MathUtils.clamp(player.y + Player.H / 2, camera.viewportHeight / 2, maze.pixelHeight() - camera.viewportHeight / 2), 0);
        camera.update();
    }

    private void collectItems() {
        if (key != null && !key.isCollected() && isNearPlayer(key.getX(), key.getY())) {
            key.collect();
        }
        if (potion != null && !potion.isConsumed() && isNearPlayer(potion.getX(), potion.getY())
            && player.getLives() < Player.MAX_LIVES) {
            potion.consume();
            player.heal();
        }
    }

    private boolean isNearPlayer(float x, float y) {
        float dx = player.x + Player.W / 2 - x;
        float dy = player.y + Player.H / 2 - y;
        return dx * dx + dy * dy < 24f * 24f;
    }

    /**
     * The enemy texture is disposed once here because every {@link Enemy}
     * shares it; enemies themselves do not own disposable resources.
     */
    @Override
    public void dispose() {
        // A failed map load returns early from create(), so some fields can be null.
        if (maze != null) maze.dispose();
        if (player != null) player.dispose();
        if (batch != null) batch.dispose();
        if (enemyTex != null) enemyTex.dispose();
        if (keyTexture != null) keyTexture.dispose();
        if (hud != null) hud.dispose();
        if (trapRenderer != null) trapRenderer.dispose();
        if(potionTexture != null) potionTexture.dispose();
    }

}
