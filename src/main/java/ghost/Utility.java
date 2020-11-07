package ghost;

import java.util.ArrayList;
import java.util.List;

public class Utility {
    public static List<MapCell> findNearbyCells(int x, int y, MapCell[][] list) {
        List<MapCell> result = new ArrayList<>();
        for (MapCell[] cellList: list) {
            for (MapCell cell: cellList) {
                // find 3x3 squares nearby
                if (Math.abs(cell.getX() - (x + 8)) <= 24 && Math.abs(cell.getY() - (y + 8)) <= 24) {
                    result.add(cell);
                }
            }
        }
        return result;
    }
}
