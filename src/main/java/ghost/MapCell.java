package ghost;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Basic cell for the map
 */
public class MapCell {
    /**
     * Image of the cell
     */
    public final PImage cellImage;
    /**
     * Type of the cell
     * @see #MapCell
     */
    public int type;
    /**
     * X axis of the cell
     */
    public final int x;
    /**
     * Y axis of the cell
     */
    public final int y;

    /**
     * Create a new instance of {@code MapCell}
     *
     * <p>
     *     List of different type of cells and their character
     *     <table border="1">
     *         <tr>
     *             <td>Cell Name</td> <td>Cell String</td> <td>Cell Character</td>
     *         </tr>
     *         <tr>
     *             <td>Empty</td> <td>0</td> <td>0</td>
     *         </tr>
     *         <tr>
     *             <td>Horizontal</td> <td>1</td> <td>1</td>
     *         </tr>
     *         <tr>
     *             <td>Vertical</td> <td>2</td> <td>2</td>
     *         </tr>
     *         <tr>
     *             <td>Up Left</td> <td>3</td> <td>3</td>
     *         </tr>
     *         <tr>
     *             <td>Up Right</td> <td>4</td> <td>4</td>
     *         </tr>
     *         <tr>
     *             <td>Down Left</td> <td>5</td> <td>5</td>
     *         </tr>
     *         <tr>
     *             <td>Down Right</td> <td>6</td> <td>6</td>
     *         </tr>
     *         <tr>
     *             <td>Fruit</td> <td>7</td> <td>7</td>
     *         </tr>
     *         <tr>
     *             <td>Waka</td> <td>p</td> <td>8</td>
     *         </tr>
     *         <tr>
     *             <td>Ghost</td> <td>g</td> <td>9</td>
     *         </tr>
     *         <tr>
     *             <td>NullGhost</td> <td>does not exist</td> <td>16</td>
     *         </tr>
     *         <tr>
     *             <td>Ambusher</td> <td>a</td> <td>10</td>
     *         </tr>
     *         <tr>
     *             <td>Chaser</td> <td>c</td> <td>11</td>
     *         </tr>
     *         <tr>
     *             <td>Ignorant</td> <td>i</td> <td>12</td>
     *         </tr>
     *         <tr>
     *             <td>Whim</td> <td>w</td> <td>13</td>
     *         </tr>
     *         <tr>
     *             <td>Super Fruit</td> <td>8</td> <td>14</td>
     *         </tr>
     *         <tr>
     *             <td>Soda</td> <td>9</td> <td>15</td>
     *         </tr>
     *         </table>
     * </p>
     * @param image PImage of this cell
     * @param character Integer describing the type of cell, see above for detail
     * @param x row of the cell in map
     * @param y column of the cell in map
     */
    MapCell(PImage image, int character, int x, int y) {
        this.cellImage = image;
        this.type = character;
        this.x = x * 16;
        this.y = y * 16;
    }

    /**
     * Returns x axis of the cell
     * @return x axis of the cell
     */
    public int getX() {
        return this.x;
    }

    /**
     * Returns y axis of the cell
     * @return y axis of the cell
     */
    public int getY() {
        return this.y;
    }

    /**
     * Returns if this cell is movable
     * @return if this cell is movable
     */
    public boolean movable() {
        return false;
    }

    /**
     * Returns if the cell can be pass through
     * @return if the cell can be pass through
     */
    public boolean cannotPassThrough() {
        return this.type != 0;
    }

    public int getType() {
        return this.type;
    }

    /**
     * Renders the cell image according to its axis on PApplet
     * @param app PApplet to render image on
     */
    public void draw(PApplet app) {
        if (this.cellImage == null) {
            return;
        }
        app.image(this.cellImage, this.x, this.y);
    }

    /**
     * Returns if the MapCell has the same location as given MapCell
     * @param o object to be compare with
     * @return if the MapCell has the same location as given MapCell
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapCell)) {
            return false;
        }
        MapCell m = (MapCell) o;
        return m.getX() == this.getX() && m.getY() == this.getY();
    }
}
