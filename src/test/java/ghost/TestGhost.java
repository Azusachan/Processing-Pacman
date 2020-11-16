package ghost;

import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import java.util.List;

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
                    assertEquals(0, whim.vectorX);
                    assertEquals(0, whim.vectorY);
                    assertEquals(352, whim.x);
                    assertEquals(416, whim.y);
                    assertEquals(352, whim.initialX);
                    assertEquals(416, whim.initialY);
                    break;
                case 12:
                    Ignorant ignorant = (Ignorant) ghost;
                    assertEquals(0, ignorant.vectorX);
                    assertEquals(0, ignorant.vectorY);
                    assertEquals(368, ignorant.x);
                    assertEquals(416, ignorant.y);
                    assertEquals(368, ignorant.initialX);
                    assertEquals(416, ignorant.initialY);
                    break;
                case 11:
                    Chaser chaser = (Chaser) ghost;
                    assertEquals(384, chaser.x);
                    assertEquals(416, chaser.y);
                    assertEquals(384, chaser.initialX);
                    assertEquals(416, chaser.initialY);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    assertEquals(0, ambusher.vectorX);
                    assertEquals(0, ambusher.vectorY);
                    assertEquals(400, ambusher.x);
                    assertEquals(416, ambusher.y);
                    assertEquals(400, ambusher.initialX);
                    assertEquals(416, ambusher.initialY);
                    break;
                case 9:
                    assertEquals(416, ghost.x);
                    assertEquals(416, ghost.y);
                    assertEquals(416, ghost.initialX);
                    assertEquals(416, ghost.initialY);
                    break;
            }
            // Ghost shared attributes
            assertNotNull(ghost.ghostImage);
            assertNotNull(ghost.frightened);
            assertEquals(1, ghost.state);
            assertEquals(0, ghost.previousState);
            assertNull(ghost.target);
            assertNull(ghost.player);
            assertNull(ghost.route);
            assertEquals(0, ghost.routePointer);
            assertEquals(0, ghost.frightenedTimer);

            // MovableCell shared attributes
            assertEquals(0, ghost.currentDirection);
            assertEquals(0, ghost.nextDirection);
            assertEquals(1, ghost.speed);
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
        testGhostGame.initMap(testGhostGameApp);

        for (Ghost ghost: testGhostGame.ghosts) {
            ghost.draw(testGhostGameApp);
            switch (ghost.type) {
                case 13:
                    Whim whim = (Whim) ghost;
                    whim.findChaser();
                    assertNotNull(whim.chaser);
                    assertEquals(37, whim.currentDirection);
                    assertEquals(3, whim.targetCorner);
                    break;
                case 12:
                    Ignorant ignorant = (Ignorant) ghost;
                    assertEquals(37, ignorant.currentDirection);
                    assertEquals(2, ignorant.targetCorner);
                    break;
                case 11:
                    Chaser chaser = (Chaser) ghost;
                    assertEquals(37, chaser.currentDirection);
                    assertEquals(0, chaser.targetCorner);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    assertEquals(37, ambusher.currentDirection);
                    assertEquals(1, ambusher.targetCorner);
                    break;
                case 9:
                    assertEquals(3, ghost.targetCorner);
                    assertEquals(37, ghost.currentDirection);
                    break;
            }
            assertNotNull(ghost.target);
            assertNotNull(ghost.route);
            assertNotNull(ghost.player);
        }
    }

    @Test
    public void testChase() {
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.parallelStream().forEach(ghost -> ghost.state = 0);
        testGhostGame.ghosts.parallelStream().forEach(Ghost::findTarget);
        for (Ghost ghost: testGhostGame.ghosts) {
            ghost.draw(testGhostGameApp);
            switch (ghost.type) {
                case 13:
                    Whim whim = (Whim) ghost;
                    whim.findChaser();
                    assertNotNull(whim.chaser);
                    assertEquals(39, whim.currentDirection);
                    break;
                case 12:
                    Ignorant ignorant = (Ignorant) ghost;
                    assertEquals(37, ignorant.currentDirection);
                    break;
                case 11:
                    Chaser chaser = (Chaser) ghost;
                    assertEquals(37, chaser.currentDirection);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    assertEquals(40, ambusher.currentDirection);
                    assertEquals(1, ambusher.targetCorner);
                    break;
                case 9:
                    assertEquals(0, ghost.targetCorner);
                    assertEquals(40, ghost.currentDirection);
                    break;
            }
            ghost.player = null;
            ghost.state = 0;
            ghost.findTarget();
            assertNotNull(ghost.player);
        }
    }

    @Test
    public void testChaseExtendedUp() {
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase_extended.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.parallelStream().forEach(ghost -> ghost.state = 0);

        testGhostGame.initMap(testGhostGameApp);
        for (Ghost ghost: testGhostGame.ghosts) {
            MovableCell player = (MovableCell) ghost.player;
            player.currentDirection = 38;
            ghost.draw(testGhostGameApp);
            switch (ghost.type) {
                case 13:
                    Whim whim = (Whim) ghost;
                    whim.findChaser();
                    whim.findTarget();
                    assertEquals(39, whim.currentDirection);
                    break;
                case 12:
                    Ignorant ignorant = (Ignorant) ghost;
                    // Ignoring the player
                    assertEquals(37, ignorant.currentDirection);
                    break;
                case 11:
                    Chaser chaser = (Chaser) ghost;
                    assertEquals(40, chaser.currentDirection);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    ambusher.findTarget();
                    assertEquals(40, ambusher.currentDirection);
                    break;
                case 9:
                    assertEquals(40, ghost.currentDirection);
                    break;
            }
        }
    }

    @Test
    public void testChaseExtendedDown() {
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase_extended.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.parallelStream().forEach(ghost -> ghost.state = 0);
        for (Ghost ghost: testGhostGame.ghosts) {

            MovableCell player = (MovableCell) ghost.player;
            player.currentDirection = 40;
            ghost.draw(testGhostGameApp);
            switch (ghost.type) {
                case 13:
                    Whim whim = (Whim) ghost;
                    whim.findTarget();
                    assertEquals(39, whim.currentDirection);
                    break;
                case 12:
                    Ignorant ignorant = (Ignorant) ghost;
                    ignorant.findTarget();
                    // Ignoring the player
                    assertEquals(37, ignorant.currentDirection);
                    break;
                case 11:
                    Chaser chaser = (Chaser) ghost;
                    chaser.findTarget();
                    assertEquals(40, chaser.currentDirection);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    ambusher.findTarget();
                    assertEquals(40, ambusher.currentDirection);
                    break;
                case 9:
                    ghost.findTarget();
                    assertEquals(40, ghost.currentDirection);
                    break;
            }
        }
    }

    @Test
    public void testChaseExtendedLeft() {
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase_extended.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.parallelStream().forEach(ghost -> ghost.state = 0);

        for (Ghost ghost: testGhostGame.ghosts) {
            MovableCell player = (MovableCell) ghost.player;
            player.currentDirection = 37;
            ghost.draw(testGhostGameApp);
            switch (ghost.type) {
                case 13:
                    Whim whim = (Whim) ghost;
                    whim.findTarget();
                    assertEquals(39, whim.currentDirection);
                    break;
                case 12:
                    Ignorant ignorant = (Ignorant) ghost;
                    ignorant.findTarget();
                    // Ignoring the player
                    assertEquals(37, ignorant.currentDirection);
                    break;
                case 11:
                    Chaser chaser = (Chaser) ghost;
                    chaser.findTarget();
                    assertEquals(40, chaser.currentDirection);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    ambusher.findTarget();
                    assertEquals(40, ambusher.currentDirection);
                    break;
                case 9:
                    ghost.findTarget();
                    assertEquals(40, ghost.currentDirection);
                    break;
            }
        }
    }

    @Test
    public void testChaseExtendedRight() {
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase_extended.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.parallelStream().forEach(ghost -> ghost.state = 0);
        testGhostGame.ghosts.parallelStream().forEach(Ghost::findTarget);

        for (Ghost ghost: testGhostGame.ghosts) {
            MovableCell player = (MovableCell) ghost.player;
            player.currentDirection = 39;
            ghost.draw(testGhostGameApp);
            switch (ghost.type) {
                case 13:
                    Whim whim = (Whim) ghost;
                    whim.findTarget();
                    assertEquals(39, whim.currentDirection);
                    break;
                case 12:
                    Ignorant ignorant = (Ignorant) ghost;
                    ignorant.findTarget();
                    // Ignoring the player
                    assertEquals(37, ignorant.currentDirection);
                    break;
                case 11:
                    Chaser chaser = (Chaser) ghost;
                    chaser.findTarget();
                    assertEquals(40, chaser.currentDirection);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    ambusher.findTarget();
                    assertEquals(40, ambusher.currentDirection);
                    break;
                case 9:
                    ghost.findTarget();
                    assertEquals(40, ghost.currentDirection);
                    break;
            }
        }
    }

    @Test
    public void testGhostMoving() {
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_between_cells.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        Ghost ghost = testGhostGame.ghosts.get(0);
        testGhostGame.draw(testGhostGameApp);
        ghost.x = ghost.route.get(ghost.routePointer).x;
        ghost.y = ghost.route.get(ghost.routePointer).y;
        testGhostGame.draw(testGhostGameApp);
        ghost.state = 0;
        ghost.findTarget();
        testGhostGame.draw(testGhostGameApp);
        ghost.x = ghost.route.get(ghost.routePointer).x;
        ghost.y = ghost.route.get(ghost.routePointer).y;
        testGhostGame.draw(testGhostGameApp);
        assertEquals(1, ghost.routePointer);
    }

    @Test
    public void testFrightened() {
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase_extended.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.parallelStream().forEach(Ghost::frighten);
        testGhostGame.draw(testGhostGameApp);
        for (Ghost ghost: testGhostGame.ghosts) {
            assertNotNull(ghost.route);
            assertEquals(2, ghost.state);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            ghost.draw(testGhostGameApp);
            assertEquals(1, ghost.state);
        }

        // Test frighten and invisible (extension)
        testGhostGame.ghosts.parallelStream().forEach(Ghost::frightenAndInvisible);
        testGhostGame.draw(testGhostGameApp);
        for (Ghost ghost: testGhostGame.ghosts) {
            assertNotNull(ghost.route);
            assertEquals(4, ghost.state);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            ghost.draw(testGhostGameApp);
            assertEquals(1, ghost.state);
        }
    }

    @Test
    public void testReset() {
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase_extended.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.parallelStream().forEach(ghost -> ghost.state = 0);
        testGhostGame.ghosts.parallelStream().forEach(Ghost::findTarget);

        for (Ghost ghost: testGhostGame.ghosts) {
            MovableCell player = (MovableCell) ghost.player;
            player.currentDirection = 39;
            List<MapCell> nearByCells = Utility.findNearbyCells(ghost.x, ghost.y, Ghost.map);
            ghost.tick(nearByCells);
            ghost.x = ghost.route.get(ghost.routePointer).x;
            ghost.y = ghost.route.get(ghost.routePointer).y;
            ghost.previousState = 1;
            // Ghost will only reset state if state = REMOVED
            ghost.state = 3;
            ghost.tick(nearByCells);
            assertEquals(1, ghost.routePointer);
            ghost.resetPosition();
            System.out.println();
        }
        // use ghost.routePointer, ghost.initialX and ghost.previousState to test if the ghost is correctly reset
        for (Ghost ghost: testGhostGame.ghosts) {
            assertEquals(0, ghost.routePointer);
            assertEquals(ghost.x, ghost.initialX);
            assertEquals(ghost.previousState, ghost.state);
        }
    }
}
