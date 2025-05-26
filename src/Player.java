public class Player {
    private int row;
    private int col;
    private String currentDirection;
    
    public Player(int startRow, int startCol) {
        this.row = startRow;
        this.col = startCol;
        this.currentDirection = "right";
    }
    
    public void move(String direction, int maxRow, int maxCol) {
        this.currentDirection = direction;
        switch (direction) {
            case "up":
                row--;
                break;
            case "down":
                row++;
                break;
            case "left":
                col--;
                break;
            case "right":
                col++;
                break;
        }
    }
    
    public int getRow() { 
        return row; 
    }
    
    public int getCol() { 
        return col; 
    }
    
    public String getCurrentDirection() { 
        return currentDirection; 
    }
    
    public String getCurrentImagePath() {
        return "images/k" + currentDirection + ".png";
    }
}
