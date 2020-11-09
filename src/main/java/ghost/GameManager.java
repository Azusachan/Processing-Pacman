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

import static ghost.Utility.findNearbyCells;

public class GameManager {
    private static final String EMPTY = "0";
    private static final String HORIZONTAL = "1";
    private static final String VERTICAL = "2";
    private static final String UP_LEFT = "3";
    private static final String UP_RIGHT = "4";
    private static final String DOWN_LEFT = "5";
    private static final String DOWN_RIGHT = "6";
    private static final String FRUIT = "7";
    public static final String SUPER_FRUIT = "8";
    private static final String WAKA = "p";
    private static final String GHOST = "g";
    private static final String AMBUSHER = "a";
    private static final String CHASER = "c";
    private static final String IGNORANT = "i";
    private static final String WHIM = "w";
    private static final int INITIALIZE = 0;
    private static final int UPDATE_PLAYER = 1;
    private static final int UPDATE_FRUITS = 2;
    private static final int WIN = 3;
    private static final int LOSE = 4;
    private static final int RESET = 5;

    public MapCell[][] mapCells;
    public List<Ghost> ghosts;
    public List<Fruit> fruits;
    private final JSONObject config;
    public Waka player;

    private boolean debug;
    private int state;
    private int startTime;
    public int modePointer;
    public List<Integer> modeLengths;

    public GameManager() {
        //Set up your objects
        JSONParser parser = new JSONParser();
        JSONObject configObject = null;
        try {
            File config = new File("config.json");
            FileReader reader = new FileReader(config);
            configObject = (JSONObject) parser.parse(reader);
        } catch (IOException e) {
            System.out.println("No configuration");
            System.exit(0);
        } catch (ParseException e) {
            System.out.println("Error in configuration");
            System.exit(0);
        }
        if (configObject == null) {
            System.out.println("Error in configuration");
            System.exit(0);
        }
        this.config = configObject;
        this.debug = false;
    }

