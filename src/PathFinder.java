
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author 11a5h
 */
public class PathFinder {
    private MazeGenerator maze;
    private int[][] grid;
    private boolean[][] visited;
    private int rows, cols;
    private int startRow, startCol;
    private int exitRow, exitCol;
    private List<Node> path;
    private List<Node> exploredNodes;
    
    public List<Node> getExploredNodes() {
        return exploredNodes;
    }

    public List<Node> getPath() {
        return path;
    }
    
    public static class Node {
        int row, col;
        Node parent;
        int h;
        
        public Node(int row, int col) {
            this.row = row;
            this.col = col;
            this.parent = null;
            this.h = 0;
        }
        
        public int getH() {
            return h;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return row == node.row && col == node.col;
        }
    }
    
    public PathFinder(MazeGenerator maze) {
        this(maze, maze.getStartRow(), maze.getStartCol(), maze.getExitRow(), maze.getExitCol());
    }
    
    public PathFinder(MazeGenerator maze, int startRow, int startCol, int exitRow, int exitCol) {
        this.maze = maze;
        this.rows = maze.getRows();
        this.cols = maze.getCols();
        this.startRow = startRow;
        this.startCol = startCol;
        this.exitRow = exitRow;
        this.exitCol = exitCol;
        
        createGridCopy();
        
        this.path = new ArrayList<>();
        this.exploredNodes = new ArrayList<>();
    }
    
    private void createGridCopy() {
        grid = new int[rows][cols];
        MazeGenerator.Cell[][] mazeGrid = maze.getGrid();
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Use the maze's grid directly to check walkable positions
                grid[r][c] = mazeGrid[r][c].getValue();
            }
        }
    }
    
    public List<Node> findPath() {
        resetSearch();
        
        visited = new boolean[rows][cols];
        Node startNode = new Node(startRow, startCol);
        startNode.h = calculateHeuristic(startRow, startCol);
        
        boolean found = hillClimbingWithBacktracking(startNode);
        
        if (found) {
            return path;
        } 
        else {
            return Collections.emptyList();
        }
    }
    
    private boolean hillClimbingWithBacktracking(Node current) {
        visited[current.row][current.col] = true;
        exploredNodes.add(current);
        
        if (current.row == exitRow && current.col == exitCol) {
            reconstructPath(current);
            return true;
        }

        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

        List<Node> neighbors = new ArrayList<>();
        
        for (int[] dir : directions) {
            int newRow = current.row + dir[0];
            int newCol = current.col + dir[1];
            
            if (isValidMove(newRow, newCol) && !visited[newRow][newCol]) {
                Node neighbor = new Node(newRow, newCol);
                neighbor.parent = current;
                neighbor.h = calculateHeuristic(newRow, newCol);
                neighbors.add(neighbor);
            }
        }

        Collections.sort(neighbors, (n1, n2) -> Integer.compare(n1.h, n2.h));

        for (Node neighbor : neighbors) {
            if (hillClimbingWithBacktracking(neighbor)) {
                return true;
            }
        }

        return false;
    }
    
    private int calculateHeuristic(int row, int col) {
        return Math.abs(row - exitRow) + Math.abs(col - exitCol);
    }
    
    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols && grid[row][col] == 1;
    }

    private void reconstructPath(Node endNode) {
        path.clear();
        Node current = endNode;
        
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        
        Collections.reverse(path);
    }

    private void resetSearch() {
        path.clear();
        exploredNodes.clear();
    }
    
    public void drawPathHighlights(Graphics g, int cellSize) {
        // Draw explored nodes (light blue)
        g.setColor(new Color(173, 216, 230, 128));
        for (Node node : exploredNodes) {
            // Skip start and target positions
            if ((node.row != startRow || node.col != startCol) && 
                (node.row != exitRow || node.col != exitCol)) {
                g.fillRect(node.col * cellSize, node.row * cellSize, cellSize, cellSize);
            }
        }
        
        // Draw optimal path (yellow)
        g.setColor(new Color(255, 255, 0, 180));
        for (Node node : path) {
            // Skip start and target positions to avoid covering them
            if ((node.row != startRow || node.col != startCol) && 
                (node.row != exitRow || node.col != exitCol)) {
                g.fillRect(node.col * cellSize, node.row * cellSize, cellSize, cellSize);
            }
        }
    }
}
