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
        this.currentDirection = 0;
        this.nextDirection = 0;
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
        // turn when 180 degree
        if (Math.abs(this.nextDirection - this.currentDirection) == 2) {
            this.currentDirection = this.nextDirection;
            this.nextDirection = 0;
        }
    }

    //handles movement
    public boolean tick(List<MapCell> nearbyCells) {
        // todo: bugfix: wall not working
        // turn when appropriate
        if (this.nextDirection != 0) {
            boolean turnable = true;
            List<MapCell> nextWalkInto = this.cellWalkInto(nearbyCells, this.nextDirection);
            for (MapCell nextCell : nextWalkInto) {
                if (!nextCell.canPassThrough()) {
                    turnable = false;
                }
            }
            if (turnable) {
                this.currentDirection = this.nextDirection;
                this.nextDirection = 0;
            }
        }
        //eat fruit
        boolean eat = false;
        for (MapCell cell : nearbyCells) {
            if (this.getX() == cell.getX() && this.getY() == cell.getY()) {
                if (cell.getType() == 7 ) { //fruit
                    Fruit fruit = (Fruit) cell;
                    fruit.eaten();
                    eat = true;
                }
            }
        }
        // stop when move into wall
        boolean movable = true;
        List<MapCell> walkInto = this.cellWalkInto(nearbyCells, this.currentDirection);
        for (MapCell cell : walkInto) {
            if (!cell.canPassThrough()) {
                movable = false;
            }
        }
        if (movable) {
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
        return eat;
    }

    public List<MapCell> cellWalkInto(List<MapCell> nearbyCells, int direction) {
        List<MapCell> result = new ArrayList<>();
        for (MapCell cell : nearbyCells) {
            switch (direction) {
                // 38 = Up, 40 = Down, 37 = Left, 39 = Right
                case 38:
                    if ((this.x <= cell.getX() || this.x >= cell.getX()) && this.y - 16 == cell.getY()) {
                        if (cell.getType() == 1 || cell.getType() == 5 || cell.getType() == 6) {
                            result.add(cell);
                        }
                    }
                    break;
                case 40:
                    if ((this.x <= cell.getX() || this.x >= cell.getX()) && this.y + 16 == cell.getY()) {
                        if (cell.getType() == 1 || cell.getType() == 3 || cell.getType() == 4) {
                            result.add(cell);
                        }
                    }
                    break;
                case 37:
                    if (this.x - 16 == cell.getX() && (this.y <= cell.getY() || this.y > cell.getY())) {
                        if (cell.getType() == 2 || cell.getType() == 4 || cell.getType() == 6) {
                            result.add(cell);
                        }
                    }
                    break;
                case 39:
                    if (this.x + 16 == cell.getX() && (this.y <= cell.getY() || this.y > cell.getY())) {
                        if (cell.getType() == 2 || cell.getType() == 3 || cell.getType() == 5) {
                            result.add(cell);
                        }
                    }
                    break;
            }
        }
        return result;
    }
}
