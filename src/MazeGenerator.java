import java.awt.Image;
import java.io.IOException;
import java.util.Random;
import javax.swing.ImageIcon;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author 11a5h
 */
public class MazeGenerator {
    private int rows;
    private int cols;
    private Cell[][] grid;
    private Image[][] tileImages;
    private Image wallImg, floorImg, startImg, exitImg;
    private int startRow, startCol;
    private int exitRow, exitCol;
    private Random random;
    
    public int getRows() {
        return rows;
    }
    
    public int getCols() {
        return cols;
    }
    
    public int getStartRow() {
        return startRow;
    }
    
    public int getStartCol() {
        return startCol;
    }
    
    public int getExitRow() {
        return exitRow;
    }
    
    public int getExitCol() {
        return exitCol;
    }
    
    public Cell[][] getGrid() {
        return grid;
    }
    
    public static class Cell {
        int value;
        boolean visited;
        
        Cell() {
            value = 0;
            visited = false;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    public MazeGenerator(int rows, int cols, String wallImgPath, String floorImgPath,
            String startImgPath, String exitImgPath) throws IOException {
        this.rows = rows;
        this.cols = cols;
        this.random = new Random();

        this.startRow = 0;
        this.startCol = 0;
        this.exitRow = rows - 1;
        this.exitCol = cols - 1;

        loadImages(wallImgPath, floorImgPath, startImgPath, exitImgPath);

        grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell();
            }
        }

        generateMaze();
        
        grid[exitRow][exitCol].value = 1;

        ensurePathToExit();

        createTileImages();
    }
    
    private void loadImages(String wallImgPath, String floorImgPath, 
            String startImgPath, String exitImgPath) throws IOException {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            wallImg = new ImageIcon(classLoader.getResource(wallImgPath)).getImage();
            floorImg = new ImageIcon(classLoader.getResource(floorImgPath)).getImage();
            startImg = new ImageIcon(classLoader.getResource(startImgPath)).getImage();
            exitImg = new ImageIcon(classLoader.getResource(exitImgPath)).getImage();
        } catch (Exception e) {
            throw new IOException("Không thể đọc file hình ảnh: " + e.getMessage());
        }
    }
      
    private void generateMaze() {
        grid[startRow][startCol].value = 1;
        grid[exitRow][exitCol].value = 1;

        DFS(startRow, startCol);

        if (!grid[exitRow][exitCol].visited) {
            connectToMaze(exitRow, exitCol);
        }
    }
    
    private void DFS(int r, int c) {
        grid[r][c].visited = true;
        grid[r][c].value = 1;

        int[][] directions = {{-2,0}, {0,2}, {2,0}, {0,-2}};

        shuffleArray(directions);

        for (int[] dir : directions) {
            int newR = r + dir[0];
            int newC = c + dir[1];

            if (newR >= 0 && newR < rows && newC >= 0 && newC < cols && !grid[newR][newC].visited) {
                grid[r + dir[0]/2][c + dir[1]/2].value = 1;
                DFS(newR, newC);
            }
        }
    }
    
    private void ensurePathToExit() {
        int[][] neighbors = {
            {exitRow-1, exitCol},
            {exitRow, exitCol+1},
            {exitRow+1, exitCol},
            {exitRow, exitCol-1}
        };

        shuffleNeighbors(neighbors);
        
        boolean pathCreated = false;

        for (int[] neighbor : neighbors) {
            int r = neighbor[0];
            int c = neighbor[1];
            
            if (r >= 0 && r < rows && c >= 0 && c < cols) {
                if (grid[r][c].value == 1) {
                    pathCreated = true;
                    break;
                }
            }
        }
        
        if (!pathCreated) {
            for (int[] neighbor : neighbors) {
                int r = neighbor[0];
                int c = neighbor[1];
                
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    grid[r][c].value = 1;
                    connectToMaze(r, c);
                    pathCreated = true;
                    break;
                }
            }
        }
    }
    
    private void connectToMaze(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            return;
        }
        
        grid[r][c].value = 1;
        grid[r][c].visited = true;

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            int newR = r + dir[0];
            int newC = c + dir[1];

            if (newR >= 0 && newR < rows && newC >= 0 && newC < cols
                && grid[newR][newC].value == 1
                && grid[newR][newC].visited) {
                return;
            }
        }
        
        shuffleArray(directions);
        for (int[] dir : directions) {
            int newR = r + dir[0], newC = c + dir[1];

            if (newR >= 0 && newR < rows && newC >= 0 && newC < cols) {
                if (grid[newR][newC].visited) return;
                else {
                    grid[newR][newC].value = 1;
                    connectToMaze(newR, newC);
                    return;
                }
            }
        }
    }
    
    private void shuffleArray(int[][] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
    
    private void shuffleNeighbors(int[][] neighbors) {
        for (int i = neighbors.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = neighbors[i];
            neighbors[i] = neighbors[j];
            neighbors[j] = temp;
        }
    }
    
    private void createTileImages() {
        tileImages = new Image[rows][cols];
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == startRow && c == startCol) {
                    tileImages[r][c] = startImg; 
                } 
                else if (r == exitRow && c == exitCol) {
                    tileImages[r][c] = exitImg;
                } 
                else {
                    tileImages[r][c] = (grid[r][c].value == 1) ? floorImg : wallImg;
                }
            }
        }
    }
    
    public Image[][] getTileImages() {
        return tileImages;
    }
}
