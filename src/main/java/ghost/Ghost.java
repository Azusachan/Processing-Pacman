package ghost;

import processing.core.PApplet;
import processing.core.PImage;

public class Ghost extends MapCell{
    int x;
    int y;
    PImage ghostImage;
    Ghost(PImage image, int character, int x, int y) {
        super(image, character, x, y);
        this.ghostImage = image;
        this.x = x * 16;
        this.y = y * 16;
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public void draw(PApplet app) {
        app.image(this.ghostImage, this.x, this.y - 6);
    }
}
