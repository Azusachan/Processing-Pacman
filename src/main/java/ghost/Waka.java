package ghost;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;

public class Waka extends MapCell {
    private int x;
    private int y;
    private int currentDirection;
    private int nextDirection;
    private int speed;
    private final PImage left;
    private final PImage right;
    private final PImage up;
    private final PImage down;
    private final PImage closed;
    private boolean closeEye;

    Waka(PImage[] images, int character, int x, int y) {
        super(images[0], character, x, y);
        this.x = x * 16;
        this.y = y * 16;
        this.left = images[0];
        this.right = images[1];
        this.up = images[2];
        this.down = images[3];
        this.closed = images[4];
        this.closeEye = false;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
    @Override
    public boolean canPassThrough() {
        return true;
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
                case 39:
                    app.image(this.right, this.x - 4, this.y - 5);
                    break;
                default:
                    app.image(this.right, this.x - 4, this.y - 5);
            }
        }
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    public void closeEye() {
        this.closeEye = !this.closeEye;
    }

    // 38 = Up, 40 = Down, 37 = Left, 39 = Right
    public void turn(int keyCode) {
        if (this.currentDirection == 0) {
            this.currentDirection = keyCode;
        } else {
            this.nextDirection = keyCode;
        }
    }

    //handles movement
    public void tick() {
        switch (this.currentDirection) {
            case 38:
                this.y -= this.speed;
                break;
            case 40:
                this.y += this.speed;
                break;
            case 37:
                this.x -= this.speed;
                break;
            case 39:
                this.x += this.speed;
                break;
        }
    }
}
