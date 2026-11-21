package entities.menu;

/**
 * Central list of the playable maze levels.
 *
 * <p>To add a new map, drop its {@code .tmx} file into {@code assets/} and add one
 * {@link Level} line to {@link #LEVELS}. The level-select screen, the "Continue"
 * button and the save data all pick it up automatically. Maps must follow the same
 * layer/object conventions as {@code level1.tmx} (see {@code entities.Maze}).</p>
 */
public final class LevelRegistry {

    /** One selectable level: a display name and its internal TMX asset path. */
    public static final class Level {
        public final String name;
        public final String file;

        public Level(String name, String file) {
            this.name = name;
            this.file = file;
        }
    }

    private static final Level[] LEVELS = {
        new Level("LEVEL 1", "level1.tmx"),
        // new Level("LEVEL 2", "level2.tmx"),
    };

    private LevelRegistry() {}

    /** @return number of registered levels */
    public static int count() {
        return LEVELS.length;
    }

    /** @return the level at {@code index} (must satisfy {@link #isValid(int)}) */
    public static Level get(int index) {
        return LEVELS[index];
    }

    /** @return true if {@code index} refers to a registered level */
    public static boolean isValid(int index) {
        return index >= 0 && index < LEVELS.length;
    }
}
