package display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

/**
 * Window resolution and fullscreen state, saved in {@link Preferences}.
 *
 * <p>Call {@link #load()} then {@link #apply()} once at startup (for example
 * at the end of {@code create()}). A settings screen can call
 * {@link #setMode(int)} and {@link #setFullscreen(boolean)}; until that UI
 * exists, {@link #handleHotkeys()} gives F10 (next resolution) and F11
 * (fullscreen) from any screen's {@code render()}.</p>
 */
public final class DisplaySettings {

    /** A selectable windowed resolution. */
    public static final class Mode {
        public final int w, h;
        Mode(int w, int h) { this.w = w; this.h = h; }
        public String label() { return w + " x " + h; }
    }

    public static final Mode[] MODES = {
        new Mode(1280, 720),
        new Mode(1600, 900),
        new Mode(1920, 1080),
        new Mode(1920, 1200),
        new Mode(2560, 1440),
    };

    private static final String PREFS = "display";
    private static final int DEFAULT_MODE = 2;   // 1920 x 1080

    private static int modeIndex = DEFAULT_MODE;
    private static boolean fullscreen = false;

    private DisplaySettings() {}

    /** Reads the saved resolution and fullscreen flag (call before {@link #apply()}). */
    public static void load() {
        Preferences p = Gdx.app.getPreferences(PREFS);
        int i = p.getInteger("mode", DEFAULT_MODE);
        modeIndex = (i >= 0 && i < MODES.length) ? i : DEFAULT_MODE;
        fullscreen = p.getBoolean("fullscreen", false);
    }

    private static void save() {
        Preferences p = Gdx.app.getPreferences(PREFS);
        p.putInteger("mode", modeIndex);
        p.putBoolean("fullscreen", fullscreen);
        p.flush();
    }

    /** Applies the current settings to the window. */
    public static void apply() {
        if (fullscreen) {
            Graphics.DisplayMode dm = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(dm);
        } else {
            Mode m = MODES[modeIndex];
            Gdx.graphics.setWindowedMode(m.w, m.h);
        }
    }

    public static int getModeIndex()  { return modeIndex; }
    public static boolean isFullscreen() { return fullscreen; }
    public static Mode current()      { return MODES[modeIndex]; }

    /** Selects a windowed resolution by index into {@link #MODES} and applies it. */
    public static void setMode(int index) {
        if (index < 0 || index >= MODES.length) return;
        modeIndex = index;
        fullscreen = false;
        apply();
        save();
    }

    /** Switches between fullscreen and the saved windowed resolution. */
    public static void setFullscreen(boolean on) {
        fullscreen = on;
        apply();
        save();
    }

    /** Cycles to the next windowed resolution. */
    public static void nextMode() {
        setMode((modeIndex + 1) % MODES.length);
    }

    /** Temporary keyboard control: F10 = next resolution, F11 = fullscreen toggle. */
    public static void handleHotkeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) setFullscreen(!fullscreen);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.F10)) nextMode();
    }
}
