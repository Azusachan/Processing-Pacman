package ghost;

import processing.core.PApplet;
import processing.core.PImage;

public class Fruit extends MapCell {
    boolean isEaten;
    PImage fruitImage;
    int x;
    int y;

    Fruit(PImage image, int character, int x, int y) {
        super(image, character, x, y);
        this.isEaten = false;
        this.fruitImage = image;
    }

    @Override
    public void draw(PApplet app) {
        if (this.isEaten) {
            return;
        }
        app.image(this.fruitImage, this.x, this.y);
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
