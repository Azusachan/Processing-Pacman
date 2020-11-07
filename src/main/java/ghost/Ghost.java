package ghost;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.*;

public class Ghost extends MovableCell{
    private final PImage ghostImage;
    public static final int CHASE = 0;
    public static final int SCATTER = 1;
    public int state;
    public MapCell target;
    public MapCell[][] map;
    public Waka player;
    private List<MapCell> route;
    private int routePointer;

    Ghost(PImage image, int character, int x, int y) {
        super(image, character, x, y);
        this.ghostImage = image;
        this.state = 1;
        this.map = null;
        this.player = null;
        this.target = null;
        this.route = null;
        this.routePointer = 0;
    }

    @Override
    public void draw(PApplet app) {
        app.image(this.ghostImage, this.x - 6, this.y - 6);
    }

    @Override
    public boolean tick(List<MapCell> nearbyCells) {
        if (this.route == null) {
            this.findTarget();
        }
        MapCell current = this.route.get(this.routePointer);
        // 38 = Up, 40 = Down, 37 = Left, 39 = Right
        if (current.getX() == this.getX() && current.getY() < this.getY()) {
            this.y -= this.speed;
        } else if (current.getX() == this.getX()
                && current.getY() > this.getY()) {
            this.y += this.speed;
        } else if (current.getX() < this.getX()
                && current.getY() == this.getY()) {
            this.x -= this.speed;
        } else if (current.getX() > this.getX()
                && current.getY() == this.getY()) {
            this.x += this.speed;
        } else if (current.equals(this)) {
            if (!this.equals(this.target)) {
                this.routePointer++;
            }
        }
        //kill player
        boolean kill = false;
        if (this.player == null) {
            for (MapCell cell : nearbyCells) {
                if (this.getX() == cell.getX() && this.getY() == cell.getY()) {
                    if (cell.getType() == 8 ) { // player
                        this.player = (Waka) cell;
                        this.player.kill();
                        kill = true;
                    }
                }
            }
        } else {
            if (this.getX() == this.player.getX() && this.getY() == this.player.getY()) {
                this.player.kill();
                kill = true;
            }
        }

        return kill;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setMap(MapCell[][] map) {
        this.map = map;
    }

    public void findTarget() {
        switch (this.state) {
            case CHASE:
                if (this.player == null) {
                    for (MapCell[] cells : this.map) {
                        for (MapCell cell : cells) {
                            if (cell.getType() == 8) {
                                this.player = (Waka) cell;
                            }
                        }
                    }
                }
                this.target = this.player;
                break;
            case SCATTER:
                MapCell[] cornerCells = new MapCell[4];
                cornerCells[0] = this.map[4][1];
                cornerCells[1] = this.map[4][26];
                cornerCells[2] = this.map[32][1];
                cornerCells[3] = this.map[32][26];
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
        List<MapCell> visited = new ArrayList<>();
        queue.add(new MapCellChild(this, null));

        while (!queue.isEmpty()) {
            MapCellChild current = queue.remove();

            if (current.cell.equals(this.target)) {
                this.route = trace(current);
                this.routePointer = 0;
                return;
            } else {
                List<MapCellChild> children = getChildren(current, this.map, visited);
                queue.addAll(children);
            }
            visited.add(current.cell);
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

    public static List<MapCellChild> getChildren(MapCellChild current, MapCell[][] map, List<MapCell> visited) {
        List<MapCellChild> children = new ArrayList<>();
        for (MapCell[] cells: map) {
            for (MapCell cell: cells) {
                boolean isVisited = false;
                for (MapCell visitedCell: visited) {
                    if (cell.equals(visitedCell)) {
                        isVisited = true;
                        break;
                    }
                }
                if (!cell.canPassThrough() || isVisited) {
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
        }
        return children;
    }

    private static class MapCellChild{
        public MapCell cell;
        public MapCellChild parentCell;

        public MapCellChild(MapCell cell, MapCellChild parentCell) {
            this.cell = cell;
            this.parentCell = parentCell;
        }
    }
}
