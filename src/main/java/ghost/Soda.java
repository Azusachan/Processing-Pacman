package ghost;

import processing.core.PImage;

public class Soda extends SuperFruit{
    Soda(PImage image, int character, int x, int y) {
        super(image, character, x, y);
    }

    @Override
    public void eaten(){
        this.isEaten = true;
        ghosts.parallelStream().forEach(Ghost::frightenAndInvisible);
    }
}
