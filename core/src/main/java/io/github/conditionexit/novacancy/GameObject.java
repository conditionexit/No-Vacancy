package io.github.conditionexit.novacancy;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class GameObject {
    protected float x, y;                                 // sprite bottom-left
    protected final float width, height;

    protected GameObject(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void update(float dt);

    public abstract void draw(SpriteBatch batch);

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean overlaps(GameObject other) {
        return getBounds().overlaps(other.getBounds());
    }

    public int getRow(float tileHeight) {
        return (int) (y / tileHeight);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
}
