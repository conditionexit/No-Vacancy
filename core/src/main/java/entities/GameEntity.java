package entities;
/**
 * Represents the base class for all game entities.
 *
 * Stores the position shared by players, enemies,
 * keys, traps, and other game objects.
 *
 * @author Mahammad Jafarli
 *
 */
public abstract class GameEntity {
    // coordinates of the game entity
    private float x;
    private float y;

    /**
     * Creates an entity at the specified position.
     *
     * @param x the initial horizontal position
     * @param y the initial vertical position
     */

    protected GameEntity(float x, float y) { // GameEntity is abstract, its constructor is intended to be called by subclasses through super(...).
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    protected final void setPosition(float x, float y){
        this.x = x;
        this.y = y;
    }

    //We want subclasses such as Player and Enemy to control their positions. Other unrelated classes shouldn't be able to teleport an entity whenever they want.
    //final prevents subclasses from overriding this basic position-changing operation unexpectedly.

}
