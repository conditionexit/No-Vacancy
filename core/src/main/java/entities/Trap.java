package entities;

import java.util.Objects;
import java.util.function.IntPredicate;

/** A stationary rectangular hazard, independent of hearts, HP, input and rendering. */
public final class Trap {
    public static final int DEFAULT_DAMAGE = 10;
    public static final float DEFAULT_COOLDOWN = 1f;

    private final float x, y, width, height;
    private final int damage;
    private final float cooldown;
    private double remaining;
    private boolean touching;

    /** Preserves the original constructor; its collision size defaults to one 32-pixel tile. */
    public Trap(float x, float y, int damage) {
        this(x, y, 32f, 32f, damage, DEFAULT_COOLDOWN);
    }

    /** Creates a map-positioned trap; cooldown is measured in gameplay seconds. */
    public Trap(float x, float y, float width, float height, int damage, float cooldown) {
        if (!Float.isFinite(x) || !Float.isFinite(y)
            || !Float.isFinite(width) || width <= 0
            || !Float.isFinite(height) || height <= 0
            || damage <= 0 || !Float.isFinite(cooldown) || cooldown <= 0) {
            throw new IllegalArgumentException("Trap needs finite coordinates, positive size, damage and cooldown");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.damage = damage;
        this.cooldown = cooldown;
    }

    /** Tests actual hitbox intersection; merely touching edges does not count. */
    public boolean overlaps(float px, float py, float pw, float ph) {
        return pw > 0 && ph > 0 && px < x + width && px + pw > x
            && py < y + height && py + ph > y;
    }

    /**
     * Advances only gameplay time and delivers due hits while overlapping.
     * The receiver handles health and returns false when it cannot accept a hit
     * (for example, after death). Call this only during active, unpaused play.
     * Leaving stops damage but does not bypass an outstanding cooldown.
     * Long continuous-contact frames retain elapsed cooldown intervals.
     */
    public void update(float dt, float px, float py, float pw, float ph,
                       IntPredicate receiveDamage) {
        if (!Float.isFinite(dt) || dt < 0) {
            throw new IllegalArgumentException("delta time must be finite and non-negative");
        }
        Objects.requireNonNull(receiveDamage, "receiveDamage");
        boolean overlap = overlaps(px, py, pw, ph);
        remaining -= dt;
        if (!overlap) {
            remaining = Math.max(0, remaining);
            touching = false;
            return;
        }
        // A new contact starts now, not at the beginning of the elapsed frame.
        if (!touching) remaining = Math.max(0, remaining);
        touching = true;
        while (remaining <= 0.000001) {
            if (!receiveDamage.test(damage)) {
                remaining = Math.max(0, remaining);
                return;
            }
            remaining += cooldown;
        }
    }

    /** Restores the initial, immediately ready state on level restart. */
    public void reset() { remaining = 0; touching = false; }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public int getDamage() { return damage; }
    public float getCooldown() { return cooldown; }
}
