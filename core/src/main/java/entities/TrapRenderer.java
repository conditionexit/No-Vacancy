package entities;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
/**
 * Draws animated spike traps from{@code trap_spikes.png} (4 frqames of 32x32)
 * <p>Purely visual:it reads trap position only and never changes damage or cooldown.
 * Each trap loops through  retracted , warning , rising and fully extended frames; a per-trap
 * time offset stops all traps pulsing in  sync</p>
 * @author zarifa
 */
public class TrapRenderer{
    private static final int FRAMES = 4;
    private static final float HIDDEN = 1.2f, RISE =0.12f , OUT = 0.8f, FALL=0.2f;
    private static final float CYCLE=HIDDEN+3*RISE+OUT+3*FALL;
    private final Texture sheet = new Texture("trap_spikes.png");
    private final TextureRegion[] frames;
    private float time;
    public TrapRenderer(){
        TextureRegion[][] split = TextureRegion.split(sheet,32,32);
        frames = new TextureRegion[FRAMES];
        System.arraycopy(split[0], 0, frames, 0, FRAMES);
    }
    //Advance the animation ; call once per frame while the game is not paused
    public void update (float dt){
        time +=dt;
    }
    public void draw (SpriteBatch batch , Iterable<Trap>traps){
        for (Trap t : traps){
            float offset=(t.getX()*0.013f+t.getY()*0.029f)%CYCLE;
            TextureRegion frame=frames[frameAt((time+offset)%CYCLE)];
            batch.draw(frame,t.getX(),t.getY(),t.getWidth(),t.getHeight());
        }
    }
    private static int frameAt(float p){
        if(p<HIDDEN) return 0;
        p-=HIDDEN;
        if(p<RISE) return 1;
        p-=RISE;
        if(p<RISE) return 2;
        p-=RISE;
        if(p<RISE) return 3;
        p-=RISE;
        if(p<OUT) return 3;
        p-=OUT;
        if(p<FALL) return 2;
        p-=FALL;
        if(p<FALL) return 1;
        p-=FALL;
        return 0;
    }
    public void dispose(){
        sheet.dispose();
    }
}
