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
    public Waka player;
    JSONObject config;
    // 0: initialize map, 1: update fruits & player & ghost, 2: update player & ghost
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
        background(0, 0, 0);
        if (this.state == 0) {
            this.initMap();
        } else if (this.state == 1) {
            this.updateFruits();
        }
    }

    public void initMap() {
        for (MapCell[] line : this.mapCells) {
            for (MapCell cell : line) {
                cell.draw(this);
            }
        }
        //Make sure Ghosts are above Fruits
        for (Ghost ghost : this.ghosts) {
            ghost.draw(this);
        }
        //Same for player
        this.player.draw(this);
    }

    public void updateFruits() {
        for (Fruit f : this.fruits) {
            f.draw(this);
        }
        for (Ghost g : this.ghosts) {
            g.draw(this);
        }
        this.player.draw(this);
    }

    public static void main(String[] args) {
         PApplet.main("ghost.App");
//        System.out.println("Hello");
    }
}
