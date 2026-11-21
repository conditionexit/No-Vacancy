package entities.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Persistent player progress, stored with libGDX {@link Preferences}.
 *
 * <p>Two values are kept: the level the player last started ({@code lastLevel}, used
 * by "Continue") and the highest unlocked level index ({@code highestUnlocked}, used
 * by the level-select screen). The game currently has no win condition, so nothing
 * unlocks new levels yet; when one is added, call {@link #completeLevel(int)} from
 * it and the next level unlocks and becomes the "Continue" target.</p>
 *
 * <p>On desktop the file lives in the user's home folder under {@code .prefs/}.</p>
 */
public final class SaveData {
    private static final String PREFS_NAME = "no-vacancy-save";
    private static final String KEY_LAST = "lastLevel";
    private static final String KEY_UNLOCKED = "highestUnlocked";

    private final Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);

    /** @return true once the player has started at least one level */
    public boolean hasSave() {
        return prefs.contains(KEY_LAST) && LevelRegistry.isValid(prefs.getInteger(KEY_LAST, -1));
    }

    /** @return index of the level "Continue" should open (always a valid index) */
    public int lastLevel() {
        return clamp(prefs.getInteger(KEY_LAST, 0));
    }

    /** @return index of the highest level the player may select */
    public int highestUnlocked() {
        return clamp(prefs.getInteger(KEY_UNLOCKED, 0));
    }

    /** @return true if the level at {@code index} may be selected */
    public boolean isUnlocked(int index) {
        return index <= highestUnlocked();
    }

    /** Remembers that the player entered a level, so "Continue" can resume it. */
    public void recordStarted(int index) {
        if (!LevelRegistry.isValid(index)) return;
        prefs.putInteger(KEY_LAST, index);
        prefs.flush();
    }

    /**
     * Marks a level as finished: unlocks the next level (if any) and makes it the
     * "Continue" target. Not called yet because the game has no win condition.
     */
    public void completeLevel(int index) {
        int next = clamp(index + 1);
        prefs.putInteger(KEY_UNLOCKED, Math.max(highestUnlocked(), next));
        prefs.putInteger(KEY_LAST, next);
        prefs.flush();
    }

    private static int clamp(int index) {
        return Math.max(0, Math.min(index, LevelRegistry.count() - 1));
    }
}
