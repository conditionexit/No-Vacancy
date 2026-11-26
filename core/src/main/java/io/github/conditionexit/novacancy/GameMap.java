package io.github.conditionexit.novacancy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Loads the tiled map and exposes its layers, spawn points, and tile animation
 * data to game objects.
 *
 * <p>This is the earlier map helper used by the original game implementation.
 * The loaded map owns its tile textures and should be released with
 * {@link #dispose()}.</p>
 * @author Fuad
 *
 * @see GameObject
 */
public class GameMap {
    TiledMap map;
    TiledMapTileLayer walls, decor;
    int ground;
    Array<Vector2> enemySpawns = new Array<>();
    Vector2 playerSpawn = new Vector2();

    /**
     * Loads a TMX map and reads its enemy and player spawn objects.
     *
     * @param file internal asset path to the TMX map
     */
    public GameMap(String file) {
        TmxMapLoader.Parameters p = new TmxMapLoader.Parameters();
        p.textureMinFilter = Texture.TextureFilter.Nearest;
        p.textureMagFilter = Texture.TextureFilter.Nearest;
        map = new TmxMapLoader().load(file, p);

        walls = (TiledMapTileLayer) map.getLayers().get("walls");
        decor = (TiledMapTileLayer) map.getLayers().get("decor");
        ground = map.getLayers().getIndex("ground");

        for (MapObject o : map.getLayers().get("objects").getObjects()) {
            float ox = o.getProperties().get("x", Float.class);
            float oy = o.getProperties().get("y", Float.class);
            String type = o.getProperties().get("type", String.class);
            if ("enemy".equals(type)) {
                enemySpawns.add(new Vector2(ox, oy));
            }
        }

        for (MapObject o : map.getLayers().get("objects").getObjects()) {
            if ("player_spawn".equals(o.getName())) {
                playerSpawn.set(o.getProperties().get("x", Float.class),
                    o.getProperties().get("y", Float.class));
            }
        }
    }

    /**
     * Checks whether a world point lies outside the map or in a wall tile.
     *
     * @param wx world x-coordinate in pixels
     * @param wy world y-coordinate in pixels
     * @return {@code true} when the point is outside the map or inside a wall
     */
    boolean solid(float wx, float wy) {
        int tx = (int) (wx / walls.getTileWidth());
        int ty = (int) (wy / walls.getTileHeight());
        if (wx < 0 || wy < 0 || tx >= walls.getWidth() || ty >= walls.getHeight()) return true;
        return walls.getCell(tx, ty) != null;
    }

    /**
     * Builds a looping animation from an animated tile in a named tileset.
     *
     * @param tilesetName name of the tileset containing the animation
     * @param localId tile id relative to that tileset
     * @return looping animation using the tile's frames and frame timing
     */
    Animation<TextureRegion> tiledAnim(String tilesetName, int localId) {
        TiledMapTileSet ts = map.getTileSets().getTileSet(tilesetName);
        int firstGid = ts.getProperties().get("firstgid", Integer.class);
        AnimatedTiledMapTile tile = (AnimatedTiledMapTile) ts.getTile(firstGid + localId);

        Array<TextureRegion> regions = new Array<>();
        for (StaticTiledMapTile f : tile.getFrameTiles()) regions.add(f.getTextureRegion());

        float frameTime = tile.getAnimationIntervals()[0] / 1000f;
        return new Animation<>(frameTime, regions, Animation.PlayMode.LOOP);
    }

    /** @return the loaded tiled map */
    public TiledMap getTiledMap() { return map; }
    /** @return the map's wall layer */
    public TiledMapTileLayer getWalls() { return walls; }
    /** @return the map's decor layer */
    public TiledMapTileLayer getDecor() { return decor; }
    /** @return index of the ground layer */
    public int getGroundIndex() { return ground; }
    /** @return enemy spawn positions in world pixels */
    public Array<Vector2> getEnemySpawns() { return enemySpawns; }
    /** @return player spawn position in world pixels */
    public Vector2 getPlayerSpawn() { return playerSpawn; }
    /** @return total map width in world pixels */
    public float getMapWidth() { return walls.getWidth() * walls.getTileWidth(); }
    /** @return total map height in world pixels */
    public float getMapHeight() { return walls.getHeight() * walls.getTileHeight(); }
    /** @return height of one map tile in world pixels */
    public float getTileHeight() { return walls.getTileHeight(); }

    /** Releases the tiled map and its owned resources. */
    public void dispose() { map.dispose(); }
}
