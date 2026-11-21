package entities.menu;

import entities.NoVacancyGame;

/**
 * Audio settings: Master, Music and SFX volume sliders plus a mute toggle.
 *
 * <p>Changes apply immediately (the music volume follows the sliders live) and are saved
 * whenever a slider is released, a key adjusts it, the mute toggle is used, or the screen
 * is left, so they persist across sessions.</p>
 */
public final class SettingsScreen extends UiScreen {

    public SettingsScreen(NoVacancyGame game) {
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
        widgets.add(new Button("BACK", true, game::showMenu));

        layoutColumn(440f, 46f, 14f, 380f);
    }

    @Override
    protected void onBack() {
        game.showMenu();
    }

    @Override
    protected String hint() {
        return "W/S: MOVE     A/D OR LEFT/RIGHT: ADJUST     ESC: BACK";
    }

    @Override
    public void hide() {
        super.hide();
        game.audio().save();
    }
}
