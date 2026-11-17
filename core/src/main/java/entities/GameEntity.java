package entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.awt.*;

/**
 * Represents the base class for all game entities.
 *
 * Stores the position shared by players, enemies,
 * keys, traps, and other game objects.
 *
 * @author Mahammad Jafarli
 * @upgraded Fuad Jafarli
 *
 */
public abstract class GameEntity {
    // coordinates of the game entity
    private float x;
    private float y;
    private float width;
    private float height;

    /**
     * Creates an entity at the specified position.
     *
     * @param x      world x of the bottom-left corner
     * @param y      world y of the bottom-left corner
     * @param width  entity width in world units
     * @param height entity height in world units
     */

    protected GameEntity(float x, float y, float width, float height) { // GameEntity is abstract, its constructor is intended to be called by subclasses through super(...).
        this.x = x;
        this.y = y;
        this.height=height;
        this.width=width;
    }

    /**
     * Advances this object's logic by one frame.
     *
     * @param dt seconds since the previous frame
     */
    public abstract void update(float dt);

    /**
     * Returns the rectangle used for collisions.
     *
     * @return a new rectangle covering this object
     */
    public abstract void draw(SpriteBatch batch);

    /**
     * Returns the tile row of this object's feet. Used to draw objects in the correct order between wall rows.
     *
     * @param tileHeight height of one tile in world units
     * @return the row index
     */
    public int getRow(float tileHeight) {
        return (int) (y / tileHeight);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() { return width; }

    public float getHeight() { return height; }

    protected final void setPosition(float x, float y){
        this.x = x;
        this.y = y;
    }

    //We want subclasses such as Player and Enemy to control their positions. Other unrelated classes shouldn't be able to teleport an entity whenever they want.
    //final prevents subclasses from overriding this basic position-changing operation unexpectedly.

}
