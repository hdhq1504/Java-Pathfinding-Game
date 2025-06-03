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
 * Lớp PathFinder sử dụng thuật toán Hill Climbing với Backtracking
 * để tìm đường đi từ điểm bắt đầu đến đích trong mê cung
 * Hill Climbing: thuật toán tham lam, luôn chọn bước đi tốt nhất dựa trên heuristic
 * Backtracking: quay lại khi gặp ngõ cụt để thử các lựa chọn khác
 * @author 11a5h
 */
public class PathFinder {
    // Tham chiếu đến đối tượng mê cung - để truy cập thông tin về mê cung gốc
    private MazeGenerator maze;
    
    // Bản sao của lưới mê cung để xử lý - ma trận 2D chứa các giá trị 0 (tường) và 1 (đường đi)
    private int[][] grid;
    
    // Mảng đánh dấu các ô đã được thăm - để tránh đi vòng lặp vô hạn
    private boolean[][] visited;
    
    // Kích thước của mê cung - số hàng và số cột
    private int rows, cols;
    
    // Vị trí điểm bắt đầu - tọa độ hàng và cột của điểm xuất phát
    private int startRow, startCol;
    // Vị trí điểm đích - tọa độ hàng và cột của điểm đích cần đến
    private int exitRow, exitCol;
    
    // Danh sách lưu đường đi tối ưu - chứa các node từ start đến exit
    private List<Node> path;
    
    // Danh sách lưu tất cả các ô đã được khám phá - để hiển thị quá trình tìm kiếm
    private List<Node> exploredNodes;
    
    /**
     * Getter method để lấy danh sách các ô đã khám phá
     * Phục vụ cho việc visualization - hiển thị các ô đã được thuật toán ghé thăm
     * @return danh sách các node đã khám phá
     */
    public List<Node> getExploredNodes() {
        return exploredNodes; // Trả về danh sách các node đã được khám phá
    }

    /**
     * Getter method để lấy đường đi tối ưu
     * Đây là kết quả chính của thuật toán tìm đường
     * @return danh sách các node trong đường đi tối ưu từ start đến exit
     */
    public List<Node> getPath() {
        return path; // Trả về đường đi tối ưu đã tìm được
    }
    
    /**
     * Lớp Node đại diện cho một ô trong mê cung
     * Chứa thông tin vị trí, node cha và heuristic value
     * Đây là inner static class - có thể truy cập mà không cần instance của PathFinder
     */
    public static class Node {
        int row, col;        // Vị trí của node (hàng, cột) trong lưới mê cung
        Node parent;         // Node cha để truy vết đường đi - dùng để xây dựng lại path
        int h;              // Giá trị heuristic (khoảng cách Manhattan đến đích)
        
        /**
         * Constructor tạo node mới với vị trí được chỉ định
         * @param row - vị trí hàng (tọa độ y)
         * @param col - vị trí cột (tọa độ x)
         */
        public Node(int row, int col) {
            this.row = row;      // Gán vị trí hàng
            this.col = col;      // Gán vị trí cột
            this.parent = null;  // Ban đầu không có node cha (chưa biết đến từ đâu)
            this.h = 0;         // Giá trị heuristic ban đầu là 0 (sẽ được tính sau)
        }
        
        /**
         * Getter method để lấy giá trị heuristic
         * Heuristic dùng để ước lượng khoảng cách đến đích
         * @return giá trị heuristic (khoảng cách Manhattan)
         */
        public int getH() {
            return h; // Trả về giá trị heuristic đã tính
        }
        
        /**
         * Override phương thức equals để so sánh hai node
         * Hai node bằng nhau nếu có cùng vị trí (row, col)
         * Cần thiết để sử dụng trong các collection như List, Set
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;  // Nếu là cùng một đối tượng trong bộ nhớ
            if (obj == null || getClass() != obj.getClass()) return false;  // Null hoặc khác class
            Node node = (Node) obj;        // Ép kiểu về Node
            return row == node.row && col == node.col;  // So sánh vị trí hàng và cột
        }
    }
    
    /**
     * Constructor sử dụng điểm bắt đầu và kết thúc mặc định của mê cung
     * Thuận tiện khi muốn dùng start/exit có sẵn trong MazeGenerator
     * @param maze - đối tượng mê cung chứa thông tin về lưới và điểm start/exit
     */
    public PathFinder(MazeGenerator maze) {
        // Gọi constructor chính với thông tin từ maze
        this(maze, maze.getStartRow(), maze.getStartCol(), maze.getExitRow(), maze.getExitCol());
    }
    
