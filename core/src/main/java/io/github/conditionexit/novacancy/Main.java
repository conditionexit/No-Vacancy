package io.github.conditionext.novacancy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {
    io.github.conditionext.novacancy.GameMap map;
    OrthogonalTiledMapRenderer renderer;
    OrthographicCamera camera = new OrthographicCamera();
    Viewport viewport = new FitViewport(640, 500, camera);
    SpriteBatch batch;
    Player player;
    Array<Enemy> enemies = new Array<>();
    Texture enemyTex;

    @Override
    public void create() {
        map = new GameMap("level1.tmx");

        renderer = new OrthogonalTiledMapRenderer(map.getTiledMap());
        batch = new SpriteBatch();

        Vector2 ps = map.getPlayerSpawn();
        player = new Player(map, ps.x - Player.W / 2, ps.y);

        enemyTex=new Texture("enemy.png");
        enemyTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        for (Vector2 s : map.getEnemySpawns()) {
            enemies.add(new Enemy(s.x - Enemy.EW / 2, s.y - Enemy.EH / 2, player, enemyTex));
        }
    }

    @Override
    public void resize(int w, int h) { viewport.update(w, h); }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        for (Enemy e : enemies) e.update(dt);
        player.update(dt);

        // camera
        float mapW = map.getMapWidth();
        float mapH = map.getMapHeight();
        camera.position.set(
            MathUtils.clamp(player.getX() + Player.W / 2, camera.viewportWidth / 2, mapW - camera.viewportWidth / 2),
            MathUtils.clamp(player.getY() + Player.H / 2, camera.viewportHeight / 2, mapH - camera.viewportHeight / 2), 0);
        camera.update();

        // draw
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.setView(camera);
        renderer.render(new int[]{map.getGroundIndex()});         // floor

        TiledMapTileLayer walls = map.getWalls();
        TiledMapTileLayer decor = map.getDecor();
        float th = map.getTileHeight();
        int feetRow = player.getRow(th);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (int row = walls.getHeight() - 1; row >= 0; row--) {  // top to bottom
            for (Enemy e : enemies)
                if (e.getRow(th) == row)
                    e.draw(batch);
            if (row == feetRow) player.draw(batch);               // player between rows
            drawRow(walls, row);
            drawRow(decor, row);
        }
        batch.end();
    }

    void drawRow(TiledMapTileLayer layer, int row) {
        float tw = layer.getTileWidth(), th = layer.getTileHeight();
        for (int col = 0; col < layer.getWidth(); col++) {
            TiledMapTileLayer.Cell cell = layer.getCell(col, row);
            if (cell != null) batch.draw(cell.getTile().getTextureRegion(), col * tw, row * th);
        }
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        batch.dispose();
        enemyTex.dispose();
    }
}
