package ghost;

import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;

/**
 * MoveableCell is an abstract class for all moving cells
 * <p>It contains shared attribute between ghosts. Although it does not have an abstract method, it still cannot be
 * instantiated. </p>
 */
public abstract class MovableCell extends MapCell{
    /**
     * x axis of current cell on map
     */
    public int x;
    /**
     * y axis of current cell on map
     */
    public int y;
    /**
     * x axis of current cell's starting point on map
     */
    public final int initialX;
    /**
     * y axis of current cell's starting point on map
     */
    public final int initialY;
    /**
     * Cell's direction it moves towards
     */
    public int currentDirection;
    /**
     * Cell's next direction it wants to turn at
     */
    public int nextDirection;
    /**
     * Cell's speed
     */
    public int speed;
    /**
     * The cell current cell is steps above or behind
     * <p>If the cell moves between two cells, {@code stepOnCell} will always be the cell behind it, when the cell is
     * at starting point, it will be {@code null}. </p>
     */
    public MapCell stepOnCell;
    /**
     * The cell current cell moves into if it keeps moving
     * <p>If the cell is between two cells, {@code nextCell} will always be the cell next to it, when the cell moves into
     * wall, nextCell will be {@code null}. </p>
     */
    public MapCell nextCell;

    /**
     * Initializes attribute of MovableCell for it's children
     * @param image PImage of this cell
     * @param character Integer describing the type of cell, see {@code MapCell}
     * @param x row of the cell in map
     * @param y column of the cell in map
     * @see #MapCell
     */
    MovableCell(PImage image, int character, int x, int y) {
        super(image, character, x, y);
        this.x = x * 16;
        this.y = y * 16;
        this.initialX = this.x;
        this.initialY = this.y;
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
    public boolean movable() {
        return true;
    }

    @Override
    public boolean cannotPassThrough() {
        return false;
    }

    /**
     * Find the next cell current cell walks towards
     * @param nearbyCells A list of MapCell near the current cell
     * @param direction The direction current cell wants to move towards
     * @return List of MapCell current cell moves towards
     */
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

    /**
     * Returns the cell current cell is above, if it is not null, update {@code stepOnCell}
     * @param nearbyCells A list of MapCell near the current cell
     * @return The cell current cell steps on, or null
     */
    public MapCell stepOn(List<MapCell> nearbyCells) {
        MapCell stepOn = null;
        for (MapCell cell : nearbyCells) {
            if (cell.getX() == this.x && cell.getY() == this.y &&
                    (cell.getType() == 0 || cell.getType() == 7 || cell.getType() == 15)) {
                stepOn = cell;
            } else if (cell.movable()) {
                MovableCell movableCell = (MovableCell) cell;
                if (movableCell.initialX == this.x && movableCell.initialY == this.y) {
                    stepOn = new MapCell(null, cell.getType(),
                            movableCell.initialX / 16, movableCell.initialY / 16);
                }
            }
        }
        if (stepOn != null) {
            this.stepOnCell = stepOn;
        }
        return stepOn;
    }

    /**
     * Handles movement of player or ghost
     *
     * <p>If the player is able to turn towards its {@code nextDirection}, it will make the turn. </p>
     *
     * <p>Then it will move according to its speed. </p>
     *
     * <p>The player will determine if it eats the fruit and returns if it eats fruit</p>
     *
     * <p>If the ghost has null route, it will {@code findTarget()}. If its state is {@code
     * FRIGHTENED} or {@code FRIGHTENED_AND_INVISIBLE}, The ghost will {@code handleMovement} where it
     * tries not to go backwards.
     *
     * <p>When the ghost has empty {@code route} * and {@code routePointer} is zero, it is assumed to
     * be arrived to destination, so it will stop moving.
     *
     * <p> If not, the ghost would move accordingly using the location of the cell from {@code route.get(routePointer)}
     *
     * <p> If the ghost reached the current cell, it will increment on its {@code routePointer} to get next cell.
     *
     * <p> The ghost then returns if {@code player} is close enough to be killed.
     *
     * <p>To see relation between direction and keycode, see {@code Waka.turn}</p>
     * @param nearbyCells a list of MapCell from {@code Utility.findNearbyCells()}
     * @return If the player eats a fruit or If the {@code player} is killed by ghost or if this MoveableCell is able
     * to make the next move
     * @see Waka#turn
     */
    public boolean tick(List<MapCell> nearbyCells) {
        MapCell stepOn = this.stepOn(nearbyCells);
        //only turn when step on an air or fruit cell, not half way between cells
        if (this.currentDirection == 0 || (this.nextDirection != 0 && stepOn != null)) {
            boolean turnable = true;
            List<MapCell> nextWalkInto = this.cellWalkInto(nearbyCells, this.nextDirection);
            for (MapCell nextCell : nextWalkInto) {
                if (nextCell.cannotPassThrough()) {
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
            if (cell.cannotPassThrough()) {
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

    /**
     * Reset current location and other attributes to initial.
     *
     * <p>The player will reset its eye and life</p>
     *
     * <p>The ghost will reset its target and recover from removed or removed and frightened state. </p>
     */
    public void resetPosition() {
        this.x = this.initialX;
        this.y = this.initialY;
        this.currentDirection = 0;
        this.nextDirection = 0;
        this.stepOnCell = null;
        this.nextCell = null;
    }
}
