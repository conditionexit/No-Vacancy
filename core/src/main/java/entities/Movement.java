package entities;

import com.badlogic.gdx.math.Vector2;

import java.util.Objects;

/**
 * Shared collision-aware movement helpers for maze actors such as
 * {@link Player} and {@link Enemy}.
 *
 * <p>Actors keep their own speed and hitbox definitions explicit and supply a
 * {@link PositionCheck} callback to test whether a candidate sprite position is
 * free of walls. Separating displacement scaling from substep collision
 * resolution keeps movement consistent across actors without coupling this
 * class to input, AI, or rendering.</p>
 * @author Mahammad Jafarli
 *
 * @see Player
 * @see Enemy
 */
final class Movement {

    /**
     * Maximum pixel distance moved along either axis in a single collision
     * substep, preventing long frames from tunnelling through thin walls.
     */
    static final float MAX_STEP = 8f;

    /**
     * Predicate that tests whether an actor's hitbox can legally occupy a
     * candidate world position.
     */
    @FunctionalInterface
    interface PositionCheck {
        /**
         * Reports whether the actor can stand at the given bottom-left sprite
         * coordinates without colliding with walls or map boundaries.
         *
         * @param x candidate world x-coordinate in pixels
         * @param y candidate world y-coordinate in pixels
         * @return {@code true} if the position is unobstructed
         */
        boolean isFree(float x, float y);
    }

    private Movement() {
    }

    /**
     * Normalizes a direction vector and scales it to the requested travel
     * distance.
     *
     * <p>Normalization ensures diagonal movement covers the same straight-line
     * distance as cardinal movement rather than moving faster along the
     * diagonal. Returns a zero vector when either the direction vector has zero
     * length or {@code distance} is non-positive.</p>
     *
     * @param dirX     horizontal direction component (arbitrary scale)
     * @param dirY     vertical direction component (arbitrary scale)
     * @param distance desired travel distance in world pixels
     * @return displacement vector of length {@code distance} (or zero)
     * @throws IllegalArgumentException if any argument is non-finite
     */
    static Vector2 scaleDirection(float dirX, float dirY, float distance) {
        if (!Float.isFinite(dirX) || !Float.isFinite(dirY) || !Float.isFinite(distance)) {
            throw new IllegalArgumentException("Movement direction and distance must be finite");
        }
        if (distance <= 0f) {
            return new Vector2(0f, 0f);
        }
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (len <= 0f) {
            return new Vector2(0f, 0f);
        }
        return new Vector2(dirX / len * distance, dirY / len * distance);
    }

    /**
     * Applies a displacement vector in small substeps while resolving the x and
     * y axes independently so blocked actors slide along walls.
     *
     * <p>Large displacements are split into substeps of at most
     * {@link #MAX_STEP} pixels along the larger axis. Testing x and y
     * separately on each substep allows movement along an unobstructed axis
     * even when the perpendicular axis hits a wall.</p>
     *
     * @param x     starting world x-coordinate in pixels
     * @param y     starting world y-coordinate in pixels
     * @param dx    horizontal displacement in pixels
     * @param dy    vertical displacement in pixels
     * @param check collision callback testing candidate actor positions
     * @return final world position after all substeps
     * @throws IllegalArgumentException if any coordinate or displacement is
     *                                  non-finite
     * @throws NullPointerException     if {@code check} is {@code null}
     */
    static Vector2 moveWithSlide(float x, float y, float dx, float dy, PositionCheck check) {
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(dx) || !Float.isFinite(dy)) {
            throw new IllegalArgumentException("Coordinates and displacement must be finite");
        }
        Objects.requireNonNull(check, "check");

        if (dx == 0f && dy == 0f) {
            return new Vector2(x, y);
        }

        int steps = Math.max(
            1,
            (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / MAX_STEP)
        );
        float stepX = dx / steps;
        float stepY = dy / steps;

        float curX = x;
        float curY = y;
        for (int i = 0; i < steps; i++) {
            if (stepX != 0f && check.isFree(curX + stepX, curY)) {
                curX += stepX;
            }
            if (stepY != 0f && check.isFree(curX, curY + stepY)) {
                curY += stepY;
            }
        }
        return new Vector2(curX, curY);
    }
}
