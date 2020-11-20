package ghost;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.*;
import java.util.stream.Collectors;

import static ghost.Utility.findClosestMovableCell;

/**
 * The basic class for ghosts.
 *
 * <p>
 *     Implements a ghost described in milestone. Instances stores its image, states, target and route to target.
 * </p>
 */
public class Ghost extends MovableCell{
    /**
     * Integer for ghost to set state chase, it's public to be used in GameManager and tests
     */
    public static final int CHASE = 0;
    /**
     * Integer for ghost to set state scatter
     */
    public static final int SCATTER = 1;
    /**
     * Integer for ghost to set state frightened
     */
    public static final int FRIGHTENED = 2;
    /**
     * Integer for ghost to set state removed
     */
    public static final int REMOVED = 3;
    /**
     * Integer for ghost to set state frightened and invisible
     */
    public static final int FRIGHTENED_AND_INVISIBLE = 4;

    /**
     * Ghost's image, public to be tested
     */
    public final PImage ghostImage;
    /**
     * Ghost's frightened image
     */
    public final PImage frightened;
    /**
     * Ghost's current state in integer
     */
    public int state;
    /**
     * Ghost's current target cell
     */
    public MapCell target;
    /**
     * reference to current map
     */
    public static MapCell[][] map;
    /**
     * reference to player
     */
    public MapCell player;
    /**
     * List of cell from the cell begins search to the target cell
     */
    public List<MapCell> route;
    /**
     * Ghost's previous state
     */
    public int previousState;
    /**
     * Time require for ghost to recover from frightened and frightened and invisible state
     */
    public static int frightenedDuration;
    /**
     * The number of cell in route as ghost's next target
     */
    public int routePointer;
    /**
     * The time when ghost is frightened in milliseconds
     */
    public int frightenedTimer;
    /**
     * The corner ghost sets target as, used to simulate debug mode
     */
    public int targetCorner;

    /**
    * Constructs a new instance of ghost
    * @param images a list of PImages, {@code list[0]} is the normal state, {@code list[1]} is the frightened state.
    * @param character internal parameter to determine type of the cell, see {@code MapCell} for more info
    * @param x row of the cell in map
    * @param y column of the cell in map
    * @see #MapCell
    */
    Ghost(PImage[] images, int character, int x, int y) {
        super(images[0], character, x, y);
        this.ghostImage = images[0];
        this.frightened = images[1];
        this.state = SCATTER;
        this.player = null;
        this.target = null;
        this.route = null;
        this.routePointer = 0;
        this.frightenedTimer = 0;
    }

    /**
    * Renders ghost onto the PApplet
    *
    * <p>
    *     If the ghost is normal state, render {@code ghostImage}, if ghost is {@code FRIGHTENED}, render
     *     {@code frightened}, if ghost is {@code FRIGHTENED_AND_INVISIBLE}, render normal image with less alpha(more
     *     transparent), if ghost is {@code REMOVED}, the function does nothing.
    * </p>
    * @param app the PApplet to render the ghost
    * */
    @Override
    public void draw(PApplet app) {
        this.handleFrighten(app);
        if (this.state == FRIGHTENED) {
            app.image(this.frightened, this.x - 6, this.y - 6);
        } else if (this.state == FRIGHTENED_AND_INVISIBLE) {
            app.tint(255, 126);
            app.image(this.ghostImage, this.x - 6, this.y - 6);
            app.noTint();
        } else if (this.state != REMOVED) {
            app.image(this.ghostImage, this.x - 6, this.y - 6);
        }
    }

