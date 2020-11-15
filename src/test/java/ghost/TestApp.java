package ghost;

import processing.core.PApplet;

public class TestApp extends PApplet {
    public GameManager currentGame;
    public static final int WIDTH = 448;
    public static final int HEIGHT = 576;
    public boolean isDraw;

    public TestApp(GameManager currentGame) {
        this.currentGame = currentGame;
        this.isDraw = false;
    }

    public void setup(){
        size(WIDTH, HEIGHT);
        this.currentGame.setup(this);
    }

    public void settings() {
        size(WIDTH, HEIGHT);
    }

    public void draw() {
        if (isDraw) {
            this.currentGame.draw(this);
        }
    }

    public void keyPressed(){
        this.currentGame.keyPressed(keyCode);
    }

}
