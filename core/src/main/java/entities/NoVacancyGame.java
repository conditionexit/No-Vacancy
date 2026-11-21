package entities;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import entities.menu.AudioManager;
import entities.menu.LevelRegistry;
import entities.menu.LevelSelectScreen;
import entities.menu.MenuScreen;
import entities.menu.SaveData;
import entities.menu.SettingsScreen;

/**
 * Application entry point and screen flow: main menu, level select, settings and the
 * actual gameplay ({@link Main}, which is now a screen).
 *
 * <p>Screen changes are queued and applied at the start of the next frame, so a screen
 * can safely request a change from inside its own {@code render} without being
 * disposed while it is still drawing. The previous screen is disposed after the switch.
 * The {@link AudioManager} and {@link SaveData} live as long as the application and
 * are shared by every screen, so music keeps playing between menu and game.</p>
 */
public class NoVacancyGame extends Game {
    private AudioManager audio;
    private SaveData save;
    private Screen pending;

    @Override
    public void create() {
        audio = new AudioManager();
        save = new SaveData();
        audio.playMusic();
        showMenu();
    }

    /** @return shared audio settings and music */
    public AudioManager audio() { return audio; }

    /** @return shared save data */
    public SaveData save() { return save; }

    // ---------------------------------------------------------- navigation

    public void showMenu()        { queue(new MenuScreen(this)); }
    public void showLevelSelect() { queue(new LevelSelectScreen(this)); }
    public void showSettings()    { queue(new SettingsScreen(this)); }

    /** "Start Game": begins at the first level. Unlocked levels are kept. */
    public void startNewGame() {
        startLevel(0);
    }

    /** "Continue": reopens the level the player last started. */
    public void continueGame() {
        startLevel(save.lastLevel());
    }

    /** Starts the level at {@code index} in {@link LevelRegistry} and remembers it for "Continue". */
    public void startLevel(int index) {
        if (!LevelRegistry.isValid(index)) return;
        save.recordStarted(index);
        queue(new Main(this, LevelRegistry.get(index).file));
    }

    private void queue(Screen next) {
        if (pending != null) pending.dispose();   // a second request in one frame replaces the first
        pending = next;
    }

    // ----------------------------------------------------------- lifecycle

    @Override
    public void render() {
        if (pending != null) {
            Screen old = getScreen();
            setScreen(pending);   // hides the old screen, shows and resizes the new one
            pending = null;
            if (old != null) old.dispose();
        }
        super.render();
    }

    @Override
    public void dispose() {
        Screen current = getScreen();
        super.dispose();
        if (current != null) current.dispose();
        if (pending != null) pending.dispose();
        if (audio != null) audio.dispose();
    }
}