    /**
    * Handles movement of ghost and return if the ghost killed the player
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
     * <p> The ghost then returns if {@code player} is less or equal than 2 pixels to itself.
    *  @param nearbyCells a list of MapCell from {@code Utility.findNearbyCells()}
    * @return if the {@code player} is killed by ghost
    */
    @Override
    public boolean tick(List<MapCell> nearbyCells) {
        if (this.route == null) {
            this.findTarget();
        }

        if (this.state == FRIGHTENED || this.state == FRIGHTENED_AND_INVISIBLE) {
            this.handleMovement();
        }
        // stop when reached target (player or wall)
        if (!(this.route.size() == 0 && this.routePointer == 0)) {
            // still moving
            MapCell current = this.route.get(this.routePointer);
            if (current.getX() == this.x && current.getY() < this.y) {
                this.currentDirection = 38;
                this.y -= this.speed;
            } else if (current.getX() == this.x && current.getY() > this.y) {
                this.currentDirection = 40;
                this.y += this.speed;
            } else if (current.getX() < this.x && current.getY() == this.y) {
                this.currentDirection = 37;
                this.x -= this.speed;
            } else if (current.getX() > this.x && current.getY() == this.y) {
                this.currentDirection = 39;
                this.x += this.speed;
            } else if (current.getX() != this.x && current.getY() != this.y) {
                if (this.x > current.getX()) {
                    this.currentDirection = 37;
                    this.x -= this.speed;
                } else {
                    this.currentDirection = 39;
                    this.x += this.speed;
                }
            } else if (this.equals(current)) {
                if (!this.equals(this.target)) {
                    if (this.routePointer == this.route.size() - 1) {
                        this.findTarget();
                    } else {
                        this.routePointer++;
                    }
                } else if (this.state == FRIGHTENED || this.state == FRIGHTENED_AND_INVISIBLE) {
                    this.findTarget();
                }
            }
        }

        boolean killed = false;
        if (this.player == null) {
            this.findPlayer();
        }

        if (Math.abs(this.player.getX() - this.x) <= 2 && Math.abs(this.player.getY() - this.y) <= 2) {
            if (this.state == FRIGHTENED || this.state == REMOVED) {
                this.setState(REMOVED);
                this.findTarget();
            } else {
                killed = true;
            }
        }

        // update this.stepOn
        super.stepOn(nearbyCells);

        return killed;
    }

    /**
     * If ghost is moving back, find another location. (only happen in frightened)
     */
    // make sure ghost does not turn backwards and stop when frightened.
    public void handleMovement() {
        if (this.route.size() == 0) {
            return;
        }
        MapCell current = this.route.get(this.routePointer);
        if (current.getX() == this.x && current.getY() < this.y) {
            if (this.currentDirection == 40) {
                this.findTarget();
            }
        } else if (current.getX() == this.x && current.getY() > this.y) {
            if (this.currentDirection == 38) {
                this.findTarget();
            }
        } else if (current.getX() < this.x && current.getY() == this.y) {
            if (this.currentDirection == 39) {
                this.findTarget();
            }
        } else if (current.getX() > this.x && current.getY() == this.y) {
            if (this.currentDirection == 37) {
                this.findTarget();
            }
        }
    }

    /**
     * set the {@code state} for ghost, reset currentDirection
     * @param state Ghost.state, see {@code CHASE} and other states in {@code Ghost}
     * @see Ghost
     */
    public void setState(int state) {
        this.state = state;
        this.currentDirection = 0;
    }

    /**
     * set {@code previousState} for ghost
     * @param state Ghost.state, see {@code CHASE} and other states in {@code Ghost}
     * @see Ghost
     */
    public void setPreviousState(int state) {
        this.previousState = state;
    }

    /**
     * Set reference to map for Ghost
     * @param map List of MapCell consisting the map
     */
    public static void setMap(MapCell[][] map) {
        Ghost.map = map;
    }

    /**
     * Returns reference of current map
     * @return List of MapCell consisting the map
     */
    public static MapCell[][] getMap() {
        return map;
    }

    /**
     * Set the length of frightened state
     * @param duration length of frightened state in seconds
     */
    public static void setFrightenedDuration(int duration) {
        Ghost.frightenedDuration = duration;
    }

    /**
     * Make the ghost frighten and find new target
     */
    public void frighten(){
        this.previousState = this.state;
        this.setState(FRIGHTENED);
        this.findTarget();
    }

    /**
     * make the ghost frighten and invisible(extension Soda)
     */
    public void frightenAndInvisible(){
        this.previousState = this.state;
        this.setState(FRIGHTENED_AND_INVISIBLE);
        this.findTarget();
    }

