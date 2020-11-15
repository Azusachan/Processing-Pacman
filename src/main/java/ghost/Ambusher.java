package ghost;

import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ghost.Utility.findClosestMovableCell;

public class Ambusher extends Ghost {
    public int vectorX;
    public int vectorY;
    Ambusher(PImage[] images, int character, int x, int y) {
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
                switch (player.currentDirection) {
                    case 0:
                        this.target = this.player;
                        break;
                    case 38:
                        this.target = findClosestMovableCell(player.getX(), player.getY() - 64, map);
                        this.vectorX = player.getX() + 8;
                        this.vectorY = player.getY() - 56;
                        break;
                    case 40:
                        this.target = findClosestMovableCell(player.getX(), player.getY() + 64, map);
                        this.vectorX = player.getX() + 8;
                        this.vectorY = player.getY() + 72;
                        break;
                    case 37:
                        this.target = findClosestMovableCell(player.getX() - 64, player.getY(), map);
                        this.vectorX = player.getX() - 56;
                        this.vectorY = player.getY() + 8;
                        break;
                    case 39:
                        this.target = findClosestMovableCell(player.getX() + 64, player.getY(), map);
                        this.vectorX = player.getX() + 72;
                        this.vectorY = player.getY() + 8;
                        break;
                }
                break;
            case SCATTER:
                this.target = findClosestMovableCell(448, 0, map);
                this.targetCorner = 0;
                break;
            case FRIGHTENED:
                List<MapCell> availableCells = new ArrayList<>();
                for (MapCell[] cells: getMap()) {
                    availableCells.addAll(Arrays.asList(cells).
                            parallelStream().
                            filter(cell -> !cell.cannotPassThrough()).
                            collect(Collectors.toList()));
                }
                availableCells.remove(null);
                int randomPointer = (int) ((Math.random() * (availableCells.size() - 1)));
                this.target = availableCells.get(randomPointer);
                break;
            case REMOVED:
                this.target = this;
        }
        this.findRoute();
    }
}
