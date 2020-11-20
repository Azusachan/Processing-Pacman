package ghost;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.List;

/**
 * MovableCell that player controls, able to eat fruits and be killed by ghost
 */
public class Waka extends MovableCell {
    private final PImage left;
    private final PImage right;
    private final PImage up;
    private final PImage down;
    private final PImage closed;
    public boolean closeEye;
    private int initialLife;
    private int life;

    /**
     * Initializes a new Waka
     * @param images Waka's left, right up down and mouth close image
     * @param character Integer describing the type of cell, see {@code MapCell}
     * @param x row of the cell in map
     * @param y column of the cell in map
     * @see #MapCell
     */
    Waka(PImage[] images, int character, int x, int y) {
        super(images[0], character, x, y);
        this.left = images[0];
        this.right = images[1];
        this.up = images[2];
        this.down = images[3];
        this.closed = images[4];
        this.closeEye = false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapCell)) {
            return false;
        }
        MapCell m = (MapCell) o;
        boolean result = false;
        if (this.nextCell != null) {
            if (this.nextCell.getX() == m.getX() && this.nextCell.getY() == m.getY()) {
                result = true;
            }
        } else if (this.stepOnCell != null) {
            if (this.stepOnCell.getX() == m.getX() && this.stepOnCell.getY() == m.getY()) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public void draw(PApplet app) {
        if (this.closeEye) {
            app.image(this.closed, this.x - 4, this.y - 5);
        } else {
            switch (this.currentDirection) {
                case 38:
                    app.image(this.up, this.x - 5, this.y - 4);
                    break;
                case 40:
                    app.image(this.down, this.x - 5, this.y - 4);
                    break;
                case 37:
                    app.image(this.left, this.x - 4, this.y - 5);
                    break;
                default:
                    app.image(this.right, this.x - 4, this.y - 5);
            }
        }
        // render life
        GameManager.rect(app, 0, 545, 448, 26);
        for (int i = 0; i < this.life; i++) {
            app.image(this.right, 25 * i,545);
        }
    }

    /**
     * Returns remaining life of the Waka
     * @return remaining life of the Waka
     */
    public int getLife() {
        return this.life;
    }

    /**
     * Make the Waka close or open its eyes(or mouth?)
     */
    public void closeEye() {
        this.closeEye = !this.closeEye;
    }

    /**
     * Set the amount of life Waka has
     * @param life the amount of life Waka has
     */
    public void setLife(int life) {
        this.life = life;
        this.initialLife = life;
    }

    /**
     * Kills the Waka and resets its location
     */
    public void kill(){
        this.life--;
        super.resetPosition();
    }

    public void resetPosition(){
        super.resetPosition();
        this.closeEye = false;
        this.life = this.initialLife;
    }

    /**
     * Turn Waka if appropriate
     *
     * <p>If the Waka is making 180 degree turn, it will turn immediately, if not, then it will buffers the turn to
     * {@code nextDirection} and wait for {@code tick} to handle the turn. </p>
     *
     * <p>Relation between direction and keycode</p>
     * <table summary="Keycode and Direction">
     *     <tr>
     *         <td>Directions</td> <td>KeyCode</td>
     *     </tr>
     *     <tr>
     *         <td>Up</td> <td>38</td>
     *     </tr>
     *     <tr>
     *         <td>Down</td> <td>40</td>
     *     </tr>
     *     <tr>
     *         <td>Left</td> <td>37</td>
     *     </tr>
     *     <tr>
     *         <td>Right</td> <td>39</td>
     *     </tr>
     *     </table>
     * @param keyCode Number to describe four direction keys on the keyboard, see description for detail
     */
    public void turn(int keyCode) {
        this.nextDirection = keyCode;
        // turn when 180 degree
        if (Math.abs(this.nextDirection - this.currentDirection) == 2) {
            this.currentDirection = this.nextDirection;
            this.nextDirection = 0;
        }
    }

    //handles movement
    @Override
    public boolean tick(List<MapCell> nearbyCells) {
        //eat fruit
        boolean edible = false;
        for (MapCell cell : nearbyCells) {
            if (this.getX() == cell.getX() && this.getY() == cell.getY()) {
                if (cell.getType() == 7 || cell.getType() == 14 || cell.getType() == 15) { //fruit
                    Fruit fruit = (Fruit) cell;
                    if (!fruit.isEaten()) {
                        fruit.eaten();
                        edible = true;
                    }
                }
            }
        }
        super.tick(nearbyCells);
        return edible;
    }
}