    /**
     * Check if the ghost is no longer frightened
     * @param app PApplet of current window to get time
     */
    public void handleFrighten(PApplet app) {
        if ((this.state == FRIGHTENED || this.state == FRIGHTENED_AND_INVISIBLE) && this.frightenedTimer == 0) {
            this.frightenedTimer = app.millis();
        } else if (this.state == FRIGHTENED || this.state == FRIGHTENED_AND_INVISIBLE) {
            int delta = app.millis() - this.frightenedTimer;
            delta = delta / 1000;
            if (delta >= frightenedDuration) {
                this.frightenedTimer = 0;
                this.setState(this.previousState);
            }
        }
    }

    /**
     * Find the player of map for the ghost
     */
    public void findPlayer() {
        if (this.player == null) {
            for (MapCell[] cells : map) {
                for (MapCell cell : cells) {
                    if (cell.getType() == 8) {
                        this.player = cell;
                    }
                }
            }
        }
    }

    /**
     * Find target according to the state, then find appropriate route, the description below only applies to normal Ghost.
     *
     * <p>In chase state, the player is the target</p>
     * <p>In scatter state, ghost will find nearest corner as the target</p>
     * <p>In frightened and frightened and invisible state, ghost will choose random cell as target</p>
     * <p>In removed state, ghost will choose itself as target</p>
     */
    public void findTarget() {
        switch (this.state) {
            case CHASE:
                if (this.player == null) {
                    this.findPlayer();
                }
                this.target = this.player;
                break;
            case SCATTER:
                MapCell[] cornerCells = new MapCell[4];
                cornerCells[0] = findClosestMovableCell(0, 0, map);
                cornerCells[1] = findClosestMovableCell(448, 0, map);
                cornerCells[2] = findClosestMovableCell(0, 576, map);
                cornerCells[3] = findClosestMovableCell(448, 576, map);
                double minDistance = 729.71;
                MapCell targetCell = null;
                for (MapCell cell: cornerCells) {
                    double distance = Math.sqrt(Math.pow(cell.getX() - this.x, 2) + Math.pow(cell.getY() - this.y, 2));
                    if (distance < minDistance) {
                        minDistance = distance;
                        targetCell = cell;
                    }
                }
                this.target = targetCell;
                for (int i = 0; i < 4; i++) {
                    if (cornerCells[i] == targetCell) {
                        this.targetCorner = i;
                    }
                }
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
     * Find appropriate route for the ghost as a list of MapCell towards target.
     * <p>The algorithm will recursively trace through available cells weighted by euclidean distance to target
     * until target is found. Then it will generate a path towards target and set it as the ghost's route.</p>
     * <p>There is no "visited list" as visited cell is being removed from {@code availableCells}</p>
     * @see Ghost.MapCellChild
     * @see Ghost#getChildren
     * @see Ghost#trace
     */
    public void findRoute() {
        if (this.equals(this.target)) {
            this.route = new ArrayList<>();
            this.route.add(this);
            this.routePointer = 0;
            return;
        }

        Queue<MapCellChild> queue = new LinkedList<>();
        List<MapCell> availableCells = new ArrayList<>();
        for (MapCell[] cells: map) {
            availableCells.addAll(Arrays.asList(cells));
        }
        availableCells = availableCells.stream().filter(Objects::nonNull).collect(Collectors.toList());

        // fix issue of function when Ghost is between cells
        if (this.stepOnCell != null) {
            queue.add(new MapCellChild(this.stepOnCell, null));
        } else {
            queue.add(new MapCellChild(this, null));
        }
        while (!queue.isEmpty()) {
            MapCellChild current = queue.remove();

            if (this.target.equals(current.cell)) {
                    this.route = new ArrayList<>();
                    this.route.addAll(trace(current));
                    this.routePointer = 0;
                    return;
            } else {
                List<MapCellChild> children = getChildren(current, this.target, availableCells);
                queue.addAll(children);
            }
            availableCells.remove(current.cell);
        }
    }

    /**
     * Generates a list of MapCell from starting point to target
     *
     * <p>Accomplished by recursively go through the LinkedList of {@code MapCellChild} and add cell to the list</p>
     * @param target the {@code MapCellChild} located above target, endpoint of the route
     * @return a list of {@code MapCell} from start to end
     * @see Ghost.MapCellChild
     */
    public static List<MapCell> trace(MapCellChild target) {
        List<MapCell> path = new ArrayList<>();
        MapCellChild current = target;
        while (current.parentCell != null) {
            path.add(current.cell);
            current = current.parentCell;
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Returns a list of movable cells sort by its euclidean distance to target
     * <p>If current {@code MapCellChild} is the start point({@code current.parentCell == null}, the cell on the
     * opposite side of ghost's direction is discarded to ensure the ghost does not move backwards</p>
     * <p>Appropriate cells near current cell's four directions will be add to the list, then sorted by their euclidean
     * distance to the target. </p>
     * @param current the cell to search with
     * @param target the target of route
     * @param nearbyCells list of cell to be searched
     * @return list of appropriate child of current cell, sorted by euclidean distance to target
     * @see Ghost.MapCellChild
     */
    public List<MapCellChild> getChildren(MapCellChild current, MapCell target, List<MapCell> nearbyCells) {
        List<MapCellChild> children = new ArrayList<>();
        for (MapCell cell: nearbyCells) {
            if (cell.cannotPassThrough()) {
                continue;
            }
            if (current.parentCell == null) {
                switch (this.currentDirection) {
                    case 38:
                        if (this.x == cell.getX() && this.y + 16 == cell.getY()) {
                            continue;
                        }
                        break;
                    case 40:
                        if (this.x == cell.getX() && this.y - 16 == cell.getY()) {
                            continue;
                        }
                        break;
                    case 37:
                        if (this.x + 16 == cell.getX() && this.y == cell.getY()) {
                            continue;
                        }
                        break;
                    case 39:
                        if (this.x - 16 == cell.getX() && this.y == cell.getY()) {
                            continue;
                        }
                        break;
                }
            }
            if (cell.movable()) {
                // make a cell from its initial position
                MovableCell movableCell = (MovableCell) cell;
                cell = new MapCell(null, cell.getType(), movableCell.initialX / 16, movableCell.initialY / 16);
            }
            if (((current.cell.getX()) == cell.getX() && current.cell.getY() - 16 == cell.getY())
                        || (current.cell.getX()) == cell.getX() && current.cell.getY() + 16 == cell.getY()
                        || (current.cell.getX() - 16 == cell.getX() && current.cell.getY() == cell.getY())
                        || (current.cell.getX() + 16 == cell.getX() && current.cell.getY() == cell.getY())) {
                MapCellChild child = new MapCellChild(cell, current);
                child.distance = Math.sqrt(
                        Math.pow(child.cell.getX() - target.getX(), 2) + Math.pow(child.cell.getY() - target.getY(), 2));
                children.add(child);
            }
        }
        // Use Straight line distance to weight the branches
        children.sort(Comparator.comparing(c -> c.distance));
        return children;
    }

    /**
     * Internal class of Ghost to find route towards target
     *
     * <p>Contains current cell and parent cell. Can be used to trace a path</p>
     * <p>Contains distance to store the euclidean distance to target</p>
     * @see Ghost#findRoute
     * @see Ghost#getChildren
     * @see Ghost#trace
     */
    public static class MapCellChild{
        public MapCell cell;
        public MapCellChild parentCell;
        public double distance;

        /**
         * constructor of {@code MapCellChild}
         * @param cell Current cell
         * @param parentCell Parent Cell above the branch
         */
        public MapCellChild(MapCell cell, MapCellChild parentCell) {
            this.cell = cell;
            this.parentCell = parentCell;
        }
    }

    /**
     * Reset {@code MovableCell} properties, state(see description), target, route of current Ghost
     * <p>State will only return to {@code previousState} if ghost is frightened or frightened and invisible</p>
     */
    @Override
    public void resetPosition() {
        super.resetPosition();
        if (this.state == REMOVED || this.state == FRIGHTENED_AND_INVISIBLE) {
            this.setState(this.previousState);
        }
        this.target = null;
        this.route = new ArrayList<>();
        this.routePointer = 0;
        this.findTarget();
    }
}
