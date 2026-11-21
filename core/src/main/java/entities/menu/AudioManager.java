package entities.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;

/**
 * Owns the background music and the player's volume settings.
 *
 * <p>Three channels are exposed: Master, Music and SFX (each 0..1) plus a mute
 * switch. Music is applied live to the playing track. Sound effects (currently the
 * footsteps in {@code entities.Player}) read {@link #sfxVolume()} and apply it
 * themselves, because libGDX {@code Sound} instances are owned by their users.
 * Settings are saved with libGDX {@link Preferences}, so they persist across sessions.</p>
 * @author zarifa
 */
public final class AudioManager {
    private static final String PREFS_NAME = "no-vacancy-settings";
    private static final String MUSIC_FILE = "Audio/Music/Bygone Halls.wav";

    private final Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
    private float master;
    private float music;
    private float sfx;
    private boolean muted;
    private Music track;

    public AudioManager() {
        master = prefs.getFloat("master", 1.0f);
        music = prefs.getFloat("music", 0.6f);
        sfx = prefs.getFloat("sfx", 1.0f);
        muted = prefs.getBoolean("muted", false);
    }

    // ---------------------------------------------------------------- music

    /** Starts the looping background track if it is not already playing. Safe to call repeatedly. */
    public void playMusic() {
        try {
            if (track == null) {
                track = Gdx.audio.newMusic(Gdx.files.internal(MUSIC_FILE));
                track.setLooping(true);
            }
            applyMusicVolume();
            if (!track.isPlaying()) track.play();
        } catch (RuntimeException e) {
            // Missing or unreadable file: keep the game running without music.
            Gdx.app.error("AudioManager", "Could not play music: " + MUSIC_FILE, e);
            if (track != null) {
                track.dispose();
                track = null;
            }
        }
    }

    private void applyMusicVolume() {
        if (track != null) track.setVolume(musicVolume());
    }

    // -------------------------------------------------------------- volumes

    /** @return the volume the music is actually played at (master x music, 0 when muted) */
    public float musicVolume() {
        return muted ? 0f : master * music;
    }

    /** @return the volume sound effects should be played at (master x sfx, 0 when muted) */
    public float sfxVolume() {
        return muted ? 0f : master * sfx;
    }

    public float getMaster() { return master; }
    public float getMusic()  { return music; }
    public float getSfx()    { return sfx; }
    public boolean isMuted() { return muted; }

    public void setMaster(float value) {
        master = MathUtils.clamp(value, 0f, 1f);
        applyMusicVolume();
    }

    public void setMusic(float value) {
        music = MathUtils.clamp(value, 0f, 1f);
        applyMusicVolume();
    }

    public void setSfx(float value) {
        sfx = MathUtils.clamp(value, 0f, 1f);
    }

    public void setMuted(boolean value) {
        muted = value;
        applyMusicVolume();
    }

    // ---------------------------------------------------------- persistence

    /** Writes the current settings to disk. */
    public void save() {
        prefs.putFloat("master", master);
        prefs.putFloat("music", music);
        prefs.putFloat("sfx", sfx);
        prefs.putBoolean("muted", muted);
        prefs.flush();
    }

    /** Saves the settings and releases the music stream. */
    public void dispose() {
        save();
        if (track != null) {
            track.dispose();
            track = null;
        }
    }
}
