package ghost;

import processing.core.PApplet;
import processing.core.PImage;

public class Waka extends MapCell {
    int x;
    int y;
    PImage left;
    PImage right;
    PImage up;
    PImage down;
    PImage closed;
    Waka(PImage[] images, int character, int x, int y) {
        super(images[0], character, x, y);
        this.x = x * 16;
        this.y = y * 16;
        this.left = images[0];
        this.right = images[1];
        this.up = images[2];
        this.down = images[3];
        this.closed = images[4];
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public void draw(PApplet app) {
        app.image(this.left, this.x, this.y - 5);
    }
}
