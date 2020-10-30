package ghost;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.List;

public class Ghost extends MapCell{
    private int x;
    private int y;
    private final PImage ghostImage;
    Ghost(PImage image, int character, int x, int y) {
        super(image, character, x, y);
        this.ghostImage = image;
        this.x = x * 16;
        this.y = y * 16;
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public void draw(PApplet app) {
        app.image(this.ghostImage, this.x - 6, this.y - 6);
    }

    public boolean tick(List<MapCell> nearbyCells) {
        MapCell stepOn = null;
        for (MapCell cell : nearbyCells) {
            if (cell.getX() == this.x && cell.getY() == this.y && (cell.getType() == 0 || cell.getType() == 7)) {
                stepOn = cell;
            }
        }
//        //only turn when step on an air or fruit cell, not half way between cells
//        if (this.nextDirection != 0 && stepOn != null) {
//            boolean turnable = true;
//            List<MapCell> nextWalkInto = this.cellWalkInto(nearbyCells, this.nextDirection);
//            for (MapCell nextCell : nextWalkInto) {
//                if (!nextCell.canPassThrough()) {
//                    turnable = false;
//                }
//            }
//            if (turnable) {
//                this.currentDirection = this.nextDirection;
//                this.nextDirection = 0;
//            }
//        }

        //eat fruit
        boolean kill = false;
        for (MapCell cell : nearbyCells) {
            if (this.getX() == cell.getX() && this.getY() == cell.getY()) {
                if (cell.getType() == 8 ) { //player
                    Waka player = (Waka) cell;
                    player.kill();
                    kill = true;
                }
            }
        }
        // stop when move into wall
//        boolean movable = true;
//        List<MapCell> walkInto = this.cellWalkInto(nearbyCells, this.currentDirection);
//        for (MapCell cell : walkInto) {
//            if (!cell.canPassThrough()) {
//                movable = false;
//            }
//        }
//        if (movable) {
//            switch (this.currentDirection) {
//                case 38:
//                    this.y -= this.speed;
//                    break;
//                case 40:
//                    this.y += this.speed;
//                    break;
//                case 37:
//                    this.x -= this.speed;
//                    break;
//                case 39:
//                    this.x += this.speed;
//                    break;
//            }
//        }
        return kill;
    }

}
