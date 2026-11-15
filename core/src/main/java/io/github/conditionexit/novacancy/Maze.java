package io.github.conditionext.novacancy;

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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/** The level: Tiled map, rendering of its layers, wall collision, and spawn points. */
public class Maze {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final TiledMapTileLayer walls, decor;
    private final int[] groundLayers;

    /** Raw object positions from the "objects" layer (not yet offset by sprite size). */
    public final Vector2 playerSpawn = new Vector2();
    public final Array<Vector2> enemySpawns = new Array<>();

    public Maze(String tmxFile) {
        TmxMapLoader.Parameters p = new TmxMapLoader.Parameters();
        p.textureMinFilter = Texture.TextureFilter.Nearest;
        p.textureMagFilter = Texture.TextureFilter.Nearest;
        map = new TmxMapLoader().load(tmxFile, p);

        renderer = new OrthogonalTiledMapRenderer(map);
        walls = (TiledMapTileLayer) map.getLayers().get("walls");
        decor = (TiledMapTileLayer) map.getLayers().get("decor");
        groundLayers = new int[]{ map.getLayers().getIndex("ground") };

        for (MapObject o : map.getLayers().get("objects").getObjects()) {
            float ox = o.getProperties().get("x", Float.class);
            float oy = o.getProperties().get("y", Float.class);
            String type = o.getProperties().get("type", String.class);

            if ("enemy".equals(type)) enemySpawns.add(new Vector2(ox, oy));
            if ("player_spawn".equals(o.getName())) playerSpawn.set(ox, oy);
        }
    }

    // ---------- size ----------

    public float pixelWidth()  { return walls.getWidth()  * walls.getTileWidth(); }
    public float pixelHeight() { return walls.getHeight() * walls.getTileHeight(); }
    public int rows()          { return walls.getHeight(); }
    public float tileHeight()  { return walls.getTileHeight(); }

    // ---------- collision ----------

    /** True if the world point is inside a wall tile or outside the map. */
    public boolean isSolid(float wx, float wy) {
        int tx = (int) (wx / walls.getTileWidth());
        int ty = (int) (wy / walls.getTileHeight());
        if (wx < 0 || wy < 0 || tx >= walls.getWidth() || ty >= walls.getHeight()) return true;
        return walls.getCell(tx, ty) != null;
    }

    // ---------- drawing ----------

    /** Draws the floor. Call before batch.begin(). */
    public void renderGround(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.render(groundLayers);
    }

    /** Draws one row of walls and decor. Call between batch.begin() and batch.end(). */
    public void drawRow(SpriteBatch batch, int row) {
        drawLayerRow(batch, walls, row);
        drawLayerRow(batch, decor, row);
    }

    private void drawLayerRow(SpriteBatch batch, TiledMapTileLayer layer, int row) {
        float tw = layer.getTileWidth(), th = layer.getTileHeight();
        for (int col = 0; col < layer.getWidth(); col++) {
            TiledMapTileLayer.Cell cell = layer.getCell(col, row);
            if (cell != null) batch.draw(cell.getTile().getTextureRegion(), col * tw, row * th);
        }
    }

    // ---------- animated tiles ----------

    /** Builds an Animation from an animated tile in the given tileset. */
    public Animation<TextureRegion> animation(String tilesetName, int localId) {
        TiledMapTileSet ts = map.getTileSets().getTileSet(tilesetName);
        int firstGid = ts.getProperties().get("firstgid", Integer.class);
        AnimatedTiledMapTile tile = (AnimatedTiledMapTile) ts.getTile(firstGid + localId);

        Array<TextureRegion> regions = new Array<>();
        for (StaticTiledMapTile f : tile.getFrameTiles()) regions.add(f.getTextureRegion());

        float frameTime = tile.getAnimationIntervals()[0] / 1000f;
        return new Animation<>(frameTime, regions, Animation.PlayMode.LOOP);
    }

    public void dispose() {
        map.dispose();
        renderer.dispose();
    }
}
