package ghost;

import processing.core.PApplet;
import processing.core.PImage;

public class Fruit extends MapCell {
    private boolean isEaten;

    Fruit(PImage image, int character, int x, int y) {
        super(image, character, x, y);
        this.isEaten = false;
    }

    @Override
    public void draw(PApplet app) {
        if (this.isEaten) {
            return;
        }
        super.draw(app);
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    public void eaten() {
        this.isEaten = true;
    }

    public boolean isEaten() {
        return this.isEaten;
    }
}
