package ghost;

import processing.core.PApplet;
import processing.core.PImage;

public class MapCell {
    private final PImage cellImage;
    public int type;
    private final int x;
    private final int y;

    MapCell(PImage image, int character, int x, int y) {
        this.cellImage = image;
        this.type = character;
        this.x = x * 16;
        this.y = y * 16;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean canPassThrough() {
        return this.cellImage == null;
    }

    public void draw(PApplet app) {
        if (this.cellImage == null) {
            return;
        }
        app.image(this.cellImage, this.x, this.y);
    }

}
