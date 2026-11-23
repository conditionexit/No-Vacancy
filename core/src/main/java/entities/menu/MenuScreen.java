package entities.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import entities.NoVacancyGame;

/**
 * The main menu: Start Game, Continue, Levels, Settings and Quit.
 *
 * <p>"Continue" is greyed out (and skipped by keyboard navigation) until the player has
 * started a level at least once; when a save exists it is focused by default.</p>
 * @author zarifa
 */
public final class MenuScreen extends UiScreen {

    public MenuScreen(NoVacancyGame game) {
        super(game);
    }

    @Override
    protected String title() {
        return "NO-VACANCY";
    }

    @Override
    protected void build() {
        boolean hasSave = game.save().hasSave();

        widgets.add(new Button("START GAME", true, game::startNewGame));
        widgets.add(new Button("CONTINUE", hasSave, game::continueGame));
        widgets.add(new Button("LEVELS", true, game::showLevelSelect));
        widgets.add(new Button("SETTINGS", true, game::showSettings));
        widgets.add(new Button("QUIT", true, () -> Gdx.app.exit()));

        layoutColumn(280f, 46f, 12f, 340f);
        setFocus(hasSave ? 1 : 0);
    }

    /** Neon-sign title with a gentle flicker, like a motel sign. */
    @Override
    protected void drawTitle() {
        float flicker = 0.88f + 0.12f * MathUtils.sin(time() * 7f);
        if (MathUtils.sin(time() * 1.3f) > 0.985f) flicker = 0.35f;   // occasional stutter

        float y = H - 78f;
        text(title(), 4f, W / 2f + 2f, y - 2f, scratch(NEON.r, NEON.g, NEON.b, 0.25f * flicker));
        text(title(), 4f, W / 2f - 2f, y + 2f, scratch(NEON.r, NEON.g, NEON.b, 0.25f * flicker));
        text(title(), 4f, W / 2f, y, scratch(1f, 0.35f, 0.30f, flicker));
        text("A MAZE RUNNER", 2f, W / 2f, y - 48f, scratch(0.62f, 0.62f, 0.70f, 1f));
    }
}
