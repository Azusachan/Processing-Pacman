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
        List<MapCell> cells = new ArrayList<>();
        for (MapCell[] cellList: list) {
            boolean isValidCell = false;
            for (MapCell cell : cellList) {
                if (isValidCell) {
                    cells.add(cell);
                }
                if (cell.getType() >= 2 && cell.getType() <= 6) {
                    isValidCell = !isValidCell;
                }
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
