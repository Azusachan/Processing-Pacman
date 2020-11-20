package ghost;

import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ghost.Utility.findClosestMovableCell;

/**
 * Ghost which scatters to bottom right corner and chase according to chaser's vector to player
 */
public class Whim extends Ghost{
    /**
     * Reference to chaser if exists in map
     */
    Ghost chaser;
    /**
     * X vector of ghost's assumed target
     */
    public int vectorX;
    /**
     * Y vector of ghost's assumed target
     */
    public int vectorY;
    /**
     * Constructs a new instance of Whim
     * @param images a list of PImages, {@code list[0]} is the normal state, {@code list[1]} is the frightened state.
     * @param character internal parameter to determine type of the cell, see {@code MapCell} for more info
     * @param x row of the cell in map
     * @param y column of the cell in map
     * @see #Ghost
     */
    Whim(PImage[] images, int character, int x, int y) {
        super(images, character, x, y);
        this.chaser = null;
    }

    /**
     * Find target according to the state, then find appropriate route
     *
     * <p>In chase state, if chaser not exists, target will be the player. If exists, taaaarget will be double the vector
     * from chaser to 2 grid spaces ahead of the player. </p>
     * <p>In scatter state, target will be bottom right corner</p>
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
                    this.vectorX = vectorX + 8;
                    this.vectorY = vectorY + 8;
                }
                break;
            case SCATTER:
                this.target = findClosestMovableCell(448, 576, map);
                this.targetCorner = 3;
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

    /**
     * Reset {@code Ghost} properties, vectors and chaser
     */
    @Override
    public void resetPosition() {
        super.resetPosition();
        this.chaser = null;
        this.vectorX = 0;
        this.vectorY = 0;
    }
}
