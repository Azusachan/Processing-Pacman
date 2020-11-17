package ghost;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.*;
import java.util.stream.Collectors;

import static ghost.Utility.findClosestMovableCell;


public class Ghost extends MovableCell{
    public static final int CHASE = 0;
    public static final int SCATTER = 1;
    public static final int FRIGHTENED = 2;
    public static final int REMOVED = 3;
    public static final int FRIGHTENED_AND_INVISIBLE = 4;

    public final PImage ghostImage;
    public final PImage frightened;
    public int state;
    public MapCell target;
    public static MapCell[][] map;
    public MapCell player;
    public List<MapCell> route;
    public int previousState;
    public static int frightenedDuration;
    public int routePointer;
    public int frightenedTimer;
    public int targetCorner;

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

    public void setState(int state) {
        this.state = state;
        this.currentDirection = 0;
    }

    public void setPreviousState(int state) {
        this.previousState = state;
    }

    public static void setMap(MapCell[][] map) {
        Ghost.map = map;
    }

    public static MapCell[][] getMap() {
        return map;
    }

    public static void setFrightenedDuration(int duration) {
        Ghost.frightenedDuration = duration;
    }

    public void frighten(){
        this.previousState = this.state;
        this.setState(FRIGHTENED);
        this.findTarget();
    }

    public void frightenAndInvisible(){
        this.previousState = this.state;
        this.setState(FRIGHTENED_AND_INVISIBLE);
        this.findTarget();
    }

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

    public static class MapCellChild{
        public MapCell cell;
        public MapCellChild parentCell;
        public double distance;

        public MapCellChild(MapCell cell, MapCellChild parentCell) {
            this.cell = cell;
            this.parentCell = parentCell;
        }
    }

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
