package ghost;

import processing.core.PApplet;
import processing.core.PImage;

public class NullGhost extends Ghost{
    NullGhost(PImage[] image) {
        super(image, 16, 0, 0);
        this.target = this;
    }

    @Override
    public void findTarget() {
        this.x = target.getX();
        this.y = target.getY();
        super.findRoute();
    }

    @Override
    public void draw(PApplet app) { }
}
