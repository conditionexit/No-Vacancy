package entities.menu;

import display.DisplaySettings;
import entities.Main;

/**
 * Audio and display settings: Master, Music and SFX volume sliders, a mute toggle,
 * a window resolution button and a fullscreen toggle.
 *
 * <p>Changes apply immediately (the music volume follows the sliders live) and are saved
 * whenever a slider is released, a key adjusts it, the mute toggle is used, or the screen
 * is left, so they persist across sessions. Display changes are applied and saved by
 * {@link DisplaySettings} as soon as the button is used.</p>
 * @author zarifa
 */
public final class SettingsScreen extends UiScreen {

    public SettingsScreen(Main game) {
        super(game);
    }

    @Override
    protected String title() {
        return "SETTINGS";
    }

    @Override
    protected void build() {
        AudioManager audio = game.audio();

        widgets.add(new Slider("MASTER", audio::getMaster, v -> audio.setMaster((float) v), audio::save));
        widgets.add(new Slider("MUSIC", audio::getMusic, v -> audio.setMusic((float) v), audio::save));
        widgets.add(new Slider("SFX", audio::getSfx, v -> audio.setSfx((float) v), audio::save));
        widgets.add(new Button(() -> audio.isMuted() ? "MUTE: ON" : "MUTE: OFF", true, () -> {
            audio.setMuted(!audio.isMuted());
            audio.save();
        }));

        // Click cycles through the windowed resolutions; from fullscreen it returns to the
        // saved windowed resolution first.
        widgets.add(new Button(
            () -> DisplaySettings.isFullscreen()
                ? "RESOLUTION: FULLSCREEN"
                : "RESOLUTION: " + DisplaySettings.current().w + "x" + DisplaySettings.current().h,
            true,
            () -> {
                if (DisplaySettings.isFullscreen()) DisplaySettings.setFullscreen(false);
                else DisplaySettings.nextMode();
            }));
        widgets.add(new Button(
            () -> DisplaySettings.isFullscreen() ? "FULLSCREEN: ON" : "FULLSCREEN: OFF",
            true,
            () -> DisplaySettings.setFullscreen(!DisplaySettings.isFullscreen())));

        widgets.add(new Button("BACK", true, game::showMenu));

        // 7 widgets must fit between the title (y ~ 430) and the hint line (y ~ 20) in the
        // 640x500 virtual screen: 7 * 36 + 6 * 10 = 312, so the column spans y 385 -> 73.
        layoutColumn(440f, 36f, 10f, 385f);
    }

    @Override
    protected void onBack() {
        game.showMenu();
    }

    @Override
    protected String hint() {
        return "W/S: MOVE     A/D OR LEFT/RIGHT: ADJUST     ENTER: SELECT     ESC: BACK";
    }

    @Override
    public void hide() {
        super.hide();
        game.audio().save();
    }
}
