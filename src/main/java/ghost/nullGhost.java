package ghost;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class nullGhost extends Ghost{
    nullGhost() {
        super(null, 9, 0, 0);
        this.target = null;
    }

    @Override
    public void findTarget() {
        if (this.target == null) {
            List<MapCell> availableCells = new ArrayList<>();
            for (MapCell[] cells: this.map) {
                availableCells.addAll(Arrays.asList(cells).
                        parallelStream().
                        filter(cell -> !cell.cannotPassThrough()).
                        collect(Collectors.toList()));
            }
            int randomPointer = (int) ((Math.random() * (availableCells.size() - 1)));
            this.target = availableCells.get(randomPointer);
        }
        this.x = target.getX();
        this.y = target.getY();
        super.findRoute();
    }

    @Override
    public void draw(PApplet app) { }
}
