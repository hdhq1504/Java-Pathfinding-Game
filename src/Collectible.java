import java.awt.Graphics;
import java.awt.Image;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author 11a5h
 */
public class Collectible {
    private int row, col;
    private Image image;
    private int points;
    private boolean collected = false;

    public Collectible(int row, int col, Image image, int points) {
        this.row = row;
        this.col = col;
        this.image = image;
        this.points = points;
    }

    public boolean isAt(int r, int c) {
        return row == r && col == c && !collected;
    }

    public void collect() {
        collected = true;
    }

    public void draw(Graphics g, int cellSize) {
        if (!collected) {
            g.drawImage(image, col * cellSize, row * cellSize, cellSize, cellSize, null);
        }
    }
    
    public boolean isCollected() {
        return collected;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    public int getPoints() {
        return points;
    }
}
