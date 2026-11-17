package entities;

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

/**
 * Loads, owns, renders, and queries one TMX maze level.
 *
 * <p>The map supplies named {@code ground}, {@code walls}, {@code decor}, and
 * {@code objects} layers. Ground is rendered by the tiled-map renderer before
 * sprites, while walls and decor are drawn one row at a time by
 * {@link #drawRow(SpriteBatch, int)}. This ordering lets {@link Main} place
 * players and enemies between rows, which gives tall wall tiles a natural
 * foreground effect. The objects layer contributes player and enemy spawn
 * locations in the TMX file's world-pixel coordinate system.</p>
 *
 * <p>This class owns the loaded {@link TiledMap} and its renderer. Call
 * {@link #dispose()} once the level is no longer needed.</p>
 *
 * @author Fuad Jafarli
 * @see Player
 * @see Enemy
 */
public class Maze {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final TiledMapTileLayer walls, decor;
    private final int[] groundLayers;

    /** Raw player-spawn object position, before a sprite-specific offset. */
    public final Vector2 playerSpawn = new Vector2();
    public final Array<Vector2> enemySpawns = new Array<>();

    /**
     * Loads a TMX level and extracts its rendering layers and spawn objects.
     *
     * <p>Nearest-neighbour texture filtering preserves crisp pixel-art edges.
     * Objects whose type is {@code enemy} become enemy spawns; the object
     * named {@code player_spawn} determines {@link #playerSpawn}. The map is
     * expected to contain the layers named {@code ground}, {@code walls},
     * {@code decor}, and {@code objects}, plus any tilesets requested through
     * {@link #animation(String, int)}.</p>
     *
     * @param tmxFile internal asset path to the TMX map
     */
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

    /** @return total map width in world pixels. */
    public float pixelWidth()  { return walls.getWidth()  * walls.getTileWidth(); }
    /** @return total map height in world pixels. */
    public float pixelHeight() { return walls.getHeight() * walls.getTileHeight(); }
    /** @return number of tile rows in the wall layer. */
    public int rows()          { return walls.getHeight(); }
    /** @return height of one wall-layer tile in world pixels. */
    public float tileHeight()  { return walls.getTileHeight(); }

    // ---------- collision ----------

    /**
     * Tests point collision against the wall layer.
     *
     * <p>World coordinates are converted to tile coordinates by tile size.
     * Points outside map bounds count as solid so callers cannot walk beyond
     * the level merely because no wall cell exists there.</p>
     *
     * @param wx world x-coordinate in pixels
     * @param wy world y-coordinate in pixels
     * @return {@code true} when the point is outside the map or lies in a wall
     *         cell
     */
    public boolean isSolid(float wx, float wy) {
        int tx = (int) (wx / walls.getTileWidth());
        int ty = (int) (wy / walls.getTileHeight());
        if (wx < 0 || wy < 0 || tx >= walls.getWidth() || ty >= walls.getHeight()) return true;
        return walls.getCell(tx, ty) != null;
    }

    /**
     * Tests whether an axis-aligned rectangular hitbox is completely within
     * the maze and intersects no wall tiles.
     *
     * <p>Rather than sampling every pixel, this method examines only the
     * inclusive range of tiles overlapped by the rectangle. The tiny offset on
     * the right and top edges avoids treating a rectangle ending exactly on a
     * tile boundary as overlapping the adjacent tile. This is both sufficient
     * for tile collision and cheaper than per-pixel testing.</p>
     *
     * @param x hitbox left x-coordinate in world pixels
     * @param y hitbox bottom y-coordinate in world pixels
     * @param width hitbox width; must be positive
     * @param height hitbox height; must be positive
     * @return {@code true} only if the complete hitbox is in map bounds and
     *         covers no wall cell
     */
    public boolean isAreaFree(float x, float y, float width, float height) {
        if (width <= 0 || height <= 0) return false;

        float tw = walls.getTileWidth();
        float th = walls.getTileHeight();

        // Reject rectangles outside map boundaries.
        if (x < 0 || y < 0 ||
            x + width > pixelWidth() ||
            y + height > pixelHeight()) {
            return false;
        }

        int minCol = (int) Math.floor(x / tw);
        int maxCol = (int) Math.floor((x + width - 0.001f) / tw);
        int minRow = (int) Math.floor(y / th);
        int maxRow = (int) Math.floor((y + height - 0.001f) / th);

        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                if (walls.getCell(col, row) != null) {
                    return false;
                }
            }
        }

        return true;
    }

    // ---------- drawing ----------

    /**
     * Renders the ground tile layers behind sprites.
     *
     * <p>This uses the map renderer rather than the application's active
     * {@link SpriteBatch}, so it must be called before the sprite batch begins.</p>
     *
     * @param camera camera defining the visible map region
     */
    public void renderGround(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.render(groundLayers);
    }

    /**
     * Draws one wall row followed by the matching decor row.
     *
     * <p>{@link Main} calls this while iterating rows from top to bottom and
     * inserts sprites at their corresponding rows. Drawing these opaque or
     * foreground tiles after a sprite in the same row allows them to occlude
     * the sprite where appropriate.</p>
     *
     * @param batch active sprite batch
     * @param row zero-based tile row to draw
     */
    public void drawRow(SpriteBatch batch, int row) {
        drawLayerRow(batch, walls, row);
        drawLayerRow(batch, decor, row);
    }

    /**
     * Draws every occupied cell in one row of a tile layer.
     *
     * @param batch active sprite batch
     * @param layer tile layer to inspect
     * @param row zero-based row in {@code layer}
     */
    private void drawLayerRow(SpriteBatch batch, TiledMapTileLayer layer, int row) {
        float tw = layer.getTileWidth(), th = layer.getTileHeight();
        for (int col = 0; col < layer.getWidth(); col++) {
            TiledMapTileLayer.Cell cell = layer.getCell(col, row);
            if (cell != null) batch.draw(cell.getTile().getTextureRegion(), col * tw, row * th);
        }
    }

    // ---------- animated tiles ----------

    /**
     * Builds a looping libGDX animation from an animated tile in a named
     * tileset.
     *
     * <p>The TMX tile's frame intervals are stored in milliseconds, whereas
     * {@link Animation} expects seconds, so the first interval is converted.
     * This assumes the source animated tile supplies at least one frame and
     * one interval, as produced by the game's tileset.</p>
     *
     * @param tilesetName name of the TMX tileset containing the animated tile
     * @param localId tile identifier relative to that tileset
     * @return looping animation containing the tile's frame texture regions
     */
    public Animation<TextureRegion> animation(String tilesetName, int localId) {
        TiledMapTileSet ts = map.getTileSets().getTileSet(tilesetName);
        int firstGid = ts.getProperties().get("firstgid", Integer.class);
        AnimatedTiledMapTile tile = (AnimatedTiledMapTile) ts.getTile(firstGid + localId);

        Array<TextureRegion> regions = new Array<>();
        for (StaticTiledMapTile f : tile.getFrameTiles()) regions.add(f.getTextureRegion());

        float frameTime = tile.getAnimationIntervals()[0] / 1000f;
        return new Animation<>(frameTime, regions, Animation.PlayMode.LOOP);
    }

    /**
     * Releases the TMX map and the map renderer owned by this maze.
     */
    public void dispose() {
        map.dispose();
        renderer.dispose();
    }
}
