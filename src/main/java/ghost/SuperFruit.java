package ghost;

import processing.core.PImage;

import java.util.List;

/**
 * Fruit that will frighten all ghosts when eaten
 */
public class SuperFruit extends Fruit{
    /**
     * List of all ghosts on map
     */
    public static List<Ghost> ghosts;

    /**
     * Create a new instance of SuperFruit
     * @param image image of the fruit
     * @param character Integer describing the type of cell, see {@code MapCell}
     * @param x row of the cell in map
     * @param y column of the cell in map
     * @see #MapCell
     */
    SuperFruit(PImage image, int character, int x, int y) {
        super(image, character, x, y);
    }

    /**
     * Set the List of all ghosts on map
     * @param ghostsList the List of all ghosts on map
     */
    public static void setGhost(List<Ghost> ghostsList) {
        ghosts = ghostsList;
    }

    @Override
    public void eaten(){
        this.isEaten = true;
        ghosts.parallelStream().forEach(Ghost::frighten);
    }
}
