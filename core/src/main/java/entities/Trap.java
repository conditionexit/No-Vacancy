package entities;

import java.util.Objects;
import java.util.function.IntPredicate;
/**
 * A stationary rectangular hazard that applies damage while the player remains
 * in contact with it.
 *
 * <p>The trap owns its position, size, and contact cooldown. A callback applies
 * damage so the trap stays independent of player health and rendering.</p>
 */
public final class Trap {
    public static final int DEFAULT_DAMAGE = 10;
    public static final float DEFAULT_COOLDOWN = 1f;
    private static final int MAX_HITS_PER_UPDATE = 8;
    private long resetGeneration;

    private final float x, y, width, height;
    private final int damage;
    private final float cooldown;
    private double remaining;
    private boolean touching;

    /**
     * Creates a trap with the default one-tile size and cooldown.
     *
     * @param x trap x-coordinate in world pixels
     * @param y trap y-coordinate in world pixels
     * @param damage damage dealt when triggered; must be greater than zero
     * @throws IllegalArgumentException if {@code damage} is not positive
     */
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

    /**
     * Tests whether a player rectangle overlaps this trap.
     * Merely touching the edges does not count as overlap.
     *
     * @param px player hitbox x-coordinate
     * @param py player hitbox y-coordinate
     * @param pw player hitbox width
     * @param ph player hitbox height
     * @return {@code true} when the rectangles overlap
     */
    public boolean overlaps(float px, float py, float pw, float ph) {
        double offsetX = (double) px - x;
        double offsetY = (double) py - y;

        return pw > 0 && ph > 0
            && offsetX < width && offsetX > -pw
            && offsetY < height && offsetY > -ph;
    }

    /**
     * Advances trap cooldown time and delivers any due damage while overlapping.
     * The receiver returns false when the player cannot accept damage. Call this
     * only during active play. Leaving stops damage but preserves any remaining
     * cooldown.
     *
     * @param dt elapsed active gameplay time in seconds
     * @param px player hitbox x-coordinate
     * @param py player hitbox y-coordinate
     * @param pw player hitbox width
     * @param ph player hitbox height
     * @param receiveDamage callback that receives damage amount and reports
     *                      whether it was accepted
     * @throws IllegalArgumentException if {@code dt} is negative or not finite
     * @throws NullPointerException if {@code receiveDamage} is null
     */
    public void update(float dt, float px, float py, float pw, float ph,
                       IntPredicate receiveDamage) {
        if (!Float.isFinite(dt) || dt < 0) {
            throw new IllegalArgumentException("delta time must be finite and non-negative");
        }
        Objects.requireNonNull(receiveDamage, "receiveDamage");

        remaining -= dt;
        if (!overlaps(px, py, pw, ph)) {
            remaining = Math.max(0, remaining);
            touching = false;
            return;
        }

        if (!touching) {
            remaining = Math.max(0, remaining);
        }
        touching = true;

        for (int hits = 0; hits < MAX_HITS_PER_UPDATE && remaining <= 0; hits++) {
            long generationBeforeHit = resetGeneration;
            boolean accepted = receiveDamage.test(damage);

            if (resetGeneration != generationBeforeHit) {
                return;
            }

            if (!accepted) {
                remaining = 0;
                return;
            }

            remaining += cooldown;
        }

        if (remaining <= 0) {
            // Drop excess overdue hits and start a fresh cooldown.
            remaining = cooldown;
        }
    }


    /** Restores the immediately ready state when the level restarts. */
    public void reset() {
        resetGeneration++;
        remaining = 0;
        touching = false;
    }

    /** @return trap x-coordinate in world pixels */
    public float getX() { return x; }
    /** @return trap y-coordinate in world pixels */
    public float getY() { return y; }
    /** @return trap width in world pixels */
    public float getWidth() { return width; }
    /** @return trap height in world pixels */
    public float getHeight() { return height; }
    /** @return damage dealt on each accepted hit */
    public int getDamage() { return damage; }
    /** @return minimum time between accepted hits, in seconds */
    public float getCooldown() { return cooldown; }
}
