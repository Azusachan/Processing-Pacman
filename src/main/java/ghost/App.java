package ghost;

import processing.core.PApplet;

public class App extends PApplet {
    Game currentGame;
    public static final int WIDTH = 448;
    public static final int HEIGHT = 576;

    public App() {
        currentGame = new Game();
    }

    public void setup(){
        frameRate(60);
        background(0, 0, 0);
        size(WIDTH, HEIGHT);
        this.currentGame.setup(this);
    }

    public void settings() {
        size(WIDTH, HEIGHT);
    }

    public void draw() {
        this.currentGame.draw(this);
    }

    public void keyPressed(){
        this.currentGame.keyPressed(keyCode);
    }

    public static void main(String[] args) {
        PApplet.main("ghost.App");
    }
}
