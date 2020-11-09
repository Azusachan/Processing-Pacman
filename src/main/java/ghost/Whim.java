package ghost;

import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ghost.Utility.findClosestMovableCell;

public class Whim extends Ghost{
    Ghost chaser;
    public int vectorX;
    public int vectorY;
    Whim(PImage[] images, int character, int x, int y) {
        super(images, character, x, y);
        this.chaser = null;
    }

    @Override
    public void findTarget() {
        switch (this.state) {
            case CHASE:
                if (this.player == null) {
                    this.findPlayer();
                }
                if (this.chaser == null) {
                    this.findChaser();
                }
                MovableCell player = (MovableCell) this.player;
                // When chaser does not exist or removed, target at player
                if (this.chaser == null || this.chaser.state == REMOVED) {
                    this.target = this.player;
                } else {
                    int vectorX;
                    int vectorY;
                    switch (player.currentDirection) {
                        case 38:
                            vectorX = (player.getX() - this.chaser.x) * 2 + this.chaser.x;
                            vectorY = (player.getY() - 16 - this.chaser.y) * 2 + this.chaser.y;
                            break;
                        case 40:
                            vectorX = (player.getX() - this.chaser.x) * 2 + this.chaser.x;
                            vectorY = (player.getY() + 16 - this.chaser.y) * 2 + this.chaser.y;
                            break;
                        case 37:
                            vectorX = (player.getX() - 16 - this.chaser.x) * 2 + this.chaser.x;
                            vectorY = (player.getY() - this.chaser.y) * 2 + this.chaser.y;
                            break;
                        case 39:
                            vectorX = (player.getX() + 16 - this.chaser.x) * 2 + this.chaser.x;
                            vectorY = (player.getY() - this.chaser.y) * 2 + this.chaser.y;
                            break;
                        default:
                            vectorX = (player.getX() - this.chaser.x) * 2 + this.chaser.x;
                            vectorY = (player.getY() - this.chaser.y) * 2 + this.chaser.y;
                            break;
                    }
                    this.target = findClosestMovableCell(vectorX, vectorY, map);
                    this.vectorX = vectorX;
                    this.vectorY = vectorY;
                }
                break;
            case SCATTER:
                this.target = findClosestMovableCell(448, 576, map);
                this.targetCorner = 3;
                break;
            case FRIGHTENED:
                List<MapCell> availableCells = new ArrayList<>();

                // choose random cell
                for (MapCell[] cells: getMap()) {
                    availableCells.addAll(Arrays.asList(cells).
                            parallelStream().
                            filter(cell -> !cell.cannotPassThrough()).
                            collect(Collectors.toList()));
                }
                int randomPointer = (int) ((Math.random() * (availableCells.size() - 1)));
                this.target = availableCells.get(randomPointer);
                break;
            case REMOVED:
                this.target = this;
        }
        this.findRoute();
    }

    public void findChaser() {
        if (this.chaser == null) {
            for (MapCell[] cells : map) {
                for (MapCell cell : cells) {
                    if (cell.getType() == 11) {
                        this.chaser = (Ghost) cell;
                    }
                }
            }
        }
    }

    @Override
    public void resetPosition() {
        super.resetPosition();
        this.chaser = null;
        this.vectorX = 0;
        this.vectorY = 0;
    }
}
