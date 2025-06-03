import java.awt.Image;
import java.io.IOException;
import java.util.Random;
import javax.swing.ImageIcon;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Lớp MazeGenerator chịu trách nhiệm tạo ra một mê cung ngẫu nhiên
 * Sử dụng thuật toán Depth-First Search (DFS) để tạo đường đi
 * @author 11a5h
 */
public class MazeGenerator {
    // Số hàng và cột trong lưới mê cung
    private int rows;
    private int cols;
    
    // Ma trận chứa thông tin về từng ô trong mê cung
    private Cell[][] grid;
    
    // Ma trận chứa hình ảnh tương ứng cho từng ô
    private Image[][] tileImages;
    
    // Các hình ảnh được sử dụng cho tường, sàn, điểm bắt đầu và lối thoát
    private Image wallImg, floorImg, startImg, exitImg;
    
    // Vị trí điểm bắt đầu và lối thoát
    private int startRow, startCol;
    private int exitRow, exitCol;
    
    // Đối tượng Random để tạo tính ngẫu nhiên trong thuật toán
    private Random random;
    
    // Các getter methods để truy cập thông tin về mê cung
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
    
    /**
     * Lớp Cell đại diện cho một ô trong mê cung
     * Mỗi ô có giá trị (0=tường, 1=đường đi) và trạng thái đã thăm
     */
    public static class Cell {
        int value;      // 0: tường, 1: đường đi có thể đi qua
        boolean visited; // Đánh dấu ô đã được thăm trong quá trình tạo mê cung
        
        /**
         * Constructor khởi tạo ô mới với giá trị mặc định
         */
        Cell() {
            value = 0;       // Mặc định là tường
            visited = false; // Chưa được thăm
        }
        
        /**
         * Getter method để lấy giá trị của ô
         * @return 0 nếu là tường, 1 nếu là đường đi
         */
        public int getValue() {
            return value;
        }
    }
    
