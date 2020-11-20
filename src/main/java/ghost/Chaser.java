package ghost;

import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ghost.Utility.findClosestMovableCell;
/**
 * Ghost which scatters to top left corner and chase towards player
 */
public class Chaser extends Ghost{
    /**
     * Constructs a new instance of Chaser
     * @param images a list of PImages, {@code list[0]} is the normal state, {@code list[1]} is the frightened state.
     * @param character internal parameter to determine type of the cell, see {@code MapCell} for more info
     * @param x row of the cell in map
     * @param y column of the cell in map
     * @see #Ghost
     */
    Chaser(PImage[] images, int character, int x, int y) {
        super(images, character, x, y);
    }

    /**
     * Find target according to the state, then find appropriate route
     *
     * <p>In chase state, target will be the player</p>
     * <p>In scatter state, target will be top left corner</p>
     * <p>In frightened and frightened and invisible state, ghost will choose random cell as target</p>
     * <p>In removed state, ghost will choose itself as target</p>
     */
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
                this.targetCorner = 0;
                break;
            case FRIGHTENED:
            case FRIGHTENED_AND_INVISIBLE:
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
