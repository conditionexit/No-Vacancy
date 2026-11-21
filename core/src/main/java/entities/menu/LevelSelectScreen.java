package entities.menu;

import entities.NoVacancyGame;

/**
 * Lists every level in {@link LevelRegistry}; locked levels (see {@link SaveData}) are
 * greyed out. Up to six levels are shown in one column, more switch to two columns.
 */
public final class LevelSelectScreen extends UiScreen {

    public LevelSelectScreen(NoVacancyGame game) {
        super(game);
    }

    @Override
    protected String title() {
        return "SELECT LEVEL";
    }

    @Override
    protected void build() {
        SaveData save = game.save();
        int count = LevelRegistry.count();

        for (int i = 0; i < count; i++) {
            final int index = i;
            boolean unlocked = save.isUnlocked(i);
            String label = LevelRegistry.get(i).name + (unlocked ? "" : "  (LOCKED)");
            widgets.add(new Button(label, unlocked, () -> game.startLevel(index)));
        }
        widgets.add(new Button("BACK", true, game::showMenu));

        // Levels in a grid, BACK centred underneath.
        int columns = count > 6 ? 2 : 1;
        float buttonW = columns == 1 ? 320f : 250f;
        float buttonH = 46f;
        float gap = 14f;
        float topY = H - 130f;
        float gridW = columns * buttonW + (columns - 1) * gap;

        for (int i = 0; i < count; i++) {
            int column = i % columns;
            int row = i / columns;
            float x = W / 2f - gridW / 2f + column * (buttonW + gap);
            float y = topY - row * (buttonH + gap) - buttonH;
            widgets.get(i).bounds.set(x, y, buttonW, buttonH);
        }
        widgets.get(count).bounds.set(W / 2f - 100f, 50f, 200f, buttonH);

        // Focus the level the player would continue, if it can be selected.
        int last = save.lastLevel();
        setFocus(save.hasSave() && save.isUnlocked(last) ? last : 0);
    }

    @Override
    protected void onBack() {
        game.showMenu();
    }
}
