package entities;

import java.util.Objects;
import java.util.function.IntPredicate;

/**
 * A stationary rectangular hazard, independent of hearts, HP, input and rendering.
 *
 * <p>The trap delivers periodic damage while a player overlaps it, governed
 * by a configurable cooldown. Collision detection, hit delivery and cooldown
 * bookkeeping are all handled internally by {@link #update}; the caller
 * only supplies the player hitbox and a damage callback each frame.</p>
 *
 * @author Zarifa
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
     * Creates a trap with default tile size (32×32) and default cooldown.
     *
     * @param x      trap x-coordinate in world pixels
     * @param y      trap y-coordinate in world pixels
     * @param damage damage dealt per hit; must be positive
     * @throws IllegalArgumentException if {@code damage} is not positive
     */
    public Trap(float x, float y, int damage) {
        this(x, y, 32f, 32f, damage, DEFAULT_COOLDOWN);
    }

    /**
     * Creates a trap with explicit dimensions and cooldown.
     *
     * @param x        trap x-coordinate in world pixels
     * @param y        trap y-coordinate in world pixels
     * @param width    collision width in pixels; must be positive
     * @param height   collision height in pixels; must be positive
     * @param damage   damage dealt per hit; must be positive
     * @param cooldown minimum seconds between consecutive hits; must be positive
     * @throws IllegalArgumentException if any size, damage or cooldown value
     *                                  is non-positive or non-finite
     */
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

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    /**
     * Tests whether the given rectangle strictly overlaps this trap's
     * hitbox. Merely touching an edge without area overlap returns
     * {@code false}.
     *
     * @param px left edge of the test rectangle
     * @param py bottom edge of the test rectangle
     * @param pw width of the test rectangle; non-positive returns {@code false}
     * @param ph height of the test rectangle; non-positive returns {@code false}
     * @return {@code true} if the rectangles share interior area
     */
    public boolean overlaps(float px, float py, float pw, float ph) {
        double offsetX = (double) px - x;
        double offsetY = (double) py - y;

        return pw > 0 && ph > 0
            && offsetX < width && offsetX > -pw
            && offsetY < height && offsetY > -ph;
    }

    /**
     * Advances the trap's cooldown timer and delivers damage while the
     * player hitbox overlaps the trap.
     *
     * <p><b>Cooldown:</b> after each hit the trap waits {@code cooldown}
     * seconds before it can hit again. Accumulated time is tracked across
     * frames so that a long frame (e.g.&nbsp;a lag spike) may cause multiple
     * hits, capped at {@value #MAX_HITS_PER_UPDATE} per call.</p>
     *
     * <p><b>Enter/leave behaviour:</b> when the player first enters or
     * re-enters the trap, any negative cooldown remainder is clamped to
     * zero so that time spent outside cannot build up "owed" hits.</p>
     *
     * <p><b>Reset safety:</b> if the damage callback triggers a level
     * restart (detected via {@code resetGeneration}), the method returns
     * immediately because the trap's state has already been cleared by
     * {@link #reset()}.</p>
     *
     * @param dt            frame delta time in seconds; must be finite and
     *                      non-negative
     * @param px            left edge of the player hitbox
     * @param py            bottom edge of the player hitbox
     * @param pw            width of the player hitbox
     * @param ph            height of the player hitbox
     * @param receiveDamage callback that accepts the damage amount and
     *                      returns {@code true} if the hit was applied, or
     *                      {@code false} to stop further hits (e.g.&nbsp;on
     *                      player death)
     * @throws IllegalArgumentException if {@code dt} is negative or non-finite
     * @throws NullPointerException     if {@code receiveDamage} is null
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


    /**
     * Restores the trap to its initial, immediately-ready state on level
     * restart. Increments the reset generation so that any in-progress
     * {@link #update} call can detect the reset and stop delivering hits.
     */
    public void reset() {
        resetGeneration++;
        remaining = 0;
        touching = false;
    }

    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getCooldown() { return cooldown; }
}
