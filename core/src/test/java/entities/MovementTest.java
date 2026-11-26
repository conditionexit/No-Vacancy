package entities;

import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Movement}, covering wall sliding, diagonal movement
 * normalization, and long-frame substepping.
 */
class MovementTest {

    @Test
    void slidesAlongVerticalWallWhenHorizontalAxisIsBlocked() {
        // Wall starts past x = 10; vertical movement remains unobstructed.
        Vector2 result = Movement.moveWithSlide(5f, 0f, 12f, 20f, (px, py) -> px <= 10f);

        assertTrue(result.x <= 10f, "x must not cross the vertical wall");
        assertEquals(20f, result.y, 0.001f, "y should slide freely along the wall");
    }

    @Test
    void slidesAlongHorizontalWallWhenVerticalAxisIsBlocked() {
        // Wall starts past y = 10; horizontal movement remains unobstructed.
        Vector2 result = Movement.moveWithSlide(0f, 5f, 20f, 12f, (px, py) -> py <= 10f);

        assertEquals(20f, result.x, 0.001f, "x should slide freely along the wall");
        assertTrue(result.y <= 10f, "y must not cross the horizontal wall");
    }

    @Test
    void stopsCompletelyWhenBothAxesAreBlocked() {
        Vector2 result = Movement.moveWithSlide(4f, 4f, 10f, 10f, (px, py) -> false);

        assertEquals(4f, result.x, 0.001f);
        assertEquals(4f, result.y, 0.001f);
    }

    @Test
    void normalizesDiagonalMovementToMatchCardinalSpeed() {
        float distance = 25f;
        Vector2 diagonal = Movement.scaleDirection(1f, 1f, distance);
        Vector2 cardinal = Movement.scaleDirection(1f, 0f, distance);

        assertEquals(distance, diagonal.len(), 0.001f,
            "Diagonal travel distance must equal cardinal travel distance");
        assertEquals(cardinal.len(), diagonal.len(), 0.001f);

        float expectedComponent = (float) (distance / Math.sqrt(2.0));
        assertEquals(expectedComponent, diagonal.x, 0.001f);
        assertEquals(expectedComponent, diagonal.y, 0.001f);

        Vector2 moved = Movement.moveWithSlide(0f, 0f, diagonal.x, diagonal.y, (px, py) -> true);
        assertEquals(distance, moved.len(), 0.001f);
    }

    @Test
    void returnsZeroVectorForZeroDirectionOrNonPositiveDistance() {
        assertEquals(new Vector2(0f, 0f), Movement.scaleDirection(0f, 0f, 25f));
        assertEquals(new Vector2(0f, 0f), Movement.scaleDirection(1f, 1f, 0f));
        assertEquals(new Vector2(0f, 0f), Movement.scaleDirection(1f, 1f, -5f));
    }

    @Test
    void longFrameDoesNotTunnelThroughThinWall() {
        // 10-pixel actor starting at x = 0; 10-pixel thin wall occupies [20, 30].
        // A single unsplit 60-pixel jump would land at x = 60 and skip the wall.
        float actorWidth = 10f;
        Movement.PositionCheck thinWall = (px, py) -> px + actorWidth <= 20f || px >= 30f;

        Vector2 result = Movement.moveWithSlide(0f, 0f, 60f, 0f, thinWall);

        assertTrue(result.x + actorWidth <= 20f,
            "Actor must stop before the thin wall instead of tunnelling past x = 30");
    }

    @Test
    void splitsLongFrameIntoSubstepsNoLargerThanMaxStep() {
        List<Float> probedX = new ArrayList<>();
        Movement.moveWithSlide(0f, 0f, 40f, 0f, (px, py) -> {
            probedX.add(px);
            return true;
        });

        assertEquals(5, probedX.size(), "40 px displacement with MAX_STEP=8 should probe 5 substeps");
        float prevX = 0f;
        for (float x : probedX) {
            assertTrue(x - prevX <= Movement.MAX_STEP + 0.001f,
                "Each substep must not exceed MAX_STEP");
            prevX = x;
        }
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class,
            () -> Movement.scaleDirection(Float.NaN, 1f, 10f));
        assertThrows(IllegalArgumentException.class,
            () -> Movement.moveWithSlide(0f, 0f, Float.POSITIVE_INFINITY, 0f, (px, py) -> true));
        assertThrows(NullPointerException.class,
            () -> Movement.moveWithSlide(0f, 0f, 1f, 1f, null));
    }
}
