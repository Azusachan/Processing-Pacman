package ghost;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Ghost which is used to fill space if the map does not contain a ghost
 */
public class NullGhost extends Ghost{
    /**
     * Initializes a nullGhost and set its target to itself
     */
    NullGhost() {
        super(new PImage[]{null, null}, 16, 0, 0);
        this.target = this;
    }

    @Override
    public void findTarget() {
        this.x = this.target.getX();
        this.y = this.target.getY();
        super.findRoute();
    }

    @Override
    public void draw(PApplet app) { }
}
