package entities;

/**
 * Represents the player character in the maze game.
 *
 * The player has a position inherited from {@link GameEntity},
 * a maximum health value, and a current health value.
 * The player can move, take damage, and die when health reaches zero.
 *
 * Health is initialized to 100 when the player is created.
 *
 * @author Mahammad Jafarli
 */

public class Player extends GameEntity {
    private int health;
    private int maxHealth;

    public Player(float x, float y) {
        super(x, y);
        maxHealth = 100;
        health = maxHealth;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth(){
        return maxHealth;
    }

    public void move(float dx, float dy){
        setPosition(getX() + dx, getY() + dy);
    }



    public void takeDamage(int amount){
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }

        health = Math.max(0, health - amount);
    }

    public boolean isAlive(){
        return health > 0;
    }


}
