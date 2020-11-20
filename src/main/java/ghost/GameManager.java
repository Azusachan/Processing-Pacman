package ghost;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ghost.Utility.findNearbyCells;

/**
 * Manages the rendering logic and game logic of the game
 */
public class GameManager {
    // Perhaps using enum is better here
    // Cell string
    private static final String EMPTY = "0";
    private static final String HORIZONTAL = "1";
    private static final String VERTICAL = "2";
    private static final String UP_LEFT = "3";
    private static final String UP_RIGHT = "4";
    private static final String DOWN_LEFT = "5";
    private static final String DOWN_RIGHT = "6";
    private static final String FRUIT = "7";
    private static final String SUPER_FRUIT = "8";
    private static final String SODA = "9";
    private static final String WAKA = "p";
    private static final String GHOST = "g";
    private static final String AMBUSHER = "a";
    private static final String CHASER = "c";
    private static final String IGNORANT = "i";
    private static final String WHIM = "w";
    // States
    private static final int INITIALIZE = 0;
    private static final int UPDATE_PLAYER = 1;
    private static final int UPDATE_FRUITS = 2;
    private static final int WIN = 3;
    private static final int LOSE = 4;
    private static final int RESET = 5;
    // Debug states
    private static final int DEBUG_OFF = 0;
    private static final int DEBUG_ON = 1;
    private static final int DEBUG_REALISTIC = 2;

    /**
     * List of list of MapCell representing map of the current game
     */
    public MapCell[][] mapCells;
    /**
     * List of all ghosts in current game
     */
    public List<Ghost> ghosts;
    /**
     * List of all fruits in current game
     */
    public List<Fruit> fruits;
    private final JSONObject config;
    /**
     * Waka/Player of the game
     */
    public Waka player;

    /**
     * The debug state of current game
     * <table>
     *     <tr><td>State</td><td>Integer</td></tr>
     *     <tr><td>DEBUG_OFF</td><td>0</td></tr>
     *     <tr><td>DEBUG_ON</td><td>1</td></tr>
     *     <tr><td>DEBUG_REALISTIC</td><td>2</td></tr>
     * </table>
     */
    public int debug;
    /**
     * The state of current game
     * <table>
     *     <tr><td>INITIALIZE</td><td>0</td></tr>
     *     <tr><td>UPDATE_PLAYER</td><td>1</td></tr>
     *     <tr><td>UPDATE_FRUITS</td><td>2</td></tr>
     *     <tr><td>WIN</td><td>3</td></tr>
     *     <tr><td>LOSE</td><td>4</td></tr>
     *     <tr><td>RESET</td><td>5</td></tr>
     * </table>
     */
    public int state;
    /**
     * The time the game starts in milliseconds
     */
    public int startTime;
    /**
     * The number of time ghost switches mode between chase and scatter, will return to 0 if all mode lengths has been gone through
     */
    public int modePointer;
    /**
     * The length of ghost's chase or scatter mode
     */
    public List<Integer> modeLengths;

    /**
     * Initializes new GameManager
     * @param configLocation location to the json config of the game
     */
    public GameManager(String configLocation) {
        JSONParser parser = new JSONParser();
        JSONObject configObject = null;
        try {
            File config = new File(configLocation);
            FileReader reader = new FileReader(config);
            configObject = (JSONObject) parser.parse(reader);
        } catch (IOException e) {
            System.out.println("No configuration");
            System.exit(1);
        } catch (ParseException e) {
            System.out.println("Error in configuration");
            System.exit(1);
        }
        if (configObject == null) {
            System.out.println("Error in configuration");
            System.exit(1);
        }
        this.config = configObject;
        this.debug = DEBUG_OFF;
    }

