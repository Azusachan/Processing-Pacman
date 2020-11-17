package ghost;

import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class TestWaka {
    @Test
    public void testEquals() {
        // test different type of player equals, for detail, see Waka.equals()
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_between_cells.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        Waka player = testGhostGame.player;
        List<Integer> expected = new ArrayList<>();
        assertNotEquals(player, expected);
        player.nextCell = null;
        player.stepOnCell = null;
        MapCell currentPosition = new MapCell(null, 7, player.x / 16 + 1, player.y / 16);
        assertNotEquals(currentPosition, player);
        currentPosition = new MapCell(null, 7, player.x / 16, player.y / 16);
        assertEquals(currentPosition, player);
        currentPosition = new MapCell(null, 7, player.x / 16 + 1, player.y / 16);
        assertNotEquals(currentPosition, player);
        player.stepOnCell = currentPosition;
        assertEquals(player, currentPosition);
        player.nextCell = currentPosition;
        currentPosition = new MapCell(null, 7, player.x / 16, player.y / 16 + 1);
        assertNotEquals(player, currentPosition);
        player.nextCell = currentPosition;
        assertEquals(player, currentPosition);
    }

    @Test
    public void testReset() {
        // Test player.reset
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_between_cells.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        Waka player = testGhostGame.player;
        player.kill();
        player.closeEye();
        player.resetPosition();
        assertEquals(3, player.getLife());
        assertFalse(player.closeEye);
    }

    @Test
    public void testTurn() {
        // test player can turn when turnable and 180 degree turn
        GameManager testGhostGame = new GameManager("src/test/resources/test_player_turn.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
        testGhostGame.initMap(testGhostGameApp);
        Waka player = testGhostGame.player;
        // 180 turns
        testGhostGame.keyPressed(38);
        testGhostGame.draw(testGhostGameApp);
        testGhostGame.keyPressed(40);
        assertEquals(40, player.currentDirection);
        player.resetPosition();
        testGhostGame.keyPressed(37);
        testGhostGame.draw(testGhostGameApp);
        testGhostGame.keyPressed(39);
        assertEquals(39, player.currentDirection);

        // other turns
        player.resetPosition();
        player.x = 16;
        player.y = 400;
        testGhostGame.keyPressed(39);
        testGhostGame.draw(testGhostGameApp);
        testGhostGame.keyPressed(38);
        do {
            testGhostGame.draw(testGhostGameApp);
        } while (testGhostGame.player.currentDirection != 38);
        assertEquals(48, player.x);

        player.resetPosition();
        player.x = 16;
        player.y = 400;
        testGhostGame.keyPressed(39);
        testGhostGame.draw(testGhostGameApp);
        testGhostGame.keyPressed(40);
        do {
            testGhostGame.draw(testGhostGameApp);
        } while (testGhostGame.player.currentDirection != 40);
        assertEquals(48, player.x);

        player.resetPosition();
        player.x = 432;
        player.y = 400;
        testGhostGame.keyPressed(37);
        testGhostGame.draw(testGhostGameApp);
        testGhostGame.keyPressed(38);
        do {
            testGhostGame.draw(testGhostGameApp);
        } while (testGhostGame.player.currentDirection != 38);
        assertEquals(384, player.x);

        player.resetPosition();
        player.x = 432;
        player.y = 400;
        testGhostGame.keyPressed(37);
        testGhostGame.draw(testGhostGameApp);
        testGhostGame.keyPressed(40);
        do {
            testGhostGame.draw(testGhostGameApp);
        } while (testGhostGame.player.currentDirection != 40);
        assertEquals(384, player.x);

        player.resetPosition();
        player.x = 32;
        player.y = 384;
        testGhostGame.keyPressed(40);
        testGhostGame.draw(testGhostGameApp);
        testGhostGame.keyPressed(37);
        do {
            testGhostGame.draw(testGhostGameApp);
        } while (testGhostGame.player.currentDirection != 37);
        assertEquals(400, player.y);

        player.resetPosition();
        player.x = 32;
        player.y = 416;
        testGhostGame.keyPressed(38);
        testGhostGame.draw(testGhostGameApp);
        testGhostGame.keyPressed(37);
        do {
            testGhostGame.draw(testGhostGameApp);
        } while (testGhostGame.player.currentDirection != 37);
        assertEquals(400, player.y);

        player.resetPosition();
        player.x = 400;
        player.y = 384;
        testGhostGame.keyPressed(40);
        testGhostGame.draw(testGhostGameApp);
        testGhostGame.keyPressed(39);
        do {
            testGhostGame.draw(testGhostGameApp);
        } while (testGhostGame.player.currentDirection != 39);
        assertEquals(400, player.y);

        player.resetPosition();
        player.x = 400;
        player.y = 416;
        testGhostGame.keyPressed(38);
        testGhostGame.draw(testGhostGameApp);
        testGhostGame.keyPressed(39);
        do {
            testGhostGame.draw(testGhostGameApp);
        } while (testGhostGame.player.currentDirection != 39);
        assertEquals(400, player.y);

        // move into wall
        player.resetPosition();
        player.x = 16;
        player.y = 400;
        player.closeEye();
        testGhostGame.keyPressed(39);
        testGhostGame.draw(testGhostGameApp);
        testGhostGame.keyPressed(37);
        testGhostGame.draw(testGhostGameApp);
        assertEquals(16, player.x);
        List<MapCell> nearByCells = Utility.findNearbyCells(player.getX(), player.getY(), Ghost.map);
        player.tick(nearByCells);
        assertEquals(16, player.x);
    }

    @Test
    public void testEat() {
        // test player able to eat fruit, super fruit and soda
        GameManager testGhostGame = new GameManager("src/test/resources/test_player_eat.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGameApp.draw();
        Waka player = testGhostGame.player;
        for (Fruit fruit: testGhostGame.fruits) {
            player.x = fruit.x;
            player.y = fruit.y;
            List<MapCell> nearByCells = Utility.findNearbyCells(player.x, player.y, Ghost.map);
            // player successfully eaten fruit
            assertTrue(player.tick(nearByCells));
        }
    }
}
