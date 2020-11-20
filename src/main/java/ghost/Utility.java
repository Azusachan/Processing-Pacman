package ghost;

import java.util.*;

/**
 * Utility functions that used across multiple places
 */
public class Utility {
    private static List<MapCell> validCells;

    /**
     * Returns cell within 3x3 space of the given location from the given list
     * @param x given location x axis
     * @param y given location y axis
     * @param list list of MapCells to search from
     * @return cell within 3x3 space of the given location from the given list
     */
    public static List<MapCell> findNearbyCells(int x, int y, MapCell[][] list) {
        List<MapCell> result = new ArrayList<>();
        for (MapCell[] cellList: list) {
            for (MapCell cell: cellList) {
                // find 3x3 squares nearby
                // Returns movable cell when it is at its original position
                if (cell == null) {
                    continue;
                }
                if (cell.movable()) {
                    MovableCell movableCell = (MovableCell) cell;
                    if (Math.abs(movableCell.initialX - (x + 8)) <= 24 && Math.abs(movableCell.initialY - (y + 8)) <= 24) {
                        result.add(cell);
                    }
                } else {
                    if (Math.abs(cell.getX() - (x + 8)) <= 24 && Math.abs(cell.getY() - (y + 8)) <= 24) {
                        result.add(cell);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Find closest cell that movable cell can reach from given location from the map of the game(given)
     * @param x given location x axis
     * @param y given location y axis
     * @param map the map of current game
     * @return closest cell that movable cell can reach from given location from the map of the game
     */
    public static MapCell findClosestMovableCell(int x, int y, MapCell[][] map) {
        if (x < 0) {
            x = 0;
        } else if (x > 448) {
            x = 448;
        }
        if (y < 0) {
            y = 0;
        } else if (y > 576) {
            y = 576;
        }
        double minimumDistance = 9999;
        MapCell result = null;
        List<MapCell> cells = findValidCells(map);
        for (MapCell cell: cells) {
            double distance;
            if (cell.movable()) {
                MovableCell movableCell = (MovableCell) cell;
                distance = Math.sqrt(Math.pow(movableCell.initialX - x, 2) + Math.pow(movableCell.initialY - y, 2));
                cell = new MapCell(null, cell.getType(),
                        movableCell.initialX / 16, movableCell.initialY / 16);
            } else {
                distance = Math.sqrt(Math.pow(cell.getX() - x, 2) + Math.pow(cell.getY() - y, 2));
            }
            if (distance < minimumDistance && !cell.cannotPassThrough()) {
                minimumDistance = distance;
                result = cell;
            }
        }
        return result;
    }

    /**
     * Returns all valid cell movable cell can reach on the map
     * @param map the map of current game
     * @return all valid cell movable cell can reach on the map
     */
    public static List<MapCell> findValidCells(MapCell[][] map) {
        if (Utility.validCells != null) {
            return Utility.validCells;
        }
        List<UtilityMapCell> availableCells = new ArrayList<>();
        for (MapCell[] cellList: map) {
            for (MapCell cell: cellList) {
                availableCells.add(new UtilityMapCell(cell));
            }
        }
        UtilityMapCell player = null;
        for (UtilityMapCell cell: availableCells) {
            if (cell.cell.getType() == 8) {
                player = cell;
            }
        }
        Queue<UtilityMapCell> queue = new LinkedList<>();
        queue.add(player);
        List<MapCell> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            UtilityMapCell current = queue.remove();
            current.wentThrough = true;
            result.add(current.cell);
            List<UtilityMapCell> children = getChildren(current, availableCells);
            queue.addAll(children);
        }
        Utility.validCells = result;
        return result;
    }

    private static List<UtilityMapCell> getChildren(UtilityMapCell current, List<UtilityMapCell> availableCells) {
        List<UtilityMapCell> children = new ArrayList<>();
        for (UtilityMapCell cell: availableCells) {
            if (cell.cell.cannotPassThrough() || cell.wentThrough) {
                continue;
            }
            if (((current.cell.getX()) == cell.cell.getX() && current.cell.getY() - 16 == cell.cell.getY())
                    || (current.cell.getX()) == cell.cell.getX() && current.cell.getY() + 16 == cell.cell.getY()
                    || (current.cell.getX() - 16 == cell.cell.getX() && current.cell.getY() == cell.cell.getY())
                    || (current.cell.getX() + 16 == cell.cell.getX() && current.cell.getY() == cell.cell.getY())) {
                children.add(cell);
                }
            }
        return children;
    }
    
    private static class UtilityMapCell {
        MapCell cell;
        boolean wentThrough;

        public UtilityMapCell(MapCell cell) {
            this.cell = cell;
            this.wentThrough = false;
        }
    }
}