    /**
     * Constructor với điểm bắt đầu và kết thúc tùy chỉnh
     * Cho phép linh hoạt trong việc chọn điểm start và exit
     * @param maze - đối tượng mê cung
     * @param startRow - hàng điểm bắt đầu
     * @param startCol - cột điểm bắt đầu  
     * @param exitRow - hàng điểm kết thúc
     * @param exitCol - cột điểm kết thúc
     */
    public PathFinder(MazeGenerator maze, int startRow, int startCol, int exitRow, int exitCol) {
        this.maze = maze;                // Lưu tham chiếu đến mê cung gốc
        this.rows = maze.getRows();      // Lấy số hàng của mê cung
        this.cols = maze.getCols();      // Lấy số cột của mê cung
        this.startRow = startRow;        // Lưu vị trí hàng điểm bắt đầu
        this.startCol = startCol;        // Lưu vị trí cột điểm bắt đầu
        this.exitRow = exitRow;          // Lưu vị trí hàng điểm đích
        this.exitCol = exitCol;          // Lưu vị trí cột điểm đích
        
        // Tạo bản sao của lưới mê cung để xử lý thuật toán
        createGridCopy();
        
        // Khởi tạo danh sách rỗng để lưu kết quả
        this.path = new ArrayList<>();         // Danh sách đường đi tối ưu
        this.exploredNodes = new ArrayList<>(); // Danh sách các node đã khám phá
    }
    
    /**
     * Tạo bản sao của lưới mê cung để xử lý
     * Chuyển đổi từ Cell[][] sang int[][] để thuận tiện cho thuật toán
     * 0 = tường (không thể đi), 1 = đường đi (có thể đi)
     */
    private void createGridCopy() {
        grid = new int[rows][cols];              // Tạo ma trận 2D với kích thước của mê cung
        MazeGenerator.Cell[][] mazeGrid = maze.getGrid(); // Lấy lưới gốc từ MazeGenerator
        
        // Sao chép giá trị từ mê cung gốc sang ma trận int
        for (int r = 0; r < rows; r++) {         // Duyệt qua từng hàng
            for (int c = 0; c < cols; c++) {     // Duyệt qua từng cột
                // Lấy giá trị từ Cell và chuyển sang int (0 hoặc 1)
                grid[r][c] = mazeGrid[r][c].getValue();
            }
        }
    }
    
    /**
     * Phương thức chính để tìm đường đi từ start đến exit
     * Sử dụng thuật toán Hill Climbing với Backtracking
     * Hill Climbing: chọn neighbor có heuristic tốt nhất
     * Backtracking: quay lại khi không tìm được đường đi
     * @return danh sách các node trong đường đi, rỗng nếu không tìm thấy
     */
    public List<Node> findPath() {
        // Đặt lại trạng thái tìm kiếm - xóa kết quả cũ
        resetSearch();
        
        // Khởi tạo mảng đánh dấu đã thăm - tất cả đều false ban đầu
        visited = new boolean[rows][cols];
        
        // Tạo node bắt đầu và tính heuristic cho nó
        Node startNode = new Node(startRow, startCol);
        startNode.h = calculateHeuristic(startRow, startCol); // Tính khoảng cách đến đích
        
        // Bắt đầu thuật toán Hill Climbing với Backtracking
        boolean found = hillClimbingWithBacktracking(startNode);
        
        if (found) {
            return path;  // Trả về đường đi nếu tìm thấy đích
        } 
        else {
            return Collections.emptyList();  // Trả về danh sách rỗng nếu không tìm thấy
        }
    }
    
    /**
     * Thuật toán Hill Climbing với Backtracking - đệ quy
     * @param current - node hiện tại đang xét
     * @return true nếu tìm thấy đường đi đến đích, false nếu không
     */
    private boolean hillClimbingWithBacktracking(Node current) {
        // Đánh dấu node hiện tại đã được thăm
        visited[current.row][current.col] = true;
        // Thêm vào danh sách đã khám phá để visualization
        exploredNodes.add(current);
        
        // Kiểm tra xem đã đến đích chưa
        if (current.row == exitRow && current.col == exitCol) {
            reconstructPath(current); // Xây dựng lại đường đi từ đích về start
            return true;              // Đã tìm thấy đích
        }

        // Định nghĩa 4 hướng di chuyển: lên, phải, xuống, trái
        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        //                     lên      phải    xuống   trái

        // Danh sách các neighbor hợp lệ
        List<Node> neighbors = new ArrayList<>();
        
        // Tìm tất cả các neighbor hợp lệ
        for (int[] dir : directions) {
            int newRow = current.row + dir[0]; // Tính vị trí hàng mới
            int newCol = current.col + dir[1]; // Tính vị trí cột mới
            
            // Kiểm tra xem vị trí mới có hợp lệ và chưa được thăm chưa
            if (isValidMove(newRow, newCol) && !visited[newRow][newCol]) {
                Node neighbor = new Node(newRow, newCol);           // Tạo neighbor mới
                neighbor.parent = current;                          // Gán node cha
                neighbor.h = calculateHeuristic(newRow, newCol);    // Tính heuristic
                neighbors.add(neighbor);                            // Thêm vào danh sách
            }
        }

        // Sắp xếp neighbors theo heuristic tăng dần (Hill Climbing)
        // Node có heuristic nhỏ hơn (gần đích hơn) sẽ được ưu tiên
        Collections.sort(neighbors, (n1, n2) -> Integer.compare(n1.h, n2.h));

        // Thử từng neighbor theo thứ tự đã sắp xếp
        for (Node neighbor : neighbors) {
            // Đệ quy tìm đường đi từ neighbor
            if (hillClimbingWithBacktracking(neighbor)) {
                return true; // Nếu tìm thấy đường đi từ neighbor này
            }
            // Nếu không tìm thấy, thuật toán sẽ backtrack và thử neighbor tiếp theo
        }

        // Không tìm thấy đường đi từ tất cả neighbors
        return false;
    }
    
