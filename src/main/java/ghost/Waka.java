package ghost;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.List;

public class Waka extends MovableCell {
    private final PImage left;
    private final PImage right;
    private final PImage up;
    private final PImage down;
    private final PImage closed;
    private boolean closeEye;
    private int life;

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
        if (this.nextCell != null){
            return m.getX() == this.nextCell.getX() && m.getY() == this.nextCell.getY();
        } else if (this.stepOnCell != null) {
            return this.stepOnCell.getX() == m.getX() && this.stepOnCell.getY() == m.getY();
        } else {
            return super.equals(o);
        }
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
        for (int i = 0; i < this.life; i++) {
            app.image(this.right, 25 * i,545);
        }
    }

    public int getLife() {
        return this.life;
    }
    public void closeEye() {
        this.closeEye = !this.closeEye;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public boolean kill(){
        this.life--;
        return this.life == 0;
    }

    // 38 = Up, 40 = Down, 37 = Left, 39 = Right
    public void turn(int keyCode) {
        if (this.currentDirection == 0) {
            this.currentDirection = keyCode;
        } else {
            this.nextDirection = keyCode;
        }
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
                if (cell.getType() == 7) { //fruit
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