    /**
     * Constructor khởi tạo MazeGenerator và tạo mê cung
     * @param rows - số hàng của mê cung
     * @param cols - số cột của mê cung
     * @param wallImgPath - đường dẫn đến hình ảnh tường
     * @param floorImgPath - đường dẫn đến hình ảnh sàn
     * @param startImgPath - đường dẫn đến hình ảnh điểm bắt đầu
     * @param exitImgPath - đường dẫn đến hình ảnh lối thoát
     * @throws IOException nếu không thể tải hình ảnh
     */
    public MazeGenerator(int rows, int cols, String wallImgPath, String floorImgPath,
            String startImgPath, String exitImgPath) throws IOException {
        this.rows = rows;
        this.cols = cols;
        this.random = new Random(); // Khởi tạo đối tượng Random

        // Thiết lập vị trí bắt đầu ở góc trên trái
        this.startRow = 0;
        this.startCol = 0;
        
        // Thiết lập vị trí lối thoát ở góc dưới phải
        this.exitRow = rows - 1;
        this.exitCol = cols - 1;

        // Tải các hình ảnh cần thiết
        loadImages(wallImgPath, floorImgPath, startImgPath, exitImgPath);

        // Khởi tạo ma trận lưới
        grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell(); // Tạo ô mới cho mỗi vị trí
            }
        }

        // Tạo mê cung sử dụng thuật toán DFS
        generateMaze();
        
        // Đảm bảo lối thoát là đường đi (không phải tường)
        grid[exitRow][exitCol].value = 1;

        // Đảm bảo có đường đi đến lối thoát
        ensurePathToExit();

        // Tạo ma trận hình ảnh tương ứng với từng ô
        createTileImages();
    }
    
    /**
     * Tải các hình ảnh từ đường dẫn được cung cấp
     * @param wallImgPath - đường dẫn hình ảnh tường
     * @param floorImgPath - đường dẫn hình ảnh sàn
     * @param startImgPath - đường dẫn hình ảnh điểm bắt đầu
     * @param exitImgPath - đường dẫn hình ảnh lối thoát
     * @throws IOException nếu không thể tải hình ảnh
     */
    private void loadImages(String wallImgPath, String floorImgPath, 
            String startImgPath, String exitImgPath) throws IOException {
        try {
            // Sử dụng ClassLoader để tải hình ảnh từ resources
            ClassLoader classLoader = getClass().getClassLoader();
            wallImg = new ImageIcon(classLoader.getResource(wallImgPath)).getImage();
            floorImg = new ImageIcon(classLoader.getResource(floorImgPath)).getImage();
            startImg = new ImageIcon(classLoader.getResource(startImgPath)).getImage();
            exitImg = new ImageIcon(classLoader.getResource(exitImgPath)).getImage();
        } catch (Exception e) {
            // Ném ngoại lệ nếu không thể tải hình ảnh
            throw new IOException("Không thể đọc file hình ảnh: " + e.getMessage());
        }
    }
      
    /**
     * Tạo mê cung sử dụng thuật toán Depth-First Search (DFS)
     * Bắt đầu từ điểm start và đảm bảo có đường đi đến exit
     */
    private void generateMaze() {
        // Đánh dấu điểm bắt đầu và lối thoát là đường đi
        grid[startRow][startCol].value = 1;
        grid[exitRow][exitCol].value = 1;

        // Bắt đầu DFS từ điểm bắt đầu
        DFS(startRow, startCol);

        // Nếu lối thoát chưa được thăm, kết nối nó với mê cung
        if (!grid[exitRow][exitCol].visited) {
            connectToMaze(exitRow, exitCol);
        }
    }
    
    /**
     * Thuật toán Depth-First Search để tạo đường đi trong mê cung
     * @param r - hàng hiện tại
     * @param c - cột hiện tại
     */
    private void DFS(int r, int c) {
        // Đánh dấu ô hiện tại đã được thăm và là đường đi
        grid[r][c].visited = true;
        grid[r][c].value = 1;

        // Các hướng di chuyển: lên, phải, xuống, trái (bước 2 ô)
        int[][] directions = {{-2,0}, {0,2}, {2,0}, {0,-2}};

        // Xáo trộn các hướng để tạo tính ngẫu nhiên
        shuffleArray(directions);

        // Thử từng hướng
        for (int[] dir : directions) {
            int newR = r + dir[0];  // Vị trí mới theo hàng
            int newC = c + dir[1];  // Vị trí mới theo cột

            // Kiểm tra vị trí mới có hợp lệ và chưa được thăm
            if (newR >= 0 && newR < rows && newC >= 0 && newC < cols && !grid[newR][newC].visited) {
                // Tạo đường đi giữa ô hiện tại và ô mới (phá tường)
                grid[r + dir[0]/2][c + dir[1]/2].value = 1;
                // Tiếp tục DFS từ ô mới
                DFS(newR, newC);
            }
        }
    }
    
    /**
     * Đảm bảo có ít nhất một đường đi đến lối thoát
     * Kiểm tra các ô xung quanh lối thoát và tạo kết nối nếu cần
     */
    private void ensurePathToExit() {
        // Các ô lân cận của lối thoát
        int[][] neighbors = {
            {exitRow-1, exitCol},  // Trên
            {exitRow, exitCol+1},  // Phải
            {exitRow+1, exitCol},  // Dưới
            {exitRow, exitCol-1}   // Trái
        };

        // Xáo trộn thứ tự kiểm tra
        shuffleNeighbors(neighbors);
        
        boolean pathCreated = false;

        // Kiểm tra xem có ô nào xung quanh đã là đường đi chưa
        for (int[] neighbor : neighbors) {
            int r = neighbor[0];
            int c = neighbor[1];
            
            // Nếu ô hợp lệ và là đường đi thì đã có kết nối
            if (r >= 0 && r < rows && c >= 0 && c < cols) {
                if (grid[r][c].value == 1) {
                    pathCreated = true;
                    break;
                }
            }
        }
        
        // Nếu chưa có kết nối, tạo một kết nối mới
        if (!pathCreated) {
            for (int[] neighbor : neighbors) {
                int r = neighbor[0];
                int c = neighbor[1];
                
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    // Biến ô này thành đường đi
                    grid[r][c].value = 1;
                    // Kết nối ô này với phần còn lại của mê cung
                    connectToMaze(r, c);
                    pathCreated = true;
                    break;
                }
            }
        }
    }
    
    /**
     * Kết nối một ô với mê cung hiện có
     * @param r - hàng của ô cần kết nối
     * @param c - cột của ô cần kết nối
     */
    private void connectToMaze(int r, int c) {
        // Kiểm tra tính hợp lệ của vị trí
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            return;
        }
        
        // Đánh dấu ô này là đường đi và đã thăm
        grid[r][c].value = 1;
        grid[r][c].visited = true;

        // Các hướng di chuyển (bước 1 ô)
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        // Kiểm tra xem có ô nào xung quanh đã là đường đi và đã thăm
        for (int[] dir : directions) {
            int newR = r + dir[0];
            int newC = c + dir[1];

            if (newR >= 0 && newR < rows && newC >= 0 && newC < cols
                && grid[newR][newC].value == 1
                && grid[newR][newC].visited) {
                return; // Đã có kết nối
            }
        }
        
        // Xáo trộn các hướng và thử kết nối
        shuffleArray(directions);
        for (int[] dir : directions) {
            int newR = r + dir[0], newC = c + dir[1];

            if (newR >= 0 && newR < rows && newC >= 0 && newC < cols) {
                if (grid[newR][newC].visited) return; // Đã tìm thấy kết nối
                else {
                    // Tạo kết nối mới
                    grid[newR][newC].value = 1;
                    connectToMaze(newR, newC);
                    return;
                }
            }
        }
    }
    
    /**
     * Xáo trộn một mảng 2 chiều để tạo tính ngẫu nhiên
     * @param array - mảng cần xáo trộn
     */
    private void shuffleArray(int[][] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);  // Chọn index ngẫu nhiên
            // Hoán đổi phần tử
            int[] temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
    
    /**
     * Xáo trộn mảng các ô lân cận
     * @param neighbors - mảng các ô lân cận
     */
    private void shuffleNeighbors(int[][] neighbors) {
        for (int i = neighbors.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = neighbors[i];
            neighbors[i] = neighbors[j];
            neighbors[j] = temp;
        }
    }
    
    /**
     * Tạo ma trận hình ảnh tương ứng với từng ô trong mê cung
     * Gán hình ảnh phù hợp cho từng loại ô
     */
    private void createTileImages() {
        tileImages = new Image[rows][cols];
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == startRow && c == startCol) {
                    // Điểm bắt đầu sử dụng hình ảnh sàn
                    tileImages[r][c] = floorImg;
                } 
                else if (r == exitRow && c == exitCol) {
                    // Lối thoát sử dụng hình ảnh exit
                    tileImages[r][c] = exitImg;
                } 
                else {
                    // Các ô khác: sàn nếu là đường đi (value=1), tường nếu không (value=0)
                    tileImages[r][c] = (grid[r][c].value == 1) ? floorImg : wallImg;
                }
            }
        }
    }
    
    /**
     * Getter method để lấy ma trận hình ảnh
     * @return ma trận hình ảnh 2 chiều
     */
    public Image[][] getTileImages() {
        return tileImages;
    }
}