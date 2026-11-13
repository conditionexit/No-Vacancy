package io.github.conditionext.novacancy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {
    static final float SPEED = 250f;
    static final float W = 32f, H = 66f;                  // sprite size
    static final float EW=32f, EH=58f;
    static final float HB_X = 6f, HB_W = 20f, HB_H = 60f; // hitbox
    static class Enemy{
        float x,y;
        float sx,sy;
        Enemy(float x,float y){
            this.x=x;
            this.y=y;
            this.sx=x;
            this.sy=y;
        }
    }


    TiledMap map;
    OrthogonalTiledMapRenderer renderer;
    OrthographicCamera camera = new OrthographicCamera();
    Viewport viewport = new FitViewport(640, 500, camera);
    SpriteBatch batch;
    TiledMapTileLayer walls, decor;
    int ground;
    float x, y;                                           // sprite bottom-left
    Array<Enemy> enemies = new Array<>();
    Texture enemyTex;

    Animation<TextureRegion>[] walk = new Animation[4];   // 0 down, 1 left, 2 right, 3 up
    float stateTime;
    int dir = 0;
    boolean moving;

    @Override
    public void create() {
        TmxMapLoader.Parameters p = new TmxMapLoader.Parameters();
        p.textureMinFilter = Texture.TextureFilter.Nearest;
        p.textureMagFilter = Texture.TextureFilter.Nearest;
        map = new TmxMapLoader().load("level1.tmx", p);

        renderer = new OrthogonalTiledMapRenderer(map);
        batch = new SpriteBatch();

        walk[0] = tiledAnim("player", 3);    // down
        walk[1] = tiledAnim("player", 7);    // left
        walk[2] = tiledAnim("player", 11);   // right
        walk[3] = tiledAnim("player", 15);   // up

        walls = (TiledMapTileLayer) map.getLayers().get("walls");
        decor = (TiledMapTileLayer) map.getLayers().get("decor");
        ground = map.getLayers().getIndex("ground");

        enemyTex=new Texture("enemy.png");
        enemyTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        for (MapObject o : map.getLayers().get("objects").getObjects()) {
            float ox = o.getProperties().get("x", Float.class);
            float oy = o.getProperties().get("y", Float.class);
            String type = o.getProperties().get("type", String.class);
            if ("enemy".equals(type)) {
                enemies.add(new Enemy(ox - EW / 2, oy - EH / 2));
            }
        }


        for (MapObject o : map.getLayers().get("objects").getObjects()) {
            if ("player_spawn".equals(o.getName())) {
                x = o.getProperties().get("x", Float.class) - W / 2;
                y = o.getProperties().get("y", Float.class);
            }
        }
    }

    @Override
    public void resize(int w, int h) { viewport.update(w, h); }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        // input
        float dx = (Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0);
        float dy = (Gdx.input.isKeyPressed(Input.Keys.W) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.S) ? 1 : 0);
        moving = dx != 0 || dy != 0;

        // facing direction + animation time
        if (Gdx.input.isKeyPressed(Input.Keys.W))      dir = 3;
        else if (Gdx.input.isKeyPressed(Input.Keys.S)) dir = 0;
        else if (Gdx.input.isKeyPressed(Input.Keys.A)) dir = 1;
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) dir = 2;

        if (moving) stateTime += dt; else stateTime = 0;

        for (Enemy e : enemies) {
            float px = x - e.x, py = y - e.y;
            float dPlayer = (float) Math.sqrt(px*px+py*py);
            if (dPlayer < 200f && dPlayer>4f) {
                    e.x += px / dPlayer * 60f * dt;
                    e.y += py / dPlayer * 60f * dt;
            }
            else {
                float hx = e.sx - e.x, hy = e.sy - e.y;
                float dHome = (float) Math.sqrt(hx * hx + hy * hy);
                if (dHome > 2f) {
                    e.x += hx / dHome * 60f * dt;
                    e.y += hy / dHome * 60f * dt;
                }
            }

        }

        // movement (separate axes = slide along walls)
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) { dx = dx / len * SPEED * dt; dy = dy / len * SPEED * dt; }
        if (free(x + dx, y)) x += dx;
        if (free(x, y + dy)) y += dy;

        // camera
        float mapW = walls.getWidth() * walls.getTileWidth();
        float mapH = walls.getHeight() * walls.getTileHeight();
        camera.position.set(
            MathUtils.clamp(x + W / 2, camera.viewportWidth / 2, mapW - camera.viewportWidth / 2),
            MathUtils.clamp(y + H / 2, camera.viewportHeight / 2, mapH - camera.viewportHeight / 2), 0);
        camera.update();

        // draw
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.setView(camera);
        renderer.render(new int[]{ground});                       // floor

        TextureRegion frame = walk[dir].getKeyFrame(stateTime);
        int feetRow = (int) (y / walls.getTileHeight());

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (int row = walls.getHeight() - 1; row >= 0; row--) {  // top to bottom
            for (Enemy e : enemies)
                if ((int) (e.y / walls.getTileHeight()) == row)
                    batch.draw(enemyTex, e.x, e.y, EW, EH);
            if (row == feetRow) batch.draw(frame, x, y, W, H);    // player between rows
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

    // true if the hitbox at (px, py) touches no wall tile
    boolean free(float px, float py) {
        return !solid(px + HB_X, py) && !solid(px + HB_X + HB_W, py)
            && !solid(px + HB_X, py + HB_H) && !solid(px + HB_X + HB_W, py + HB_H);
    }

    boolean solid(float wx, float wy) {
        int tx = (int) (wx / walls.getTileWidth());
        int ty = (int) (wy / walls.getTileHeight());
        if (wx < 0 || wy < 0 || tx >= walls.getWidth() || ty >= walls.getHeight()) return true;
        return walls.getCell(tx, ty) != null;
    }

    Animation<TextureRegion> tiledAnim(String tilesetName, int localId) {
        TiledMapTileSet ts = map.getTileSets().getTileSet(tilesetName);
        int firstGid = ts.getProperties().get("firstgid", Integer.class);
        AnimatedTiledMapTile tile = (AnimatedTiledMapTile) ts.getTile(firstGid + localId);

        Array<TextureRegion> regions = new Array<>();
        for (StaticTiledMapTile f : tile.getFrameTiles()) regions.add(f.getTextureRegion());

        float frameTime = tile.getAnimationIntervals()[0] / 1000f;
        return new Animation<>(frameTime, regions, Animation.PlayMode.LOOP);
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        batch.dispose();
        enemyTex.dispose();
    }
}
