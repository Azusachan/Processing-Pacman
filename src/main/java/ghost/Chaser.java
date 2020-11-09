package ghost;

import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ghost.Utility.findClosestMovableCell;

public class Chaser extends Ghost{
    Chaser(PImage[] images, int character, int x, int y) {
        super(images, character, x, y);
    }

    @Override
    public void findTarget() {
        switch (this.state) {
            case CHASE:
                if (this.player == null) {
                    this.findPlayer();
                }
                this.target = this.player;
                break;
            case SCATTER:
                this.target = findClosestMovableCell(0, 0, map);
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
                this.targetCorner = 1;
                break;
            case REMOVED:
                this.target = this;
        }
        this.findRoute();
    }
}
