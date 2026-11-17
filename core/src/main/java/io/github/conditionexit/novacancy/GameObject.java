package io.github.conditionexit.novacancy;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Base class for everything that exists in the maze and has a position and size:
 * the player, enemies, traps, keys and the exit. Subclasses supply their own update logic and drawing.
 * @author Fuad
 */

public abstract class GameObject {
    protected float x, y;
    protected final float width, height;

    /**
     * Creates a game object.
     *
     * @param x      world x of the bottom-left corner
     * @param y      world y of the bottom-left corner
     * @param width  object width in world units
     * @param height object height in world units
     * @author Fuad
     */

    protected GameObject(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Advances this object's logic by one frame.
     *
     * @param dt seconds since the previous frame
     * @author Fuad
     */
    public abstract void update(float dt);

    /**
     * Draws this object. The caller must already have called {@code batch.begin()}.
     *
     * @param batch the batch to draw with
     * @author Fuad
     */
    public abstract void draw(SpriteBatch batch);

    /**
     * Returns the rectangle used for collisions.
     *
     * @return a new rectangle covering this object
     * @author Fuad
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Returns the tile row of this object's feet. Used to draw objects in the correct order between wall rows.
     *
     * @param tileHeight height of one tile in world units
     * @return the row index
     * @author Fuad
     */
    public int getRow(float tileHeight) {
        return (int) (y / tileHeight);
    }

    /** @return world x of the bottom-left corner */
    public float getX() { return x; }

    /** @return world y of the bottom-left corner */
    public float getY() { return y; }

    /** @return the object's width */
    public float getWidth() { return width; }

    /** @return the object's height */
    public float getHeight() { return height; }
}
