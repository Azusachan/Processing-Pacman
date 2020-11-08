package ghost;

import processing.core.PImage;

import java.util.List;

public class SuperFruit extends Fruit{
    private static List<Ghost> ghosts;
    SuperFruit(PImage image, int character, int x, int y) {
        super(image, character, x, y);
    }

    public static void setGhost(List<Ghost> ghostsList) {
        ghosts = ghostsList;
    }

    @Override
    public void eaten(){
        super.eaten();
        ghosts.parallelStream().forEach(Ghost::frighten);
    }
}
