package ghost;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.*;
import java.util.stream.Collectors;

import static ghost.Utility.findClosestMovableCell;


public class Ghost extends MovableCell{
    private final PImage ghostImage;
    private final PImage frightened;
    public static final int CHASE = 0;
    public static final int SCATTER = 1;
    public static final int FRIGHTENED = 2;
    public static final int REMOVED = 3;
    public int state;
    public MapCell target;
    private static MapCell[][] map;
    public MapCell player;
    private List<MapCell> route;
    public int previousState;
    private static int frightenedDuration;
    private int routePointer;
    private int frightenedTimer;

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
        } else if (this.state != REMOVED) {
            app.image(this.ghostImage, this.x - 6, this.y - 6);
        }
    }

    @Override
    public boolean tick(List<MapCell> nearbyCells) {
        if (this.route == null) {
            this.findTarget();
        }

        // stop when reached target (player or wall)
        if (!(this.route.size() == 0 && this.routePointer == 0)) {
            // still moving
            MapCell current = this.route.get(this.routePointer);
            // 38 = Up, 40 = Down, 37 = Left, 39 = Right
            if (current.getX() == this.getX() && current.getY() < this.getY()) {
                this.y -= this.speed;
            } else if (current.getX() == this.getX() && current.getY() > this.getY()) {
                this.y += this.speed;
            } else if (current.getX() < this.getX() && current.getY() == this.getY()) {
                this.x -= this.speed;
            } else if (current.getX() > this.getX() && current.getY() == this.getY()) {
                this.x += this.speed;
            } else if (current.getX() != this.getX() && current.getY() != this.getY()) {
                // happens when ghost is between cells and refresh route list
                this.route.add(0, this.stepOnCell);
                this.routePointer = 0;
            } else if (current.equals(this)) {
                if (!this.equals(this.target)) {
                    if (this.routePointer == this.route.size() - 1) {
                        this.findTarget();
                    } else {
                        this.routePointer++;
                    }
                } else if (this.state == FRIGHTENED) {
                    this.findTarget();
                }
            }
        }

        boolean killed = false;
        if (this.player == null) {
            this.findPlayer();
        }

        if (Math.abs(this.player.getX() - this.x) <= 1 && Math.abs(this.player.getY() - this.y) <= 1) {
            if (this.state == FRIGHTENED || this.state == REMOVED) {
                this.state = REMOVED;
                this.findTarget();
            } else {
                killed = true;
            }
        }

        // update this.stepOn
        super.stepOn(nearbyCells);

        return killed;
    }

    public void setState(int state) {
        this.state = state;
    }

    public static void setMap(MapCell[][] map) {
        Ghost.map = map;
    }

    public static MapCell[][] getMap() {
        return map;
    }

    public static void setFrightenedDuration(int duration) {
        frightenedDuration = duration;
    }

    public void frighten(){
        this.previousState = this.state;
        this.state = FRIGHTENED;
        this.findTarget();
    }

    public void handleFrighten(PApplet app) {
        if (this.state == FRIGHTENED && this.frightenedTimer == 0) {
            this.frightenedTimer = app.millis();
        } else if (this.state == FRIGHTENED) {
            int delta = app.millis() - this.frightenedTimer;
            delta = delta / 1000;
            if (delta >= frightenedDuration) {
                this.frightenedTimer = 0;
                this.state = previousState;
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
                cornerCells[1] = findClosestMovableCell(0, 448, map);
                cornerCells[2] = findClosestMovableCell(576, 0, map);
                cornerCells[3] = findClosestMovableCell(576, 448, map);
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
                break;
            case FRIGHTENED:
                List<MapCell> availableCells = new ArrayList<>();
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
        if (this.equals(target)) {
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

        // fix issue of function when Ghost is between cells
        if (this.stepOnCell != null) {
            queue.add(new MapCellChild(this.stepOnCell, null));
        } else {
            queue.add(new MapCellChild(this, null));
        }
        while (!queue.isEmpty()) {
            MapCellChild current = queue.remove();

            if (this.target.equals(current.cell)) {
                    this.route = trace(current);
                    this.routePointer = 0;
                    return;
            } else {
                List<MapCell> nearbyCells = findAvailableNearbyCells(current.cell.getX(), current.cell.getY(), availableCells);
                List<MapCellChild> children = getChildren(current, nearbyCells);
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

    public static List<MapCellChild> getChildren(MapCellChild current, List<MapCell> nearbyCells) {
        List<MapCellChild> children = new ArrayList<>();
            for (MapCell cell: nearbyCells) {
                if (cell.cannotPassThrough()) {
                    continue;
                }

                if (((current.cell.getX()) == cell.getX() && current.cell.getY() - 16 == cell.getY())
                            || (current.cell.getX()) == cell.getX() && current.cell.getY() + 16 == cell.getY()
                            || (current.cell.getX() - 16 == cell.getX() && current.cell.getY() == cell.getY())
                            || (current.cell.getX() + 16 == cell.getX() && current.cell.getY() == cell.getY())) {
                    if (cell.getType() == 8 || cell.getType() == 9) {
                        // make a cell that does not move around creating random bugs
                        cell = new MapCell(null, cell.getType(), cell.getX() / 16, cell.getY() / 16);
                    }
                    MapCellChild child = new MapCellChild(cell, current);
                    children.add(child);
                }
            }
        return children;
    }

    public static List<MapCell> findAvailableNearbyCells(int x, int y, List<MapCell> availableCells) {
        List<MapCell> result = new ArrayList<>();
        for (MapCell cell: availableCells) {
            // find 3x3 squares nearby
            if (Math.abs(cell.getX() - (x + 8)) <= 24 && Math.abs(cell.getY() - (y + 8)) <= 24) {
                result.add(cell);
            }
        }
        return result;
    }

    private static class MapCellChild{
        public MapCell cell;
        public MapCellChild parentCell;

        public MapCellChild(MapCell cell, MapCellChild parentCell) {
            this.cell = cell;
            this.parentCell = parentCell;
        }
    }

    @Override
    public void resetPosition() {
        super.resetPosition();
        if (this.state == REMOVED) {
            this.state = previousState;
        }
        this.target = null;
        this.route = null;
        this.routePointer = 0;
    }
}
