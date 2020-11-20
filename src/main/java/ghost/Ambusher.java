package ghost;

import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ghost.Utility.findClosestMovableCell;

/**
 * Ghost which scatters to top right corner and chase 4 grid ahead of player
 */
public class Ambusher extends Ghost {
    /**
     * X vector of ghost's assumed target
     */
    public int vectorX;
    /**
     * Y vector of ghost's assumed target
     */
    public int vectorY;

    /**
     * Constructs a new instance of Ambusher
     * @param images a list of PImages, {@code list[0]} is the normal state, {@code list[1]} is the frightened state.
     * @param character internal parameter to determine type of the cell, see {@code MapCell} for more info
     * @param x row of the cell in map
     * @param y column of the cell in map
     * @see #Ghost
     */
    Ambusher(PImage[] images, int character, int x, int y) {
        super(images, character, x, y);
    }

    /**
     * Find target according to the state, then find appropriate route
     *
     * <p>In chase state, if player is moving, target will be 4 grid ahead of player, if not, target will be the player</p>
     * <p>In scatter state, target will be top right corner</p>
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
                this.targetCorner = 1;
                break;
            case FRIGHTENED:
            case FRIGHTENED_AND_INVISIBLE:
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
