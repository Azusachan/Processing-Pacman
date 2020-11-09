package ghost;

import java.util.ArrayList;
import java.util.List;

public class Utility {
    public static List<MapCell> findNearbyCells(int x, int y, MapCell[][] list) {
        List<MapCell> result = new ArrayList<>();
        for (MapCell[] cellList: list) {
            for (MapCell cell: cellList) {
                // find 3x3 squares nearby
                // Returns movable cell when it is at its original position
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

    // only supports map that has same width as game window
    public static MapCell findClosestMovableCell(int x, int y, MapCell[][] list) {
        if (x < 0 || y < 0) {
            return null;
        }
        double minimumDistance = 9999;
        MapCell result = null;
        boolean isValid = false;
        List<MapCell> cells = new ArrayList<>();
        for (MapCell[] cellList: list) {
            int counter = 0;
            for (MapCell cell : cellList) {
                if (cell.getType() == 1) {
                    counter++;
                }
                if (isValid) {
                    cells.add(cell);
                }
            }
            if (counter != 0 && !isValid) {
                isValid = true;
            } else if (counter == 0 && isValid) {
                isValid = false;
            }
        }
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
}
