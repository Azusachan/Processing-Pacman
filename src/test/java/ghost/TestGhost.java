package ghost;

import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestGhost {
    @Test
    public void testInitialize()  {
        // Test all variables of each type of ghost is correctly initialized
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
        // test the tick method, called by GameManager.draw() -> GameManager.updatePlayer() can correctly set direction
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.parallelStream().forEach(Ghost::findTarget);
        testGhostGame.draw(testGhostGameApp);
        for (Ghost ghost: testGhostGame.ghosts) {
            switch (ghost.type) {
                case 13:
                    Whim whim = (Whim) ghost;
                    assertEquals(40, whim.currentDirection);
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
                    assertEquals(39, ambusher.currentDirection);
                    assertEquals(1, ambusher.targetCorner);
                    break;
                case 9:
                    assertEquals(0, ghost.targetCorner);
                    assertEquals(38, ghost.currentDirection);
                    break;
            }
            assertNotNull(ghost.target);
            assertNotNull(ghost.route);
            assertNotNull(ghost.player);
        }
    }

    @Test
    public void testChase() {
        // Test the chase mode of Ghost can work with different type of ghost and set ghost.player if player = null
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.parallelStream().forEach(ghost -> ghost.setState(Ghost.CHASE));
        testGhostGame.ghosts.parallelStream().forEach(Ghost::findTarget);
        testGhostGame.draw(testGhostGameApp);
        for (Ghost ghost: testGhostGame.ghosts) {
            switch (ghost.type) {
                case 13:
                    Whim whim = (Whim) ghost;
                    whim.findChaser();
                    assertNotNull(whim.chaser);
                    assertEquals(40, whim.currentDirection);
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
                    assertEquals(39, ambusher.currentDirection);
                    assertEquals(1, ambusher.targetCorner);
                    break;
                case 9:
                    assertEquals(0, ghost.targetCorner);
                    assertEquals(38, ghost.currentDirection);
                    break;
            }
            // Test can ghost find player if chase and player is null
            ghost.player = null;
            ghost.setState(Ghost.CHASE);
            ghost.findTarget();
            assertNotNull(ghost.player);
        }
    }

    @Test
    public void testChaseExtendedUp() {
        // Test chase can work differently when player turns at different directions
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase_extended.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.parallelStream().forEach(ghost -> ghost.setState(Ghost.CHASE));

        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.draw(testGhostGameApp);
        for (Ghost ghost: testGhostGame.ghosts) {
            MovableCell player = (MovableCell) ghost.player;
            player.currentDirection = 38;
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
                    assertEquals(38, chaser.currentDirection);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    ambusher.findTarget();
                    assertEquals(39, ambusher.currentDirection);
                    break;
                case 9:
                    assertEquals(38, ghost.currentDirection);
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
        testGhostGame.ghosts.parallelStream().forEach(ghost -> ghost.setState(Ghost.CHASE));
        testGhostGame.draw(testGhostGameApp);
        for (Ghost ghost: testGhostGame.ghosts) {
            MovableCell player = (MovableCell) ghost.player;
            player.currentDirection = 40;
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
                    assertEquals(38, chaser.currentDirection);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    ambusher.findTarget();
                    assertEquals(39, ambusher.currentDirection);
                    break;
                case 9:
                    ghost.findTarget();
                    assertEquals(38, ghost.currentDirection);
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
        testGhostGame.ghosts.parallelStream().forEach(ghost -> ghost.setState(Ghost.CHASE));
        testGhostGame.draw(testGhostGameApp);
        for (Ghost ghost: testGhostGame.ghosts) {
            MovableCell player = (MovableCell) ghost.player;
            player.currentDirection = 37;
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
                    assertEquals(38, chaser.currentDirection);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    ambusher.findTarget();
                    assertEquals(39, ambusher.currentDirection);
                    break;
                case 9:
                    ghost.findTarget();
                    assertEquals(38, ghost.currentDirection);
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
        testGhostGame.ghosts.parallelStream().forEach(ghost -> ghost.setState(Ghost.CHASE));
        testGhostGame.ghosts.parallelStream().forEach(Ghost::findTarget);
        testGhostGame.draw(testGhostGameApp);
        for (Ghost ghost: testGhostGame.ghosts) {
            MovableCell player = (MovableCell) ghost.player;
            player.currentDirection = 39;
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
                    assertEquals(38, chaser.currentDirection);
                    break;
                case 10:
                    Ambusher ambusher = (Ambusher) ghost;
                    ambusher.findTarget();
                    assertEquals(39, ambusher.currentDirection);
                    break;
                case 9:
                    ghost.findTarget();
                    assertEquals(38, ghost.currentDirection);
                    break;
            }
        }
    }

    @Test
    public void testGhostMoving() {
        // test ghost correctly move towards next cell in route
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_between_cells.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        Ghost ghost = testGhostGame.ghosts.get(0);

        MapCell nextCell = new MapCell(null, 7, 10, 25);
        ghost.route = new ArrayList<>();
        ghost.route.add(nextCell);
        ghost.target = nextCell;

        ghost.x = 160;
        ghost.y = 416;
        List<MapCell> nearByCells = Utility.findNearbyCells(ghost.x, ghost.y, Ghost.map);
        ghost.tick(nearByCells);
        assertEquals(38, ghost.currentDirection);

        ghost.x = 160;
        ghost.y = 384;
        nearByCells = Utility.findNearbyCells(ghost.x, ghost.y, Ghost.map);
        ghost.tick(nearByCells);
        assertEquals(40, ghost.currentDirection);

        ghost.x = 144;
        ghost.y = 400;
        nearByCells = Utility.findNearbyCells(ghost.x, ghost.y, Ghost.map);
        ghost.tick(nearByCells);
        assertEquals(39, ghost.currentDirection);

        ghost.x = 176;
        ghost.y = 400;
        nearByCells = Utility.findNearbyCells(ghost.x, ghost.y, Ghost.map);
        ghost.tick(nearByCells);
        assertEquals(37, ghost.currentDirection);

        // To handle error when ghost is turning and refreshing route list, these edge cases are added
        ghost.x = 144;
        ghost.y = 401;
        nearByCells = Utility.findNearbyCells(ghost.x, ghost.y, Ghost.map);
        ghost.tick(nearByCells);
        assertEquals(39, ghost.currentDirection);

        ghost.x = 176;
        ghost.y = 401;
        nearByCells = Utility.findNearbyCells(ghost.x, ghost.y, Ghost.map);
        ghost.tick(nearByCells);
        assertEquals(37, ghost.currentDirection);

    }

    @Test
    public void testFrightened() {
        // test if the ghost can correctly set to frighten mode and return to previous mode after frightened_duration
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase_extended.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.forEach(Ghost::frighten);
        testGhostGame.draw(testGhostGameApp);
        for (Ghost ghost: testGhostGame.ghosts) {
            assertNotNull(ghost.route);
            assertEquals(2, ghost.state);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        for (Ghost ghost: testGhostGame.ghosts){
            ghost.draw(testGhostGameApp);
            assertEquals(1, ghost.state);
        }

        // Test frighten and invisible (extension)
        testGhostGame.ghosts.forEach(Ghost::frightenAndInvisible);
        testGhostGame.draw(testGhostGameApp);
        for (Ghost ghost: testGhostGame.ghosts) {
            assertNotNull(ghost.route);
            assertEquals(4, ghost.state);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        for (Ghost ghost: testGhostGame.ghosts){
            ghost.draw(testGhostGameApp);
            assertEquals(1, ghost.state);
        }
    }

    @Test
    public void testFrightenedFindNextTarget() {
        // When ghost is frightened and teleported to the target location, it will find a new target and refresh route
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_between_cells.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        // move player away to make sure that it is not accidentally killed
        testGhostGame.player.x = 0;
        testGhostGame.player.y = 0;
        Ghost ghost = testGhostGame.ghosts.get(0);
        ghost.frighten();
        MapCell target = ghost.target;
        while (!ghost.equals(ghost.target)) {
            testGhostGame.draw(testGhostGameApp);
        }
        testGhostGame.draw(testGhostGameApp);
        assertNotEquals(target, ghost.target);
    }

    @Test
    public void testReset() {
        // Test the ghost's location and state is correctly reset
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase_extended.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.parallelStream().forEach(ghost -> ghost.setState(Ghost.CHASE));
        testGhostGame.ghosts.parallelStream().forEach(Ghost::findTarget);

        for (Ghost ghost: testGhostGame.ghosts) {
            MovableCell player = (MovableCell) ghost.player;
            player.currentDirection = 39;
            List<MapCell> nearByCells = Utility.findNearbyCells(ghost.x, ghost.y, Ghost.map);
            ghost.tick(nearByCells);
            ghost.x = ghost.route.get(ghost.routePointer).x;
            ghost.y = ghost.route.get(ghost.routePointer).y;
            ghost.setPreviousState(1);
            // Ghost will only reset state if state = REMOVED
            ghost.setState(Ghost.REMOVED);
            ghost.tick(nearByCells);
            ghost.resetPosition();
            assertNotEquals(Ghost.REMOVED, ghost.state);
        }
        // use ghost.routePointer, ghost.initialX and ghost.previousState to test if the ghost is correctly reset
        for (Ghost ghost: testGhostGame.ghosts) {
            assertEquals(0, ghost.routePointer);
            assertEquals(ghost.x, ghost.initialX);
            assertEquals(ghost.previousState, ghost.state);
        }
    }

    @Test
    public void testKill() {
        // If ghost is not frightened, kill player, if ghost is frightened, ghost.state = REMOVE
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_between_cells.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        Ghost ghost = testGhostGame.ghosts.get(0);
        testGhostGame.draw(testGhostGameApp);

        // kill player
        ghost.x = testGhostGame.player.x;
        ghost.y = testGhostGame.player.y;
        testGhostGame.draw(testGhostGameApp);
        assertEquals(2, testGhostGame.player.getLife());

        // kill ghost
        ghost.frighten();
        ghost.x = testGhostGame.player.x;
        ghost.y = testGhostGame.player.y;
        testGhostGame.draw(testGhostGameApp);
        assertEquals(Ghost.REMOVED, ghost.state);
        ghost.resetPosition();

        // kill player when soda(extension)
        ghost.frightenAndInvisible();
        ghost.x = testGhostGame.player.x;
        ghost.y = testGhostGame.player.y;
        testGhostGame.draw(testGhostGameApp);
        assertEquals(1, testGhostGame.player.getLife());
    }

    @Test
    public void testHandleMovement() {
        // Test ghost handleMovement() method
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_between_cells.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        Ghost ghost = testGhostGame.ghosts.get(0);
        // handleMovement will do nothing if ghost has no route
        ghost.route = new ArrayList<>();
        ghost.handleMovement();
        assertEquals(0, ghost.route.size());

        // handleMovement will reset route if ghost wants to move back
        ghost.resetPosition();
        MapCell nextCell = new MapCell(null, 7, 1, 25);
        ghost.route = new ArrayList<>();
        ghost.currentDirection = 40;
        ghost.route.add(nextCell);
        ghost.target = nextCell;
        ghost.handleMovement();
        assertNotEquals(nextCell, ghost.target);

        ghost.resetPosition();
        nextCell = new MapCell(null, 7, 1, 27);
        ghost.route = new ArrayList<>();
        ghost.currentDirection = 38;
        ghost.route.add(nextCell);
        ghost.target = nextCell;
        ghost.handleMovement();
        assertNotEquals(nextCell, ghost.target);

        ghost.resetPosition();
        nextCell = new MapCell(null, 7, 0, 26);
        ghost.route = new ArrayList<>();
        ghost.currentDirection = 39;
        ghost.route.add(nextCell);
        ghost.target = nextCell;
        ghost.handleMovement();
        assertNotEquals(nextCell, ghost.target);

        ghost.resetPosition();
        nextCell = new MapCell(null, 7, 3, 26);
        ghost.route = new ArrayList<>();
        ghost.currentDirection = 37;
        ghost.route.add(nextCell);
        ghost.target = nextCell;
        ghost.handleMovement();
        assertNotEquals(nextCell, ghost.target);
    }

    @Test
    public void testNullGhost() {
        // when a map has no ghost, a NullGhost will be added to the map, its target will be itself and it does not draw
        GameManager testGhostGame = new GameManager("src/test/resources/test_null_ghost.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.draw(testGhostGameApp);
        Ghost ghost = testGhostGame.ghosts.get(0);
        ghost.findTarget();
        assertEquals(ghost, ghost.target);
        // the route will only have one cell, which is itself
        assertEquals(1, ghost.route.size());
    }

    @Test
    public void testRemoved() {
        // test the ghost's behavior when enters removed state
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_chase.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        testGhostGame.ghosts.parallelStream().forEach(g -> g.setState(Ghost.REMOVED));
        testGhostGame.ghosts.parallelStream().forEach(Ghost::findTarget);
        testGhostGame.draw(testGhostGameApp);
        for (Ghost ghost: testGhostGame.ghosts) {
            assertEquals(ghost, ghost.target);
        }
    }

    @Test
    public void testGetChildren() {
        // When ghost has a moving direction, getChildren should toss away the cell at its opposite direction to ensure
        // it does not turn back
        GameManager testGhostGame = new GameManager("src/test/resources/test_ghost_between_cells.json");
        TestApp testGhostGameApp = new TestApp(testGhostGame);
        PApplet.runSketch(new String[] {"App"}, testGhostGameApp);
        testGhostGameApp.setup();
        testGhostGame.initMap(testGhostGameApp);
        Ghost ghost = testGhostGame.ghosts.get(0);
        ghost.x = 160;
        ghost.y = 400;
        List<MapCell> nearByCells = Utility.findNearbyCells(ghost.x, ghost.y, Ghost.map);
        ghost.currentDirection = 38;
        MapCell ignoredCell = new MapCell(null, 7, 160, 416);
        List<Ghost.MapCellChild> children = ghost.getChildren(
                new Ghost.MapCellChild(ghost, null), ghost, nearByCells);
        for (Ghost.MapCellChild child: children) {
            assertNotEquals(ignoredCell, child.cell);
        }

        ghost.currentDirection = 40;
        ignoredCell = new MapCell(null, 7, 160, 384);
        children = ghost.getChildren(new Ghost.MapCellChild(ghost, null), ghost, nearByCells);
        for (Ghost.MapCellChild child: children) {
            assertNotEquals(ignoredCell, child.cell);
        }

        ghost.currentDirection = 37;
        ignoredCell = new MapCell(null, 7, 176, 400);
        children = ghost.getChildren(new Ghost.MapCellChild(ghost, null), ghost, nearByCells);
        for (Ghost.MapCellChild child: children) {
            assertNotEquals(ignoredCell, child.cell);
        }

        ghost.currentDirection = 39;
        ignoredCell = new MapCell(null, 7, 144, 400);
        children = ghost.getChildren(new Ghost.MapCellChild(ghost, null), ghost, nearByCells);
        for (Ghost.MapCellChild child: children) {
            assertNotEquals(ignoredCell, child.cell);
        }
    }
}
