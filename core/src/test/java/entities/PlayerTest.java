
package entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Player} class.
 *
 * Verifies player health initialization, damage handling,
 * health limits, and alive/dead state.
 */
class PlayerTest {

    /**
     * Verifies that a new player starts with full health
     * and is alive.
     */
    @Test
    void playerStartsWithFullHealth() {
        Player player = new Player(0, 0);

        assertEquals(100, player.getHealth());
        assertEquals(100, player.getMaxHealth());
        assertTrue(player.isAlive());
    }

    /**
     * Verifies that receiving zero damage does not
     * change the player's health.
     */
    @Test
    void zeroDamageDoesNotChangeHealth() {
        Player player = new Player(0, 0);

        player.takeDamage(0);

        assertEquals(100, player.getHealth());
        assertTrue(player.isAlive());
    }

    /**
     * Verifies that damage equal to the player's remaining
     * health reduces health to zero and kills the player.
     */
    @Test
    void exactDamageKillsPlayer() {
        Player player = new Player(0, 0);

        player.takeDamage(100);

        assertEquals(0, player.getHealth());
        assertFalse(player.isAlive());
    }

    /**
     * Verifies that excessive damage cannot reduce
     * the player's health below zero.
     */
    @Test
    void excessiveDamageCannotMakeHealthNegative() {
        Player player = new Player(0, 0);

        player.takeDamage(150);

        assertEquals(0, player.getHealth());
        assertFalse(player.isAlive());
    }

    /**
     * Verifies that a dead player remains dead
     * after receiving additional damage.
     */
    @Test
    void deadPlayerStaysDeadAfterMoreDamage() {
        Player player = new Player(0, 0);

        player.takeDamage(100);
        player.takeDamage(20);

        assertEquals(0, player.getHealth());
        assertFalse(player.isAlive());
    }

    /**
     * Verifies that negative damage is rejected
     * without modifying the player's health.
     */
    @Test
    void negativeDamageThrowsException() {
        Player player = new Player(0, 0);

        assertThrows(IllegalArgumentException.class,
            () -> player.takeDamage(-10));

        assertEquals(100, player.getHealth());
    }
}
