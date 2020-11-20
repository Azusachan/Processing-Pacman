package ghost;

import processing.core.PImage;

/**
 * Fruit which will make all ghosts frighten and invisible if eaten
 */
public class Soda extends SuperFruit{
    /**
     * Create a new instance of soda
     * @param image image of the soda
     * @param character Integer describing the type of cell, see {@code MapCell}
     * @param x row of the cell in map
     * @param y column of the cell in map
     * @see #MapCell
     */
    Soda(PImage image, int character, int x, int y) {
        super(image, character, x, y);
    }

    @Override
    public void eaten(){
        this.isEaten = true;
        ghosts.parallelStream().forEach(Ghost::frightenAndInvisible);
    }
}
