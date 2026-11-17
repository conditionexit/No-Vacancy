package io.github.conditionext.novacancy;

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

public class Main extends ApplicationAdapter {
    OrthographicCamera camera = new OrthographicCamera();
    Viewport viewport = new FitViewport(640, 500, camera);
    SpriteBatch batch;

    Maze maze;
    Player player;
    Array<Enemy> enemies = new Array<>();
    Texture enemyTex;

    @Override
    public void create() {
        batch = new SpriteBatch();

        maze = new Maze("level1.tmx");
        player = new Player(maze);

        enemyTex = new Texture("enemy.png");
        enemyTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        for (Vector2 s : maze.enemySpawns) {
            enemies.add(new Enemy(s.x - Enemy.W / 2, s.y - Enemy.H / 2, enemyTex));
        }

        hud = new Hud(640, 500);
    }
    Hud  hud;
    @Override
    public void resize(int w, int h) { viewport.update(w, h); }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        // update
        player.update(dt);
        for (Enemy e : enemies) {
            e.update(dt, player.x, player.y);
            if (e.distanceTo(player.x, player.y) < 24f) player.takeDamage();
        }
        if (player.isDead()) player.reset();
        updateCamera();


        // draw
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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

    private void updateCamera() {
        camera.position.set(
            MathUtils.clamp(player.x + Player.W / 2, camera.viewportWidth / 2, maze.pixelWidth() - camera.viewportWidth / 2),
            MathUtils.clamp(player.y + Player.H / 2, camera.viewportHeight / 2, maze.pixelHeight() - camera.viewportHeight / 2), 0);
        camera.update();
    }

    @Override
    public void dispose() {
        maze.dispose();
        player.dispose();
        batch.dispose();
        enemyTex.dispose();
        hud.dispose();
    }


}
