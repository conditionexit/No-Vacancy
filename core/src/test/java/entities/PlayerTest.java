
package entities;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the collision and cooldown rules of {@link Trap}. */
class PlayerTest {

    @Test
    void rejectsInvalidTrapDimensionsDamageAndCooldown() {
        assertThrows(IllegalArgumentException.class,
            () -> new Trap(0, 0, 0, 32, 10, 1));
        assertThrows(IllegalArgumentException.class,
            () -> new Trap(0, 0, 32, 32, 0, 1));
        assertThrows(IllegalArgumentException.class,
            () -> new Trap(0, 0, 32, 32, 10, 0));
    }

    @Test
    void detectsOnlyPositiveAreaHitboxIntersections() {
        Trap trap = new Trap(10, 20, 32, 32, 10, 1);

        assertTrue(trap.overlaps(20, 30, 8, 8));
        assertFalse(trap.overlaps(42, 20, 8, 8));
        assertFalse(trap.overlaps(10, 20, 0, 8));
    }

    @Test
    void appliesDamageOnContactThenRespectsCooldownAndReset() {
        Trap trap = new Trap(0, 0, 32, 32, 10, 1);
        AtomicInteger hits = new AtomicInteger();

        trap.update(0, 1, 1, 8, 8, damage -> {
            hits.incrementAndGet();
            return true;
        });
        trap.update(0.5f, 1, 1, 8, 8, damage -> {
            hits.incrementAndGet();
            return true;
        });
        assertEquals(1, hits.get());

        trap.update(0.5f, 1, 1, 8, 8, damage -> {
            hits.incrementAndGet();
            return true;
        });
        assertEquals(2, hits.get());

        trap.reset();
        trap.update(0, 1, 1, 8, 8, damage -> {
            hits.incrementAndGet();
            return true;
        });
        assertEquals(3, hits.get());
    }

}