    /**
     * Setup PImage and map of the game
     * @param app PApplet to create images
     */
    public void setup(PApplet app) {
        //start timer
        this.startTime = app.millis();
        this.modePointer = 0;
        //load up map
        String mapLocation = (String) this.config.get("map");
        if (mapLocation == null) {
            System.out.println("Error in config: map");
            System.exit(1);
        }
        PImage horizontal = app.loadImage("src/main/resources/horizontal.png");
        PImage vertical = app.loadImage("src/main/resources/vertical.png");
        PImage upLeft = app.loadImage("src/main/resources/upLeft.png");
        PImage upRight = app.loadImage("src/main/resources/upRight.png");
        PImage downLeft = app.loadImage("src/main/resources/downLeft.png");
        PImage downRight = app.loadImage("src/main/resources/downRight.png");
        PImage fruit = app.loadImage("src/main/resources/fruit.png");
        PImage superFruit = app.loadImage("src/main/resources/superFruit.png");
        PImage soda = app.loadImage("src/main/resources/soda.png");

        PImage[] playerImages = new PImage[5];
        //[left, right, up, down, closed]
        playerImages[0] = app.loadImage("src/main/resources/playerLeft.png");
        playerImages[1] = app.loadImage("src/main/resources/playerRight.png");
        playerImages[2] = app.loadImage("src/main/resources/playerUp.png");
        playerImages[3] = app.loadImage("src/main/resources/playerDown.png");
        playerImages[4] = app.loadImage("src/main/resources/playerClosed.png");
        PImage ghostFrightened = app.loadImage("src/main/resources/frightened.png");
        PImage[] ghost = new PImage[]{app.loadImage("src/main/resources/ghost.png"), ghostFrightened};
        PImage[] ambusher = new PImage[]{app.loadImage("src/main/resources/ambusher.png"), ghostFrightened};
        PImage[] chaser = new PImage[]{app.loadImage("src/main/resources/chaser.png"), ghostFrightened};
        PImage[] ignorant = new PImage[]{app.loadImage("src/main/resources/ignorant.png"), ghostFrightened};
        PImage[] whim = new PImage[]{app.loadImage("src/main/resources/whim.png"), ghostFrightened};
        PFont font = app.createFont("src/main/resources/PressStart2P-Regular.ttf", 16, false);
        app.textFont(font);
        List<String> lines = new ArrayList<>();
        try {
            File mapFile = new File(mapLocation);
            FileReader reader = new FileReader(mapFile);
            BufferedReader mapBufferedReader = new BufferedReader(reader);

            String line;
            while ((line = mapBufferedReader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Error in map: IOException");
            System.exit(1);
        }
        if (lines.size() != 36 || lines.get(0).length() != 28) {
            System.out.println("Error in map: size");
            System.exit(1);
        }
        int speed = ((Long) this.config.get("speed")).intValue();
        if (speed > 2 || speed <= 0) {
            System.out.println("Error in config: speed");
            System.exit(1);
        }
        List<Integer> modeLengths = new ArrayList<>();
        JSONArray modeLengthsJSON =(JSONArray) this.config.get("modeLengths");
        if (modeLengthsJSON != null) {
            for (Object o : modeLengthsJSON) {
                modeLengths.add(Math.toIntExact((Long) o));
            }
        } else {
            System.out.println("Error in config: modeLengths");
            System.exit(1);
        }
        int frightened = ((Long) this.config.get("frightenedLength")).intValue();
        this.modeLengths = modeLengths;
        MapCell[][] mapList = new MapCell[36][28];
        List<Ghost> ghostList = new ArrayList<>();
        List<Fruit> fruitList = new ArrayList<>();
        Waka player = null;
        Ghost.setFrightenedDuration(frightened);
        Ghost.setMap(mapList);
        SuperFruit.setGhost(ghostList);
        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);
            for (int col = 0; col < line.length(); col++) {
                String variable = String.valueOf(line.charAt(col));
                switch (variable) {
                    case EMPTY:
                        mapList[row][col] = new MapCell(null, 0, col, row);
                        break;
                    case HORIZONTAL:
                        mapList[row][col] = new MapCell(horizontal, 1, col, row);
                        break;
                    case VERTICAL:
                        mapList[row][col] = new MapCell(vertical, 2, col, row);
                        break;
                    case UP_LEFT:
                        mapList[row][col] = new MapCell(upLeft, 3, col, row);
                        break;
                    case UP_RIGHT:
                        mapList[row][col] = new MapCell(upRight, 4, col, row);
                        break;
                    case DOWN_LEFT:
                        mapList[row][col] = new MapCell(downLeft, 5, col, row);
                        break;
                    case DOWN_RIGHT:
                        mapList[row][col] = new MapCell(downRight, 6, col, row);
                        break;
                    case FRUIT:
                        Fruit newFruit = new Fruit(fruit, 7, col, row);
                        fruitList.add(newFruit);
                        mapList[row][col] = newFruit;
                        break;
                    case SUPER_FRUIT:
                        SuperFruit newSuperFruit = new SuperFruit(superFruit, 14, col, row);
                        fruitList.add(newSuperFruit);
                        mapList[row][col] = newSuperFruit;
                        break;
                    case SODA:
                        Soda newSoda = new Soda(soda, 15, col, row);
                        fruitList.add(newSoda);
                        mapList[row][col] = newSoda;
                        break;
                    case WAKA:
                        player = new Waka(playerImages, 8, col, row);
                        player.setSpeed(speed);
                        mapList[row][col] = player;
                        break;
                    case GHOST:
                        Ghost newGhost = new Ghost(ghost, 9, col, row);
                        newGhost.setSpeed(speed);
                        newGhost.setState(Ghost.SCATTER);
                        ghostList.add(newGhost);
                        mapList[row][col] = newGhost;
                        break;
                    case AMBUSHER:
                        Ghost newAmbusher = new Ambusher(ambusher, 10, col, row);
                        newAmbusher.setSpeed(speed);
                        newAmbusher.setState(Ghost.SCATTER);
                        ghostList.add(newAmbusher);
                        mapList[row][col] = newAmbusher;
                        break;
                    case CHASER:
                        Ghost newChaser = new Chaser(chaser, 11, col, row);
                        newChaser.setSpeed(speed);
                        newChaser.setState(Ghost.SCATTER);
                        ghostList.add(newChaser);
                        mapList[row][col] = newChaser;
                        break;
                    case IGNORANT:
                        Ghost newIgnorant = new Ignorant(ignorant, 12, col, row);
                        newIgnorant.setSpeed(speed);
                        newIgnorant.setState(Ghost.SCATTER);
                        ghostList.add(newIgnorant);
                        mapList[row][col] = newIgnorant;
                        break;
                    case WHIM:
                        Ghost newWhim = new Whim(whim, 13, col, row);
                        newWhim.setSpeed(speed);
                        newWhim.setState(Ghost.SCATTER);
                        ghostList.add(newWhim);
                        mapList[row][col] = newWhim;
                        break;
                    default:
                        System.out.println("Error in map: unknown configuration");
                        System.exit(1);
                }
            }
        }
        if (player == null) {
            System.out.println("Error in map: no player");
            System.exit(1);
        }
        if (fruitList.isEmpty()) {
            System.out.println("Error in map: no fruit");
            System.exit(1);
        }
        // create a nullGhost which effectively does nothing when the map does not contain anything
        if (ghostList.isEmpty()) {
            Ghost newGhost = new NullGhost();
            newGhost.setSpeed(speed);
            newGhost.setState(Ghost.SCATTER);
            ghostList.add(newGhost);
        }
        player.setSpeed(speed);
        int life = ((Long) this.config.get("lives")).intValue();
        if (life <= 0) {
            System.out.println("Error in config: lives");
            System.exit(1);
        }
        player.setLife(life);
        this.mapCells = mapList;
        this.player = player;
        this.ghosts = ghostList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        this.fruits = fruitList.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Render the game according to its state
     * @param app PApplet the manager renders on
     * @see #initMap
     * @see #updatePlayers
     * @see #win
     * @see #lose
     * @see #reset
     */
    public void draw(PApplet app) {
        // player changes state every 8 frame
        if (app.frameCount % 8 == 0) {
            player.closeEye();
        }
        switch (this.state) {
            case INITIALIZE:
                this.initMap(app);
                break;
            case UPDATE_PLAYER:
                this.updatePlayers(app);
                break;
            case UPDATE_FRUITS:
                this.updateFruits(app);
                break;
            case WIN:
                this.win(app);
                break;
            case LOSE:
                this.lose(app);
                break;
            case RESET:
                this.reset(app);
                break;
        }
    }

    /**
     * Draw all cell on the map, then {@code updatePlayers}
     * @param app PApplet to render on
     * @see #updatePlayers
     */
    public void initMap(PApplet app) {
        rect(app, 0, 0, 448, 576);
        for (MapCell[] line : this.mapCells) {
            for (MapCell cell : line) {
                cell.draw(app);
            }
        }
        //Make sure Ghosts and Players are above Fruits
        this.updatePlayers(app);
        this.state = UPDATE_PLAYER;
    }

    /**
     * Draw all fruits on the map, check if all fruits is eaten to determine if the game is won, then {@code updatePlayers}
     * @param app PApplet to render on
     * @see #updatePlayers
     */
    public void updateFruits(PApplet app) {
        for (Fruit f : this.fruits) {
            rect(app, f.getX(), f.getY(), 16, 16);
            f.draw(app);
        }
        this.state = UPDATE_PLAYER;
        boolean isGameFinished = this.fruits.parallelStream().allMatch(Fruit::isEaten);
        if (isGameFinished) {
            this.state = WIN;
        }
        this.updatePlayers(app);
    }

    /**
     * Render win message, then change state to reset
     * @param app PApplet to render on
     * @see #reset
     */
    public void win(PApplet app) {
        rect(app, 0, 0, 448, 576);
        app.fill(255);
        app.textAlign(app.CENTER);
        app.text("YOU WIN", 224, 288);
        app.noFill();
        this.state = RESET;
    }

    /**
     * Render game over message, then change state to reset
     * @param app PApplet to render on
     * @see #reset
     */
    public void lose(PApplet app) {
        rect(app, 0, 0, 448, 576);
        app.fill(255);
        app.textAlign(app.CENTER);
        app.text("GAME OVER", 224, 288);
        app.noFill();
        this.state = RESET;
    }

    /**
     * Wait for 10 seconds, then reset the map to initial state
     * @param app PApplet to render on
     */
    public void reset(PApplet app) {
        app.delay(10000);
        this.startTime = app.millis();
        this.modePointer = 0;
        this.ghosts.parallelStream().forEach(Ghost::resetPosition);
        this.ghosts.parallelStream().forEach(g -> g.setState(Ghost.SCATTER));
        this.fruits.parallelStream().forEach(Fruit::restore);
        this.player.resetPosition();
        this.state = INITIALIZE;
    }

    /**
     * Returns if it is time to change state for ghosts. If it is, increment {@code modePointer}
     * @param app PApplet to get current time
     * @return if it is time to change state for ghosts
     */
    public boolean updateTimer(PApplet app){
        int timer = app.millis() - this.startTime;
        timer = timer / 1000;
        int modeLength = this.modeLengths.get(this.modePointer);
        if (timer >= modeLength) {
            if (this.modePointer != this.modeLengths.size() - 1) {
                this.modePointer++;
            } else {
                this.modePointer = 0;
            }
            this.startTime = app.millis();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Move player and ghost, render cell near them and render player and ghosts.
     *
     * <p>If debug mode is on, it will refresh all cells at start, then call {@code handleDebug} to render debug mode lines</p>
     * @param app PApplet to render on
     * @see #handleDebug
     */
    public void updatePlayers(PApplet app) {
        if (this.debug != DEBUG_OFF) {
            rect(app, 0, 0, 448, 576);
            for (MapCell[] line : this.mapCells) {
                for (MapCell cell : line) {
                    cell.draw(app);
                }
            }
        }
        boolean changeGhostState = this.updateTimer(app);
        boolean killedByGhost = false;
        for (Ghost ghost : this.ghosts) {
            if (changeGhostState){
                if (ghost.state < 2) {
                    if (this.modePointer % 2 == 0) {
                        ghost.setState(Ghost.SCATTER);
                    } else {
                        ghost.setState(Ghost.CHASE);
                    }
                    ghost.findTarget();
                } else {
                    if (this.modePointer % 2 == 0) {
                        ghost.setPreviousState(1);
                    } else {
                        ghost.setPreviousState(0);
                    }
                }
            }
            //update player every 16 frames
            if (ghost.state == Ghost.CHASE && app.frameCount%16 == 0) {
                ghost.findTarget();
            }
            List<MapCell> nearby = findNearbyCells(ghost.getX(), ghost.getY(), this.mapCells);
            // clear the cells nearby for ghost
            if (ghost.state != 3) {
                for (MapCell cell : nearby) {
                    if (cell.getType() == 7) {
                        rect(app, cell.getX(), cell.getY(), 16, 16);
                        cell.draw(app);
                    }
                }
                // clear ghost
                rect(app, ghost.getX() - 6, ghost.getY() - 6, 28, 28);
                // Sometimes ghost.tick will raise NullPointerExceptions, this only will happen when multiple test cases
                // run at once.
                boolean killed = false;
                try {
                    killed = ghost.tick(nearby);
                } catch (NullPointerException ignored) {}
                if (killed) {
                    killedByGhost = true;
                }
            }
        }
        // refresh the cells nearby for player
        rect(app, player.getX() - 6, player.getY() - 6, 27, 27);
        List<MapCell> nearby = findNearbyCells(player.getX(), player.getY(), this.mapCells);
        for (MapCell cell: nearby) {
            cell.draw(app);
        }
        if (killedByGhost) {
            for (Ghost ghost : this.ghosts) {
                rect(app, ghost.getX() - 6, ghost.getY() - 6, 28, 28);
            }
            this.player.kill();
            this.ghosts.parallelStream().forEach(Ghost::resetPosition);
            this.state = UPDATE_FRUITS;
        }
        if (this.player.getLife() == 0) {
            this.state = LOSE;
        }
        boolean eat = player.tick(nearby);
        // Draw ghost first, then player to ensure player is above ghost as the video shows
        for (Ghost ghost : this.ghosts) {
            this.handleDebug(app, ghost, killedByGhost);
            ghost.draw(app);
        }
        this.player.draw(app);
        if (eat) {
            this.state = UPDATE_FRUITS;
        }
    }

    /**
     * Render the debug mode lines according to debug mode
     * <p>If debug mode is off, or the ghosts are resetting after killed the player, nothing will be rendered</p>
     * <p>If debug mode is on, it will render lines to simulate behavior of debug mode in the video</p>
     * <p>If debug mode is realistic, it will render lines to the center of the cell ghost target at and the square
     * the game is trying to refresh. </p>
     * @param app PApplet to render on
     * @param ghost List of ghosts in the game
     * @param killedByGhost If any ghost killed the player
     */
    public void handleDebug(PApplet app, Ghost ghost, boolean killedByGhost) {
        if (this.debug != DEBUG_OFF && !killedByGhost) {
            app.stroke(255, 255, 0);
            if (this.debug == DEBUG_ON) {
                if (ghost.state == 1) {
                    switch (ghost.targetCorner) {
                        case 0:
                            app.line(ghost.getX() + 8, ghost.getY() + 8, 0, 0);
                            break;
                        case 1:
                            app.line(ghost.getX() + 8, ghost.getY() + 8, 448, 0);
                            break;
                        case 2:
                            app.line(ghost.getX() + 8, ghost.getY() + 8, 0, 576);
                            break;
                        case 3:
                            app.line(ghost.getX() + 8, ghost.getY() + 8, 448, 576);
                            break;
                    }
                } else {
                        switch (ghost.getType()) {
                            case 16: // NullGhost
                                break;
                            case 9: // Ghost
                            case 11: // Chaser
                                app.line(ghost.getX() + 8, ghost.getY() + 8,
                                        ghost.target.getX() + 8, ghost.target.getY() + 8);
                                break;
                            case 10: // Ambusher
                                Ambusher ambusher = (Ambusher) ghost;
                                app.line(ambusher.getX() + 8, ambusher.getY() + 8,
                                        ambusher.vectorX, ambusher.vectorY);
                                break;
                            case 12: // Ignorant
                                Ignorant ignorant = (Ignorant) ghost;
                                app.line(ignorant.getX() + 8, ignorant.getY() + 8,
                                        ignorant.vectorX, ignorant.vectorY);
                                break;
                            case 13: // Whim
                                Whim whim = (Whim) ghost;
                                app.line(whim.getX() + 8, whim.getY() + 8,
                                        whim.vectorX, whim.vectorY);
                                break;
                        }
                }
                app.noStroke();
            } else if (this.debug == DEBUG_REALISTIC){
                app.line(ghost.getX() + 8, ghost.getY() + 8,
                        ghost.target.getX() + 8, ghost.target.getY() + 8);
            }
        }
    }

    /**
     * Change debug mode or move player according to input
     * <p>If input is arrow button, player will be moved. </p>
     * <p>If input is space, debug mode will turn on if it is off, turn off if it is on or realistic. </p>
     * <p>If input is "r", debug mode will change to realistic if it is on or off, on if it is realistic. </p>
     * @param keyCode key code of current input
     */
    public void keyPressed(int keyCode) {
        if (keyCode == 32) { // press space
            switch (this.debug) {
                case DEBUG_OFF:
                    this.debug = DEBUG_ON;
                    break;
                case DEBUG_ON:
                case DEBUG_REALISTIC:
                    this.debug = DEBUG_OFF;
                    this.state = INITIALIZE;
                    break;
            }
        } else if (keyCode == 82) { // press r
            switch (this.debug) {
                case DEBUG_OFF:
                case DEBUG_ON:
                    this.debug = DEBUG_REALISTIC;
                    break;
                case DEBUG_REALISTIC:
                    this.debug = DEBUG_ON;
                    break;
            }
        } else {
            player.turn(keyCode);
        }
    }

    /**
     * Simplifies the refresh square process and handle issue in description below.
     * <p>For unknown reason, any call of app.rect() (even if the input are all valid integers) has little chance to raise
     * NullPointerException. This would only happen if test cases are ran repetitively. </p>
     * @param app PApplet to render on
     * @param a x-coordinate of the rectangle by default
     * @param b y-coordinate of the rectangle by default
     * @param c width of the rectangle by default
     * @param d height of the rectangle by default
     */
    public static void rect(PApplet app, float a, float b, float c, float d) {
        try {
            app.fill(0);
            app.rect(a, b, c, d);
            app.noFill();
        } catch (NullPointerException ignored) { }
    }
}
