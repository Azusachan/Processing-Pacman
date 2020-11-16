package ghost;

import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ghost.Utility.findClosestMovableCell;

public class Ignorant extends Ghost{
    public int vectorX;
    public int vectorY;
    Ignorant(PImage[] images, int character, int x, int y) {
        super(images, character, x, y);
    }

    @Override
    public void findTarget() {
        switch (this.state) {
            case CHASE:
                if (this.player == null) {
                    this.findPlayer();
                }
                MovableCell player = (MovableCell) this.player;
                double distance = Math.sqrt(Math.pow(player.getX() - this.x, 2) + Math.pow(player.getY() - this.y, 2));
                if (distance > 128) {
                    this.target = findClosestMovableCell(0, 576, map);
                    this.vectorX = 0;
                    this.vectorY = 576;
                } else {
                    this.target = this.player;
                    this.vectorX = this.player.getX() + 8;
                    this.vectorY = this.player.getY() + 8;
                }
                break;
            case SCATTER:
                this.target = findClosestMovableCell(0, 576, map);
                this.targetCorner = 2;
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
}