    /**
     * Tính toán giá trị heuristic (khoảng cách Manhattan) từ vị trí hiện tại đến đích
     * Manhattan distance = |x1-x2| + |y1-y2|
     * Đây là ước lượng khoảng cách ngắn nhất (không tính tường cản)
     * @param row - vị trí hàng hiện tại
     * @param col - vị trí cột hiện tại
     * @return khoảng cách Manhattan đến đích
     */
    private int calculateHeuristic(int row, int col) {
        return Math.abs(row - exitRow) + Math.abs(col - exitCol);
    }
    
    /**
     * Kiểm tra xem một bước di chuyển có hợp lệ không
     * @param row - vị trí hàng cần kiểm tra
     * @param col - vị trí cột cần kiểm tra
     * @return true nếu có thể di chuyển đến vị trí này, false nếu không
     */
    private boolean isValidMove(int row, int col) {
        return row >= 0 &&           // Không vượt quá biên trên
               row < rows &&         // Không vượt quá biên dưới
               col >= 0 &&           // Không vượt quá biên trái  
               col < cols &&         // Không vượt quá biên phải
               grid[row][col] == 1;  // Là đường đi (không phải tường)
    }

    /**
     * Xây dựng lại đường đi từ node đích về node bắt đầu
     * Sử dụng liên kết parent để truy vết ngược
     * @param endNode - node đích (node cuối cùng tìm được)
     */
    private void reconstructPath(Node endNode) {
        path.clear();           // Xóa đường đi cũ (nếu có)
        Node current = endNode; // Bắt đầu từ node đích
        
        // Truy vết ngược từ đích về start thông qua parent
        while (current != null) {
            path.add(current);        // Thêm node hiện tại vào đường đi
            current = current.parent; // Di chuyển đến node cha
        }
        
        // Đảo ngược danh sách để có đường đi từ start đến exit
        Collections.reverse(path);
    }

    /**
     * Đặt lại trạng thái tìm kiếm - xóa kết quả của lần tìm kiếm trước
     * Cần thiết khi muốn tìm kiếm lại hoặc tìm kiếm với điều kiện mới
     */
    private void resetSearch() {
        path.clear();           // Xóa đường đi cũ
        exploredNodes.clear();  // Xóa danh sách node đã khám phá
    }
    
    /**
     * Vẽ các highlight để hiển thị quá trình tìm kiếm và kết quả
     * @param g - đối tượng Graphics để vẽ
     * @param cellSize - kích thước của mỗi ô trong pixel
     */
    public void drawPathHighlights(Graphics g, int cellSize) {
        // Vẽ các node đã khám phá (màu xanh nhạt với độ trong suốt)
        g.setColor(new Color(173, 216, 230, 128));
        for (Node node : exploredNodes) {
            // Bỏ qua vị trí start và target để không che khuất chúng
            if ((node.row != startRow || node.col != startCol) && 
                (node.row != exitRow || node.col != exitCol)) {
                // Vẽ hình chữ nhật tô màu cho node đã khám phá
                g.fillRect(node.col * cellSize,    // Tọa độ x (cột * kích thước ô)
                          node.row * cellSize,     // Tọa độ y (hàng * kích thước ô)  
                          cellSize,                // Chiều rộng
                          cellSize);               // Chiều cao
            }
        }
        
        // Vẽ đường đi tối ưu (màu vàng với độ trong suốt)
        g.setColor(new Color(255, 255, 0, 180));
        for (Node node : path) {
            // Bỏ qua vị trí start và target để không che khuất chúng
            if ((node.row != startRow || node.col != startCol) && 
                (node.row != exitRow || node.col != exitCol)) {
                // Vẽ hình chữ nhật tô màu cho đường đi tối ưu
                g.fillRect(node.col * cellSize,    // Tọa độ x
                          node.row * cellSize,     // Tọa độ y
                          cellSize,                // Chiều rộng  
                          cellSize);               // Chiều cao
            }
        }
    }
}