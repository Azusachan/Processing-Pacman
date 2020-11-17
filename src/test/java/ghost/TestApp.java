package ghost;

public class TestApp extends App {
    public boolean isDraw;

    public TestApp(GameManager currentGame) {
        this.currentGame = currentGame;
        this.isDraw = false;
    }

    @Override
    public void setup(){
        this.currentGame.setup(this);
    }

    @Override
    public void draw() {
        if (isDraw) {
            super.draw();
        }
    }

}
