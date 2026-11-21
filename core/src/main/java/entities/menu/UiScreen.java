package entities.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import entities.NoVacancyGame;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * Shared base for the menu screens: a dark stone-brick background, a vertical list of
 * buttons / sliders, mouse and keyboard navigation, and a 640x500 {@link FitViewport}
 * (the same virtual size the game uses, so the menu scales and letterboxes like the game).
 *
 * <p>Subclasses only describe their content: {@link #title()} and {@link #build()}
 * (add widgets to {@link #widgets} and position them, e.g. with
 * {@link #layoutColumn(float, float, float, float)}). Everything is drawn with a
 * {@link ShapeRenderer} and libGDX's built-in font, so no extra assets are required.</p>
 *
 * <p>Controls: W/S or Up/Down move, Enter/Space select, A/D or Left/Right adjust a
 * slider, Esc goes back ({@link #onBack()}); the mouse hovers, clicks and drags sliders.</p>
 */
public abstract class UiScreen extends ScreenAdapter {
    public static final float W = 640f;
    public static final float H = 500f;

    // Palette taken from the game's tileset: slate stone, warm wood, lantern gold.
    protected static final Color MORTAR = new Color(0.05f, 0.05f, 0.08f, 1f);
    protected static final Color BRICK = new Color(0.10f, 0.11f, 0.15f, 1f);
    protected static final Color WOOD = new Color(0.30f, 0.20f, 0.12f, 1f);
    protected static final Color WOOD_FOCUS = new Color(0.52f, 0.34f, 0.14f, 1f);
    protected static final Color SLATE_OFF = new Color(0.14f, 0.15f, 0.19f, 1f);
    protected static final Color EDGE = new Color(0.10f, 0.07f, 0.05f, 1f);
    protected static final Color GOLD = new Color(1f, 0.82f, 0.35f, 1f);
    protected static final Color TEXT = new Color(0.93f, 0.88f, 0.78f, 1f);
    protected static final Color TEXT_OFF = new Color(0.40f, 0.41f, 0.47f, 1f);
    protected static final Color NEON = new Color(1f, 0.25f, 0.22f, 1f);

    // ------------------------------------------------------------ widgets

    /** A focusable menu element occupying {@link #bounds} in virtual-screen coordinates. */
    protected abstract static class Widget {
        final Rectangle bounds = new Rectangle();
        boolean enabled;

        Widget(boolean enabled) {
            this.enabled = enabled;
        }

        abstract String text();

        /** Mouse click or Enter/Space. */
        void activate() {}

        /** Left (-1) / right (+1) key. */
        void adjust(int direction) {}

        /** Mouse is held down over the widget at {@code worldX}. */
        void drag(float worldX) {}

        /** Mouse button released after a drag. */
        void release() {}

        boolean draggable() {
            return false;
        }
    }

    /** A clickable button; the label may change at runtime (e.g. a mute toggle). */
    protected static final class Button extends Widget {
        private final Supplier<String> label;
        private final Runnable action;

        Button(String label, boolean enabled, Runnable action) {
            this(() -> label, enabled, action);
        }

        Button(Supplier<String> label, boolean enabled, Runnable action) {
            super(enabled);
            this.label = label;
            this.action = action;
        }

        @Override String text() { return label.get(); }

        @Override void activate() {
            if (enabled) action.run();
        }
    }

    /** A 0..1 slider; {@code onRelease} runs when the user lets go (used to save settings). */
    protected static final class Slider extends Widget {
        private static final float LABEL_SPACE = 170f;
        private static final float VALUE_SPACE = 80f;

        private final String label;
        private final DoubleSupplier getter;
        private final DoubleConsumer setter;
        private final Runnable onRelease;

        Slider(String label, DoubleSupplier getter, DoubleConsumer setter, Runnable onRelease) {
            super(true);
            this.label = label;
            this.getter = getter;
            this.setter = setter;
            this.onRelease = onRelease;
        }

        @Override String text() { return label; }

        float value() { return MathUtils.clamp((float) getter.getAsDouble(), 0f, 1f); }

        float trackX() { return bounds.x + LABEL_SPACE; }

        float trackW() { return bounds.width - LABEL_SPACE - VALUE_SPACE; }

        @Override void adjust(int direction) {
            setter.accept(MathUtils.clamp(value() + direction * 0.05f, 0f, 1f));
            onRelease.run();
        }

        @Override void drag(float worldX) {
            setter.accept(MathUtils.clamp((worldX - trackX()) / trackW(), 0f, 1f));
        }

        @Override void release() { onRelease.run(); }

        @Override boolean draggable() { return true; }
    }

    // -------------------------------------------------------------- state

    protected final NoVacancyGame game;
    protected final Array<Widget> widgets = new Array<>();

    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(W, H, camera);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector2 mouse = new Vector2();
    private final Color tmp = new Color();

    private int focus = -1;
    private Widget dragging;
    private float time;

    protected UiScreen(NoVacancyGame game) {
        this.game = game;
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    // ----------------------------------------------------- for subclasses

    /** Screen heading. */
    protected abstract String title();

    /** Create the widgets (called every time the screen is shown, so state is always fresh). */
    protected abstract void build();

    /** Esc pressed. Default: nothing (the main menu has no "back"). */
    protected void onBack() {}

    /** Controls hint shown at the bottom. */
    protected String hint() {
        return "W/S OR ARROWS: MOVE     ENTER: SELECT     ESC: BACK";
    }

    /** Index of the widget that should start focused; subclasses may call this inside {@link #build()}. */
    protected void setFocus(int index) {
        focus = index;
    }

    /** Seconds since this screen was shown; handy for animation. */
    protected float time() {
        return time;
    }

    /**
     * Stacks all widgets in a centred column, top to bottom.
     *
     * @param topY y of the top edge of the first widget (virtual coordinates, 0 = bottom)
     */
    protected void layoutColumn(float width, float height, float gap, float topY) {
        float y = topY;
        for (int i = 0; i < widgets.size; i++) {
            y -= height;
            widgets.get(i).bounds.set(W / 2f - width / 2f, y, width, height);
            y -= gap;
        }
    }

    /** Draws the heading. Called with the sprite batch open; override for a custom title. */
    protected void drawTitle() {
        text(title(), 3f, W / 2f + 3f, H - 73f, tmp.set(0f, 0f, 0f, 0.6f));
        text(title(), 3f, W / 2f, H - 70f, GOLD);
    }

    /** Draws centred text; {@code cy} is the vertical centre. Sprite batch must be open. */
    protected void text(String s, float scale, float cx, float cy, Color color) {
        font.getData().setScale(scale);
        font.setColor(color);
        layout.setText(font, s);
        font.draw(batch, s, cx - layout.width / 2f, cy + layout.height / 2f);
    }

    /** Draws left-aligned text starting at {@code x}. Sprite batch must be open. */
    protected void textLeft(String s, float scale, float x, float cy, Color color) {
        font.getData().setScale(scale);
        font.setColor(color);
        layout.setText(font, s);
        font.draw(batch, s, x, cy + layout.height / 2f);
    }

    /** A scratch colour for subclasses that need an alpha variant. */
    protected Color scratch(float r, float g, float b, float a) {
        return tmp.set(r, g, b, a);
    }

    // ---------------------------------------------------------- lifecycle

    @Override
    public void show() {
        widgets.clear();
        focus = -1;
        dragging = null;
        time = 0f;
        build();
        if (focus < 0 || focus >= widgets.size) focus = firstEnabled();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        dragging = null;
    }

    @Override
    public void render(float delta) {
        time += delta;
        handleInput();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // 1) filled shapes: background, widget bodies
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawBackground();
        for (int i = 0; i < widgets.size; i++) drawBody(widgets.get(i), i == focus);
        shapes.end();

        // 2) outlines
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < widgets.size; i++) {
            Rectangle b = widgets.get(i).bounds;
            shapes.setColor(i == focus ? GOLD : EDGE);
            shapes.rect(b.x, b.y, b.width, b.height);
            shapes.rect(b.x + 1f, b.y + 1f, b.width - 2f, b.height - 2f);
        }
        shapes.end();

        // 3) text
        batch.begin();
        drawTitle();
        for (int i = 0; i < widgets.size; i++) drawLabel(widgets.get(i), i == focus);
        text(hint(), 1f, W / 2f, 20f, TEXT_OFF);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }

    // -------------------------------------------------------------- input

    private void handleInput() {
        viewport.unproject(mouse.set(Gdx.input.getX(), Gdx.input.getY()));

        // Mouse hover moves the focus, but only when the mouse actually moved,
        // so it does not fight with keyboard navigation.
        if (Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0) {
            int hover = widgetIndexAt(mouse);
            if (hover >= 0 && widgets.get(hover).enabled) focus = hover;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            int hit = widgetIndexAt(mouse);
            if (hit >= 0 && widgets.get(hit).enabled) {
                focus = hit;
                Widget w = widgets.get(hit);
                if (w.draggable()) {
                    dragging = w;
                    w.drag(mouse.x);
                } else {
                    w.activate();
                    return;   // the click may have queued a screen change
                }
            }
        }

        if (dragging != null) {
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                dragging.drag(mouse.x);
            } else {
                dragging.release();
                dragging = null;
            }
        }

        if (pressed(Input.Keys.UP, Input.Keys.W)) moveFocus(-1);
        if (pressed(Input.Keys.DOWN, Input.Keys.S)) moveFocus(1);

        Widget focused = focusedWidget();
        if (focused != null && focused.enabled) {
            if (pressed(Input.Keys.LEFT, Input.Keys.A)) focused.adjust(-1);
            if (pressed(Input.Keys.RIGHT, Input.Keys.D)) focused.adjust(1);
            if (pressed(Input.Keys.ENTER, Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)) {
                focused.activate();
                return;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) onBack();
    }

    private static boolean pressed(int keyA, int keyB) {
        return Gdx.input.isKeyJustPressed(keyA) || Gdx.input.isKeyJustPressed(keyB);
    }

    private Widget focusedWidget() {
        return focus >= 0 && focus < widgets.size ? widgets.get(focus) : null;
    }

    private int widgetIndexAt(Vector2 p) {
        for (int i = 0; i < widgets.size; i++) {
            if (widgets.get(i).bounds.contains(p)) return i;
        }
        return -1;
    }

    private int firstEnabled() {
        for (int i = 0; i < widgets.size; i++) {
            if (widgets.get(i).enabled) return i;
        }
        return widgets.size > 0 ? 0 : -1;
    }

    private void moveFocus(int direction) {
        if (widgets.size == 0) return;
        int i = focus < 0 ? 0 : focus;
        for (int n = 0; n < widgets.size; n++) {
            i = (i + direction + widgets.size) % widgets.size;
            if (widgets.get(i).enabled) {
                focus = i;
                return;
            }
        }
    }

    // ------------------------------------------------------------ drawing

    /** Stone-brick wall with a soft lantern glow, echoing the game's tileset. */
    private void drawBackground() {
        shapes.setColor(MORTAR);
        shapes.rect(0f, 0f, W, H);

        final float brickW = 64f;
        final float brickH = 24f;
        for (int row = 0; row * brickH < H; row++) {
            float offset = (row % 2 == 0) ? 0f : brickW / 2f;
            for (float x = -offset; x < W; x += brickW) {
                int column = (int) ((x + offset) / brickW);
                float shade = ((row * 7 + column * 13) % 5) * 0.008f;
                shapes.setColor(BRICK.r + shade, BRICK.g + shade, BRICK.b + shade, 1f);
                shapes.rect(x + 1f, row * brickH + 1f, brickW - 2f, brickH - 2f);
            }
        }

        shapes.setColor(0f, 0f, 0f, 0.35f);
        shapes.rect(0f, 0f, W, H);
        for (int i = 0; i < 6; i++) {
            shapes.setColor(1f, 0.78f, 0.35f, 0.035f);
            shapes.circle(W / 2f, H - 100f, 260f - i * 38f, 48);
        }
    }

    private void drawBody(Widget w, boolean focused) {
        Rectangle b = w.bounds;
        shapes.setColor(!w.enabled ? SLATE_OFF : focused ? WOOD_FOCUS : WOOD);
        shapes.rect(b.x, b.y, b.width, b.height);

        if (w instanceof Slider s) {
            float cy = b.y + b.height / 2f;
            shapes.setColor(0.07f, 0.05f, 0.04f, 1f);
            shapes.rect(s.trackX(), cy - 5f, s.trackW(), 10f);
            shapes.setColor(GOLD);
            shapes.rect(s.trackX(), cy - 5f, s.trackW() * s.value(), 10f);
            shapes.setColor(TEXT);
            shapes.rect(s.trackX() + s.trackW() * s.value() - 5f, cy - 12f, 10f, 24f);
        }

        if (focused) {   // small lantern-gold pointer to the left of the focused widget
            float cy = b.y + b.height / 2f;
            shapes.setColor(GOLD);
            shapes.triangle(b.x - 22f, cy - 8f, b.x - 22f, cy + 8f, b.x - 8f, cy);
        }
    }

    private void drawLabel(Widget w, boolean focused) {
        Rectangle b = w.bounds;
        float cy = b.y + b.height / 2f;
        Color color = !w.enabled ? TEXT_OFF : focused ? GOLD : TEXT;

        if (w instanceof Slider s) {
            textLeft(s.text(), 2f, b.x + 16f, cy, color);
            text(Math.round(s.value() * 100f) + "%", 2f, b.x + b.width - 40f, cy, color);
        } else {
            text(w.text(), 2f, b.x + b.width / 2f, cy, color);
        }
    }
}
