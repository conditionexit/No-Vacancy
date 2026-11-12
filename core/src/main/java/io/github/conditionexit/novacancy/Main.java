package io.github.conditionext.novacancy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Main extends ApplicationAdapter {
    static final float SIZE = 24f;    // player size in pixels (keep smaller than a tile)
    static final float SPEED = 150f;  // pixels per second

    TiledMap map;
    OrthogonalTiledMapRenderer renderer;
    OrthographicCamera camera;
    SpriteBatch batch;
    Array<Rectangle> solids;
    Texture playerTex;
    TiledMapTileLayer walls;
    float tileW, tileH;
    float x, y; // player position

    @Override
    public void create() {
        map = new TmxMapLoader().load("maps/level1.tmx");
        solids = new Array<>();
        renderer = new OrthogonalTiledMapRenderer(map);
        batch = new SpriteBatch();
        playerTex = new Texture("player.png");
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 640, 480);

        for (MapObject o : map.getLayers().get("collision").getObjects()) {
            if (o instanceof RectangleMapObject) {
                solids.add(((RectangleMapObject) o).getRectangle());
            }
        }

        // Spawn at the "start" object from the objects layer
        for (MapObject obj : map.getLayers().get("objects").getObjects()) {
            if ("start".equals(obj.getName())) {
                x = obj.getProperties().get("x", Float.class);
                y = obj.getProperties().get("y", Float.class);
            }
        }
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy += SPEED * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy -= SPEED * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx -= SPEED * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx += SPEED * dt;

        // Move each axis separately so you can slide along walls
        if (!blocked(x + dx, y)) x += dx;
        if (!blocked(x, y + dy)) y += dy;

        camera.position.set(x + SIZE / 2, y + SIZE / 2, 0);
        camera.update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.setView(camera);
        renderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(playerTex, x, y, SIZE, SIZE);
        batch.end();
    }

    private boolean blocked(float px, float py) {
        Rectangle player = new Rectangle(px, py, SIZE, SIZE);
        for (Rectangle s : solids) {
            if (player.overlaps(s)) return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        batch.dispose();
        playerTex.dispose();
    }
}
