package ghost;

import processing.core.PImage;

public class Fruit extends MapCell {
    Fruit(PImage image, int character, int x, int y) {
        super(image, character, x, y);
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }
}