    public void setup(PApplet app) {
        //start timer
        this.startTime = app.millis();
        this.modePointer = 0;
        //load up map
        String mapLocation = (String) this.config.get("map");
        if (mapLocation == null) {
            System.out.println("Error in config: map");
            System.exit(0);
        }
        PImage horizontal = app.loadImage("src/main/resources/horizontal.png");
        PImage vertical = app.loadImage("src/main/resources/vertical.png");
        PImage upLeft = app.loadImage("src/main/resources/upLeft.png");
        PImage upRight = app.loadImage("src/main/resources/upRight.png");
        PImage downLeft = app.loadImage("src/main/resources/downLeft.png");
        PImage downRight = app.loadImage("src/main/resources/downRight.png");
        PImage fruit = app.loadImage("src/main/resources/fruit.png");
        PImage superFruit = app.loadImage("src/main/resources/superFruit.png");

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
            System.exit(0);
        }
        if (lines.size() != 36 || lines.get(0).length() != 28) {
            System.out.println("Error in map: size");
            System.exit(0);
        }
        int speed = ((Long) this.config.get("speed")).intValue();
        if (speed > 2 || speed <= 0) {
            System.out.println("Error in config: speed");
            System.exit(0);
        }
        List<Integer> modeLengths = new ArrayList<>();
        JSONArray modeLengthsJSON =(JSONArray) this.config.get("modeLengths");
        if (modeLengthsJSON != null) {
            for (Object o : modeLengthsJSON) {
                modeLengths.add(Math.toIntExact((Long) o));
            }
        } else {
            System.out.println("Error in config: modeLengths");
            System.exit(0);
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
                    case WAKA:
                        player = new Waka(playerImages, 8, col, row);
                        player.setSpeed(speed);
                        mapList[row][col] = player;
                        break;
                    case GHOST:
                        Ghost newGhost = new Ghost(ghost, 9, col, row);
                        newGhost.setSpeed(speed);
                        newGhost.setState(1);
                        ghostList.add(newGhost);
                        mapList[row][col] = newGhost;
                        break;
                    case AMBUSHER:
                        Ghost newAmbusher = new Ambusher(ambusher, 10, col, row);
                        newAmbusher.setSpeed(speed);
                        newAmbusher.setState(1);
                        ghostList.add(newAmbusher);
                        mapList[row][col] = newAmbusher;
                        break;
                    case CHASER:
                        Ghost newChaser = new Chaser(chaser, 11, col, row);
                        newChaser.setSpeed(speed);
                        newChaser.setState(1);
                        ghostList.add(newChaser);
                        mapList[row][col] = newChaser;
                        break;
                    case IGNORANT:
                        Ghost newIgnorant = new Ignorant(ignorant, 12, col, row);
                        newIgnorant.setSpeed(speed);
                        newIgnorant.setState(1);
                        ghostList.add(newIgnorant);
                        mapList[row][col] = newIgnorant;
                        break;
                    case WHIM:
                        Ghost newWhim = new Whim(whim, 13, col, row);
                        newWhim.setSpeed(speed);
                        newWhim.setState(1);
                        ghostList.add(newWhim);
                        mapList[row][col] = newWhim;
                        break;
                    default:
                        System.out.println("Error in map: unknown configuration");
                        System.exit(0);
                }
            }
        }
        if (player == null) {
            System.out.println("Error in map: no player");
            System.exit(0);
        }
        // create a nullGhost which effectively does nothing when the map does not contain anything
        if (ghostList.isEmpty()) {
            Ghost newGhost = new nullGhost(ghost);
            newGhost.setSpeed(speed);
            newGhost.setState(1);
            ghostList.add(newGhost);
        }
        player.setSpeed(speed);
        int life = ((Long) this.config.get("lives")).intValue();
        if (life <= 0) {
            System.out.println("Error in config: lives");
            System.exit(0);
        }
        player.setLife(life);
        this.mapCells = mapList;
        this.player = player;
        this.ghosts = ghostList;
        this.fruits = fruitList;
    }

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

    public void initMap(PApplet app) {
        app.fill(0);
        app.rect(0, 0, 448, 576);
        for (MapCell[] line : this.mapCells) {
            for (MapCell cell : line) {
                cell.draw(app);
            }
        }
        //Make sure Ghosts and Players are above Fruits
        this.updatePlayers(app);
        this.state = UPDATE_PLAYER;
    }

    public void updateFruits(PApplet app) {
        for (Fruit f : this.fruits) {
            app.fill(0);
            app.rect(f.getX(), f.getY(), 16, 16);
            f.draw(app);
        }
        this.state = UPDATE_PLAYER;
        boolean isGameFinished = this.fruits.parallelStream().allMatch(Fruit::isEaten);
        if (isGameFinished) {
            this.state = WIN;
        }
        this.updatePlayers(app);
    }

    public void win(PApplet app) {
        app.fill(0);
        app.rect(0, 0, 448, 576);
        app.fill(255);
        app.textAlign(app.CENTER);
        app.text("YOU WIN", 224, 288);
        this.state = RESET;
    }

    public void lose(PApplet app) {
        app.fill(0);
        app.rect(0, 0, 448, 576);
        app.fill(255);
        app.textAlign(app.CENTER);
        app.text("GAME OVER", 224, 288);
        this.state = RESET;
    }

    public void reset(PApplet app) {
        app.delay(10000);
        this.startTime = app.millis();
        this.modePointer = 0;
        this.ghosts.parallelStream().forEach(Ghost::resetPosition);
        this.ghosts.parallelStream().forEach(g -> g.state = 1);
        this.fruits.parallelStream().forEach(Fruit::restore);
        this.player.resetPosition();
        this.state = INITIALIZE;
    }

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

    public void updatePlayers(PApplet app) {
        if (this.debug) {
            app.fill(0);
            app.rect(0, 0, 448, 576);
            for (MapCell[] line : this.mapCells) {
                for (MapCell cell : line) {
                    cell.draw(app);
                }
            }
        }
        boolean changeGhostState = this.updateTimer(app);
        boolean killedByGhost = false;
        for (Ghost ghost : this.ghosts) {
            if (changeGhostState && ghost.state != 2 && ghost.state != 3){
                if (this.modePointer % 2 == 0) {
                    ghost.setState(1);
                } else {
                    ghost.setState(0);
                }
                ghost.findTarget();
            }
            //update player every 16 frames
            if (ghost.state == 0 && app.frameCount%16 == 0) {
                ghost.findTarget();
            }
            List<MapCell> nearby = findNearbyCells(ghost.getX(), ghost.getY(), this.mapCells);
            // clear the cells nearby for ghost
            for (MapCell cell : nearby) {
                if (cell.getType() == 7) {
                    app.fill(0);
                    app.rect(cell.getX(), cell.getY(), 16, 16);
                    cell.draw(app);
                }
            }
            // clear ghost
            app.fill(0);
            app.rect(ghost.getX() - 6, ghost.getY() - 6, 28, 28);
            boolean killed = ghost.tick(nearby);
            if (killed) {
                killedByGhost = true;
            }
        }
        // refresh the cells nearby for player
        app.fill(0);
        app.rect(player.getX() - 6, player.getY() - 6, 27, 27);
        List<MapCell> nearby = findNearbyCells(player.getX(), player.getY(), this.mapCells);
        for (MapCell cell: nearby) {
            cell.draw(app);
        }
        if (killedByGhost) {
            for (Ghost ghost : this.ghosts) {
                app.fill(0);
                app.rect(ghost.getX() - 6, ghost.getY() - 6, 28, 28);
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
            this.handleDemo(app, ghost, killedByGhost);
            ghost.draw(app);
        }
        this.player.draw(app);
        if (eat) {
            this.state = UPDATE_FRUITS;
        }
    }

    public void handleDemo(PApplet app, Ghost ghost, boolean killedByGhost) {
        if (this.debug && !killedByGhost) {
            app.stroke(255, 255, 0);
//                if (ghost.state == 1) {
//                    switch (ghost.targetCorner) {
//                        case 0:
//                            app.line(ghost.getX() + 8, ghost.getY() + 8, 0, 0);
//                            break;
//                        case 1:
//                            app.line(ghost.getX() + 8, ghost.getY() + 8, 448, 0);
//                            break;
//                        case 2:
//                            app.line(ghost.getX() + 8, ghost.getY() + 8, 0, 576);
//                            break;
//                        case 3:
//                            app.line(ghost.getX() + 8, ghost.getY() + 8, 448, 576);
//                            break;
//                    }
//                } else {
//                    app.line(ghost.getX() + 8, ghost.getY() + 8,
//                            ghost.target.getX() + 8, ghost.target.getY() + 8);
//                }
            app.line(ghost.getX() + 8, ghost.getY() + 8,
                    ghost.target.getX() + 8, ghost.target.getY() + 8);
            if (ghost.getType() == 13) {
                Whim whim = (Whim) ghost;
                app.line(whim.getX() + 8, whim.getY() + 8, whim.vectorX, whim.vectorY);
            }
        } else {
            if (app.g.stroke) {
                app.noStroke();
                app.fill(0);
                app.rect(0, 0, 448, 576);
                for (MapCell[] line : this.mapCells) {
                    for (MapCell cell : line) {
                        cell.draw(app);
                    }
                }
            }
        }
    }

    public void keyPressed(int keyCode) {
        if (keyCode == 32) {
            this.debug = !this.debug;
        } else {
            player.turn(keyCode);
        }
    }
}
