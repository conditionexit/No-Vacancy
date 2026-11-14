package io.github.conditionext.novacancy;

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

public class GameMap {
    TiledMap map;
    TiledMapTileLayer walls, decor;
    int ground;
    Array<Vector2> enemySpawns = new Array<>();
    Vector2 playerSpawn = new Vector2();

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

    public TiledMap getTiledMap() { return map; }
    public TiledMapTileLayer getWalls() { return walls; }
    public TiledMapTileLayer getDecor() { return decor; }
    public int getGroundIndex() { return ground; }
    public Array<Vector2> getEnemySpawns() { return enemySpawns; }
    public Vector2 getPlayerSpawn() { return playerSpawn; }
    public float getMapWidth() { return walls.getWidth() * walls.getTileWidth(); }
    public float getMapHeight() { return walls.getHeight() * walls.getTileHeight(); }
    public float getTileHeight() { return walls.getTileHeight(); }

    public void dispose() { map.dispose(); }
}
