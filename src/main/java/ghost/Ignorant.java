package ghost;

import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ghost.Utility.findClosestMovableCell;
/**
 * Ghost which scatters to bottom left corner and chase towards player only if it is more than 8 units away
 */
public class Ignorant extends Ghost{
    /**
     * X vector of ghost's assumed target
     */
    public int vectorX;
    /**
     * Y vector of ghost's assumed target
     */
    public int vectorY;
    /**
     * Constructs a new instance of Ignorant
     * @param images a list of PImages, {@code list[0]} is the normal state, {@code list[1]} is the frightened state.
     * @param character internal parameter to determine type of the cell, see {@code MapCell} for more info
     * @param x row of the cell in map
     * @param y column of the cell in map
     * @see #Ghost
     */
    Ignorant(PImage[] images, int character, int x, int y) {
        super(images, character, x, y);
    }

    /**
     * Find target according to the state, then find appropriate route
     *
     * <p>In chase state, if player is more than 8 units away, target will be the player, if not, target location is bottom left corner</p>
     * <p>In scatter state, target will be bottom left corner</p>
     * <p>In frightened and frightened and invisible state, ghost will choose random cell as target</p>
     * <p>In removed state, ghost will choose itself as target</p>
     * <p>{@code vectorX} and {@code vectorY} is used to simulate debug mode in description</p>
     */
    @Override
    public void findTarget() {
        switch (this.state) {
            case CHASE:
                if (this.player == null) {
                    this.findPlayer();
                }
                MovableCell player = (MovableCell) this.player;
                double distance = Math.sqrt(Math.pow(player.getX() - this.x, 2) + Math.pow(player.getY() - this.y, 2));
                if (distance < 128) {
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

    /**
     * Reset {@code Ghost} properties and vectors
     */
    @Override
    public void resetPosition() {
        super.resetPosition();
        this.vectorX = 0;
        this.vectorY = 0;
    }
}
