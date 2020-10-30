package ghost;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import processing.core.PApplet;
import processing.core.PImage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class App extends PApplet {

    public static final int WIDTH = 448;
    public static final int HEIGHT = 576;
    public MapCell[][] mapCells;
    public List<Ghost> ghosts;
    public List<Fruit> fruits;
    private final JSONObject config;
    public Waka player;
    // 0: initialize map, 1: update fruits & player & ghost, 2: update player & ghost, 3: dies
    private int state;

    public void parseMap(String mapFileLocation){

    }

    public App() {
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
    }

    public void setup() {
        frameRate(60);
        size(WIDTH, HEIGHT);
        //load up map
        String mapLocation = (String) this.config.get("map");
        PImage horizontal = this.loadImage("src/main/resources/horizontal.png");
        PImage vertical = this.loadImage("src/main/resources/vertical.png");
        PImage upLeft = this.loadImage("src/main/resources/upLeft.png");
        PImage upRight = this.loadImage("src/main/resources/upRight.png");
        PImage downLeft = this.loadImage("src/main/resources/downLeft.png");
        PImage downRight = this.loadImage("src/main/resources/downRight.png");
        PImage fruit = this.loadImage("src/main/resources/fruit.png");
        PImage[] playerImages = new PImage[5];
        //[left, right, up, down, closed]
        playerImages[0] = this.loadImage("src/main/resources/playerLeft.png");
        playerImages[1] = this.loadImage("src/main/resources/playerRight.png");
        playerImages[2] = this.loadImage("src/main/resources/playerUp.png");
        playerImages[3] = this.loadImage("src/main/resources/playerDown.png");
        playerImages[4] = this.loadImage("src/main/resources/playerClosed.png");
        PImage ghost = this.loadImage("src/main/resources/ghost.png");
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
        MapCell[][] mapList = new MapCell[36][28];
        List<Ghost> ghostList = new ArrayList<>();
        List<Fruit> fruitList = new ArrayList<>();
        Waka player = null;
        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);
            for (int col = 0; col < line.length(); col++) {
                String variable = String.valueOf(line.charAt(col));
                switch (variable) {
                    case "0":
                        mapList[row][col] = new MapCell(null, 0, col, row);
                        break;
                    case "1":
                        mapList[row][col] = new MapCell(horizontal, 1, col, row);
                        break;
                    case "2":
                        mapList[row][col] = new MapCell(vertical, 2, col, row);
                        break;
                    case "3":
                        mapList[row][col] = new MapCell(upLeft, 3, col, row);
                        break;
                    case "4":
                        mapList[row][col] = new MapCell(upRight, 4, col, row);
                        break;
                    case "5":
                        mapList[row][col] = new MapCell(downLeft, 5, col, row);
                        break;
                    case "6":
                        mapList[row][col] = new MapCell(downRight, 6, col, row);
                        break;
                    case "7":
                        Fruit newFruit = new Fruit(fruit, 7, col, row);
                        fruitList.add(newFruit);
                        mapList[row][col] = newFruit;
                        break;
                    case "p":
                        player = new Waka(playerImages, 8, col, row);
                        mapList[row][col] = player;
                        break;
                    case "g":
                        Ghost newGhost = new Ghost(ghost, 9, col, row);
                        ghostList.add(newGhost);
                        mapList[row][col] = newGhost;
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
        if (ghostList.isEmpty()) {
            System.out.println("Error in map: no ghost");
            System.exit(0);
        }
        int speed = ((Long) this.config.get("speed")).intValue();
        if (speed > 2 || speed < 0) {
            System.out.println("Error in config: speed");
            System.exit(0);
        }
        player.setSpeed(speed);
        int life = ((Long) this.config.get("lives")).intValue();
        player.setLife(life);
        this.mapCells = mapList;
        this.player = player;
        this.ghosts = ghostList;
        this.fruits = fruitList;
        // Load map
        for (MapCell[] line : this.mapCells) {
            for (MapCell cell : line) {
                cell.draw(this);
            }
        }
    }

    public void settings() {
        size(WIDTH, HEIGHT);
    }

    public void draw() {
        // player changes state every 8 frame
        if (frameCount%8 == 0) {
            player.closeEye();
        }
        switch (this.state) {
            case 0:
                this.initMap();
                break;
            case 1:
                this.updateFruits();
                break;
            case 2:
                this.updatePlayers();
                break;
        }
    }

    public void initMap() {
        background(0, 0, 0);
        for (MapCell[] line : this.mapCells) {
            for (MapCell cell : line) {
                cell.draw(this);
            }
        }
        //Make sure Ghosts and Players are above Fruits
        this.updatePlayers();
        this.state = 2;
    }

    public void updateFruits() {
        for (Fruit f : this.fruits) {
            this.fill(0);
            this.rect(f.getX(), f.getY(), 16, 16);
            f.draw(this);
        }
        this.updatePlayers();
    }

    public List<MapCell> findNearbyCells(int x, int y) {
        List<MapCell> result = new ArrayList<>();
        for (MapCell[] cellList: this.mapCells) {
            for (MapCell cell: cellList) {
                // find 3x3 squares nearby
                if (Math.abs(cell.getX() - (x + 8)) <= 24 && Math.abs(cell.getY() - (y + 8)) <= 24) {
                    result.add(cell);
                }
            }
        }
        return result;
    }

    public void updatePlayers() {
        for (Ghost ghost : this.ghosts) {
            List<MapCell> nearby = this.findNearbyCells(ghost.getX(), ghost.getY());
            boolean is_killed = ghost.tick(nearby);
            if (is_killed) {
                if (this.player.getLife() == 0) {
                    this.state = 3;
                }
            }
            ghost.draw(this);
        }
        // refresh the cells nearby for player
        this.fill(0);
        this.rect(player.getX() - 6, player.getY() - 6, 27, 27);
        List<MapCell> nearby = this.findNearbyCells(player.getX(), player.getY());
        for (MapCell cell: nearby) {
            cell.draw(this);
        }
        boolean eat = player.tick(nearby);
        this.player.draw(this);
        if (eat) {
            this.state = 1;
        }
    }

    public void keyPressed() {
        player.turn(keyCode);
    }

    public static void main(String[] args) {
         PApplet.main("ghost.App");
//        System.out.println("Hello");
    }
}
