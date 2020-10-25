package ghost;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.List;

public class Ghost extends MovableCell{
    private final PImage ghostImage;
    Ghost(PImage image, int character, int x, int y) {
        super(image, character, x, y);
        this.ghostImage = image;
    }

    @Override
    public void draw(PApplet app) {
        app.image(this.ghostImage, this.x - 6, this.y - 6);
    }

    @Override
    public boolean tick(List<MapCell> nearbyCells) {
        MapCell stepOn = null;
        boolean movable = super.tick(nearbyCells);
        for (MapCell cell : nearbyCells) {
            if (cell.getX() == this.x && cell.getY() == this.y && (cell.getType() == 0 || cell.getType() == 7)) {
                stepOn = cell;
            }
        }

        //eat fruit
        boolean kill = false;
        for (MapCell cell : nearbyCells) {
            if (this.getX() == cell.getX() && this.getY() == cell.getY()) {
                if (cell.getType() == 8 ) { //player
                    Waka player = (Waka) cell;
                    player.kill();
                    kill = true;
                }
            }
        }
        return kill;
    }
}
