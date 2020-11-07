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
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public boolean canPassThrough() {
        return this.type == 0;
    }

    public int getType() {
        return this.type;
    }
    public void draw(PApplet app) {
        if (this.cellImage == null) {
            return;
        }
        app.image(this.cellImage, this.x, this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapCell)) {
            return false;
        }
        MapCell m = (MapCell) o;
        return m.getX() == this.getX() && m.getY() == this.getY();
    }
}
