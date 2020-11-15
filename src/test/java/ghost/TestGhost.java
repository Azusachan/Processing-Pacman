package ghost;

import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import static org.junit.jupiter.api.Assertions.*;

public class TestGhost {
    @Test
    public void testInitialize()  {
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        for (Ghost ghost: testGhostGame.ghosts) {
            assertNotNull(ghost);
            switch (ghost.type) {
                case 13:
                    Whim whim = (Whim) ghost;
                    assertNull(whim.chaser);
                    assertEquals(whim.vectorX, 0);
                    assertEquals(whim.vectorY, 0);
                    assertEquals(whim.x, 352);
                    assertEquals(whim.y, 416);
                    assertEquals(whim.initialX, 352);
                    assertEquals(whim.initialY, 416);
                    break;
                case 12:
                    Ignorant ignorant = (Ignorant) ghost;
                    assertEquals(ignorant.vectorX, 0);
                    assertEquals(ignorant.vectorY, 0);
                    assertEquals(ignorant.x, 368);
                    assertEquals(ignorant.y, 416);
                    assertEquals(ignorant.initialX, 368);
                    assertEquals(ignorant.initialY, 416);
                    break;
                case 11:
                    Chaser chaser = (Chaser) ghost;
                    assertEquals(chaser.x, 384);
                    assertEquals(chaser.y, 416);
                    assertEquals(chaser.initialX, 384);
                    assertEquals(chaser.initialY, 416);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    assertEquals(ambusher.vectorX, 0);
                    assertEquals(ambusher.vectorY, 0);
                    assertEquals(ambusher.x, 400);
                    assertEquals(ambusher.y, 416);
                    assertEquals(ambusher.initialX, 400);
                    assertEquals(ambusher.initialY, 416);
                    break;
                case 9:
                    assertEquals(ghost.x, 416);
                    assertEquals(ghost.y, 416);
                    assertEquals(ghost.initialX, 416);
                    assertEquals(ghost.initialY, 416);
                    break;
            }
            // Ghost shared attributes
            assertNotNull(ghost.ghostImage);
            assertNotNull(ghost.frightened);
            assertEquals(ghost.state, 1);
            assertEquals(ghost.previousState, 0);
            assertNull(ghost.target);
            assertNull(ghost.player);
            assertNull(ghost.route);
            assertEquals(ghost.routePointer, 0);
            assertEquals(ghost.frightenedTimer, 0);

            // MovableCell shared attributes
            assertEquals(ghost.currentDirection, 0);
            assertEquals(ghost.nextDirection, 0);
            assertEquals(ghost.speed, 1);
            assertNull(ghost.stepOnCell);
            assertNull(ghost.nextCell);
            assertNotNull(ghost.cellImage);
        }
    }

    @Test
    public void testDrawAndTick() {
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.updatePlayers(testGhostGameApp);

        for (Ghost ghost: testGhostGame.ghosts) {
            ghost.draw(testGhostGameApp);
            switch (ghost.type) {
                case 13:
                    Whim whim = (Whim) ghost;
                    whim.findChaser();
                    assertNotNull(whim.chaser);
                    assertEquals(whim.currentDirection, 37);
                    assertEquals(whim.targetCorner, 3);
                    break;
                case 12:
                    Ignorant ignorant = (Ignorant) ghost;
                    assertEquals(ignorant.currentDirection, 37);
                    assertEquals(ignorant.targetCorner, 2);
                    break;
                case 11:
                    Chaser chaser = (Chaser) ghost;
                    assertEquals(chaser.currentDirection, 37);
                    assertEquals(chaser.targetCorner, 0);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    assertEquals(ambusher.currentDirection, 37);
                    assertEquals(ambusher.targetCorner, 1);
                    break;
                case 9:
                    assertEquals(ghost.targetCorner, 3);
                    assertEquals(ghost.currentDirection, 37);
                    break;
            }
            assertNotNull(ghost.target);
            assertNotNull(ghost.route);
            assertNotNull(ghost.player);
        }
    }
}
