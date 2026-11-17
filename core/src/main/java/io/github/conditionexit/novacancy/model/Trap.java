package io.github.conditionexit.novacancy.model;

import java.util.Objects;
import java.util.function.IntPredicate;
/**
 * A stationary rectangular trap.
 * Collision and cooldown are independent of the player's health system.
 *
 * @author Zarifa
 */
public final class Trap{

    public static final int DEFAULT_DAMAGE = 10;
    public static final float DEFAULT_COOLDOWN = 1f;

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final int damage;
    private final float cooldown;

    private double remainingCooldown;
    private boolean touching;

    /**
     * Creates a trap using world coordinates.
     *
     * @param x left position
     * @param y bottom position
     * @param width collision width
     * @param height collision height
     * @param damage damage value passed to the health handler
     * @param cooldown seconds between successful hits
     */
    public Trap(float x, float y, float width, float height, int damage, float cooldown){
        if(!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(width) || width<=0 || !Float.isFinite(height) || height<=0 || damage<=0 || !Float.isFinite(cooldown) || cooldown<=0){
            throw new IllegalArgumentException(
                "Trap requires finite coordinates and positive" + "size,damage and cooldown"
            );

        }
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
        this.damage=damage;
        this.cooldown=cooldown;
    }
    //Creates a trap with 10 damage and a one-second cooldown.
    public Trap(float x, float y, float width, float height){
        this(x,y,width,height,DEFAULT_DAMAGE,DEFAULT_COOLDOWN);
    }
    //Preserves the original constructor with a 32-by-32 hitbox.
    public Trap(float x, float y, int damage){
        this(x,y,32f,32f,damage,DEFAULT_COOLDOWN);
    }
    // Return whether the supplied hitbox intersects this trap
    public boolean overlaps(float playerX, float playerY, float playerWidth, float playerHeight){
        return playerWidth>0 && playerHeight>0 && playerX < x+width && playerX + playerWidth > x && playerY <y +height && playerY + playerHeight>y;
    }
    /**
     * Advances the cooldown and applies hits while the player overlaps
     * The damage handler receives the configured damage and returns true
     * when a hit is accepted , or false when no hit can be applied.
     * Call once per active gameplay updates.Skip calls while paused
     * Use one trap instance per trap for the game's single player.
     * @param deltaTime elapsed gameplay time in seconds
     * @param playerX player hitbox left position
     * @param playerY player hitbox bottom position
     * @param playerWidth player hitbox width
     * @param playerHeight player hitbox height
     * @param receiveDamage adapter to the hearts or HP system
     */
    public void update(float deltaTime,float playerX, float playerY, float playerWidth, float playerHeight, IntPredicate receiveDamage){
        if(!Float.isFinite(deltaTime) || deltaTime <0){
            throw new IllegalArgumentException(
                "deltaTime must be finite and non-negative"
            );
        }
        Objects.requireNonNull(receiveDamage,"receiveDamage");
        remainingCooldown -= deltaTime;
        if(!overlaps(playerX, playerY, playerWidth, playerHeight)){
            remainingCooldown = Math.max(0,remainingCooldown);
            touching=false;
            return;
        }
        // A new contact starts now, not before this frame.
        if (!touching){
            remainingCooldown = Math.max(0,remainingCooldown);
        }
        touching = true;
        // Preserve elapsed intervals during continuous contact.
        while (remainingCooldown <=0.000001){
            if (!receiveDamage.test(damage)){
                remainingCooldown =Math.max(0,remainingCooldown);
                return;
            }
            remainingCooldown += cooldown;
        }

    }
    // Clears contact and makes the trap ready after a level restart.
    public void reset(){
        remainingCooldown=0;
        touching =false;
    }
    public float getX(){
        return x;
    }
    public float getY(){
        return y;
    }
    public float getWidth(){
        return width;
    }

    public float getHeight() {
        return height;
    }
    public int getDamage(){
        return damage;
    }

    public float getCooldown() {
        return cooldown;
    }
}
