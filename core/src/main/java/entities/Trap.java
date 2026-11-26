package entities;

import java.util.Objects;
import java.util.function.IntPredicate;
/**
 * A proximity-triggered spike trap that starts hidden.
 * When the player's feet enter the trigger area, the spikes rise,
 * stay fully extended briefly, then retract and disappear.
 * The trap deals damage at most once per activation, while fully
 * extended and overlapping the player's feet hitbox.
 * After a cooldown, it can activate again.
 *
 * @author Zarifa
 */

public final class Trap {
    public static final int DEFAULT_DAMAGE = 10;
    public static final float DEFAULT_COOLDOWN = 1f;

    private static final float HIDDEN = 1.2f;
    private static final float RISE = 0.08f;
    private static final float OUT = 0.8f;
    private static final float FALL = 0.2f;
    private static final float TRIGGER_PADDING = 25f;

    private enum State { HIDDEN, RISE, OUT, FALL }

    private final float x, y, width, height;
    private final int damage;
    private final float cooldown;

    private State state = State.HIDDEN;
    private double stateTime;
    private double hiddenRemaining;
    private boolean hitThisCycle;
    private long resetGeneration;

    public Trap(float x, float y, int damage) {
        this(x, y, 32f, 32f, damage, DEFAULT_COOLDOWN);
    }

    public Trap(float x, float y, float width, float height, int damage, float cooldown) {
        if (!Float.isFinite(x) || !Float.isFinite(y)
            || !Float.isFinite(width) || width <= 0
            || !Float.isFinite(height) || height <= 0
            || damage <= 0 || !Float.isFinite(cooldown) || cooldown <= 0) {
            throw new IllegalArgumentException(
                "Trap needs finite coordinates, positive size, damage and cooldown"
            );
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.damage = damage;
        this.cooldown = cooldown;
    }

    public boolean overlaps(float px, float py, float pw, float ph) {
        return overlaps(px, py, pw, ph, 0f);
    }

    private boolean overlaps(float px, float py, float pw, float ph, float padding) {
        double offsetX = (double) px - x;
        double offsetY = (double) py - y;

        return pw > 0 && ph > 0
            && offsetX < width + padding && offsetX > -pw - padding
            && offsetY < height + padding && offsetY > -ph - padding;
    }

    // -1 tells the renderer to skip this trap entirely.
    int frameIndex() {
        return switch (state) {
            case HIDDEN -> -1;
            case RISE -> Math.min(3, 1 + (int) (stateTime / RISE));
            case OUT -> 3;
            case FALL -> Math.max(0, 2 - (int) (stateTime / FALL));
        };
    }

    public void update(float dt, float px, float py, float pw, float ph,
                       IntPredicate receiveDamage) {
        if (!Float.isFinite(dt) || dt < 0) {
            throw new IllegalArgumentException("delta time must be finite and non-negative");
        }
        Objects.requireNonNull(receiveDamage, "receiveDamage");

        if (state == State.HIDDEN) {
            hiddenRemaining = Math.max(0, hiddenRemaining - dt);

            if (hiddenRemaining == 0 && overlaps(px, py, pw, ph, TRIGGER_PADDING)) {
                state = State.RISE;
                stateTime = 0;
                hitThisCycle = false;
            }
            return;
        }

        stateTime += dt;

        if (state == State.RISE && stateTime >= 2.0 * RISE) {
            stateTime -= 2.0 * RISE;
            state = State.OUT;
        }

        if (state == State.OUT) {
            if (!hitThisCycle && overlaps(px, py, pw, ph)) {
                long generationBeforeHit = resetGeneration;
                boolean accepted = receiveDamage.test(damage);
                if (resetGeneration != generationBeforeHit) return;
                hitThisCycle = accepted;
            }

            if (stateTime >= OUT) {
                stateTime -= OUT;
                state = State.FALL;
            }
        }

        if (state == State.FALL && stateTime >= 3.0 * FALL) {
            double extraTime = stateTime - 3.0 * FALL;
            hiddenRemaining = Math.max(0, Math.max(HIDDEN, cooldown) - extraTime);
            stateTime = 0;
            state = State.HIDDEN;
        }
    }

    public void reset() {
        resetGeneration++;
        state = State.HIDDEN;
        stateTime = 0;
        hiddenRemaining = 0;
        hitThisCycle = false;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public int getDamage() { return damage; }
    public float getCooldown() { return cooldown; }
}
