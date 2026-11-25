package display;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Resolution-independent cameras: one for the maze world, one for the HUD.
 *
 * <p>Both use a {@link FitViewport} with a fixed virtual size, so the player
 * always sees the same part of the maze. Windows with a different aspect
 * ratio (for example 1920x1200) get black bars instead of extra map.
 * To show more map on taller screens instead, swap {@code FitViewport} for
 * {@code ExtendViewport} on {@link #world}.</p>
 *
 * <p>Set {@link #VIEW_W} and {@link #VIEW_H} to the amount of maze, in world
 * pixels, that the camera should show. Match what your current camera shows
 * if you want the look to stay the same.</p>
 *
 * <p>Per-frame use:</p>
 * <pre>
 * ScreenUtils.clear(0, 0, 0, 1);            // black bars
 * view.follow(px, py, mapW, mapH);
 * view.applyWorld(batch);
 * batch.begin(); ... maze, entities ... batch.end();
 * view.applyHud(batch);
 * batch.begin(); ... hearts, drawn in 0..VIEW_W / 0..VIEW_H ... batch.end();
 * </pre>
 * and from {@code resize(w, h)}: {@code view.resize(w, h);}
 */
public class GameView {

    public static final float VIEW_W = 1920f;
    public static final float VIEW_H = 1080f;

    public final OrthographicCamera worldCam = new OrthographicCamera();
    public final FitViewport world = new FitViewport(VIEW_W, VIEW_H, worldCam);

    public final OrthographicCamera hudCam = new OrthographicCamera();
    public final FitViewport hud = new FitViewport(VIEW_W, VIEW_H, hudCam);

    /** Call from {@code Screen.resize(int, int)}. */
    public void resize(int width, int height) {
        world.update(width, height, false);
        hud.update(width, height, true);   // HUD camera sits at (0,0) so y=0 is the bottom
    }

    /**
     * Centres the world camera on a point, kept inside the map so no
     * void is shown. If the map is smaller than the view in one axis, the
     * camera is centred on the map in that axis.
     */
    public void follow(float focusX, float focusY, float mapW, float mapH) {
        float halfW = VIEW_W / 2f, halfH = VIEW_H / 2f;
        worldCam.position.x = (mapW <= VIEW_W) ? mapW / 2f : MathUtils.clamp(focusX, halfW, mapW - halfW);
        worldCam.position.y = (mapH <= VIEW_H) ? mapH / 2f : MathUtils.clamp(focusY, halfH, mapH - halfH);
        worldCam.update();
    }

    /** Activates the world viewport and points the batch at the world camera. */
    public void applyWorld(com.badlogic.gdx.graphics.g2d.Batch batch) {
        world.apply();
        batch.setProjectionMatrix(worldCam.combined);
    }

    /** Activates the HUD viewport and points the batch at the HUD camera. */
    public void applyHud(com.badlogic.gdx.graphics.g2d.Batch batch) {
        hud.apply();
        batch.setProjectionMatrix(hudCam.combined);
    }

    /** Pixel position inside the HUD, measured from the top-left corner. */
    public static float hudYFromTop(float offset, float elementHeight) {
        return VIEW_H - offset - elementHeight;
    }
}
