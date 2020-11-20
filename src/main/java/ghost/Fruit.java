package ghost;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Edible MapCell, if all eaten, player will win the game.
 */
public class Fruit extends MapCell {
    /**
     * If the fruit is eaten by player
     */
    public boolean isEaten;

    /**
     * Create a new instance of Fruit
     * @param image image of the fruit
     * @param character Integer describing the type of cell, see {@code MapCell}
     * @param x row of the cell in map
     * @param y column of the cell in map
     * @see #MapCell
     */
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
    public boolean cannotPassThrough() {
        return false;
    }

    /**
     * Eat the fruit
     * <p>If fruit is eaten, it will disappear as draw function no longer draw anything</p>
     * <p>If a super fruit is eaten, all ghosts will be frighten</p>
     * <p>If a soda is eaten, all ghosts will be frighten and invisible</p>
     */
    public void eaten() {
        this.isEaten = true;
    }

    /**
     * Make the fruit no longer eaten
     */
    public void restore() {
        this.isEaten = false;
    }

    /**
     * Returns if the fruit is eaten
     * @return if the fruit is eaten
     */
    public boolean isEaten() {
        return this.isEaten;
    }
}
