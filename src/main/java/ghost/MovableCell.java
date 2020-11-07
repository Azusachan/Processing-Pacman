package ghost;

import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;

public class MovableCell extends MapCell{
    public int x;
    public int y;
    public int currentDirection;
    public int nextDirection;
    public int speed;
    public MapCell stepOnCell;
    public MapCell nextCell;
    MovableCell(PImage image, int character, int x, int y) {
        super(image, character, x, y);
        this.x = x * 16;
        this.y = y * 16;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    public List<MapCell> cellWalkInto(List<MapCell> nearbyCells, int direction) {
        List<MapCell> result = new ArrayList<>();
        for (MapCell cell : nearbyCells) {
            switch (direction) {
                // 38 = Up, 40 = Down, 37 = Left, 39 = Right
                case 38:
                    if (this.x == cell.getX() && this.y - 16 == cell.getY()) {
                        result.add(cell);
                    }
                    break;
                case 40:
                    if (this.x == cell.getX() && this.y + 16 == cell.getY()) {
                        result.add(cell);
                    }
                    break;
                case 37:
                    if (this.x - 16 == cell.getX() && this.y == cell.getY()) {
                        result.add(cell);
                    }
                    break;
                case 39:
                    if (this.x + 16 == cell.getX() && this.y == cell.getY()) {
                        result.add(cell);
                    }
                    break;
            }
        }
        return result;
    }

    public MapCell stepOn(List<MapCell> nearbyCells) {
        MapCell stepOn = null;
        for (MapCell cell : nearbyCells) {
            if (cell.getX() == this.x && cell.getY() == this.y && (cell.getType() == 0 || cell.getType() == 7)) {
                stepOn = cell;
            }
        }
        if (stepOn != null) {
            this.stepOnCell = stepOn;
        }
        return stepOn;
    }
    public boolean tick(List<MapCell> nearbyCells) {
        MapCell stepOn = this.stepOn(nearbyCells);
        //only turn when step on an air or fruit cell, not half way between cells
        if (this.nextDirection != 0 && stepOn != null) {
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
        // stop when move into wall
        boolean movable = true;
        List<MapCell> walkInto = this.cellWalkInto(nearbyCells, this.currentDirection);
        for (MapCell cell : walkInto) {
            this.nextCell = cell;
            if (!cell.canPassThrough()) {
                this.nextCell = null;
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
        return movable;
    }
}
