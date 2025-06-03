package POJO;

/**
 * Lớp Player đại diện cho người chơi trong game mê cung
 * Quản lý vị trí, hướng di chuyển và hình ảnh hiển thị của player
 */
public class Player {
    // Vị trí hiện tại của player trong lưới mê cung
    private int row;  // Tọa độ hàng (tọa độ y) - tăng khi đi xuống
    private int col;  // Tọa độ cột (tọa độ x) - tăng khi đi sang phải
    
    // Hướng di chuyển hiện tại của player
    private String currentDirection; // Lưu hướng: "up", "down", "left", "right"
    
    /**
     * Constructor khởi tạo player với vị trí bắt đầu
     * @param startRow - vị trí hàng ban đầu (tọa độ y xuất phát)
     * @param startCol - vị trí cột ban đầu (tọa độ x xuất phát)
     */
    public Player(int startRow, int startCol) {
        this.row = startRow;              // Gán vị trí hàng ban đầu
        this.col = startCol;              // Gán vị trí cột ban đầu
        this.currentDirection = "right";  // Hướng mặc định là đi sang phải
    }
    
    /**
     * Di chuyển player theo hướng được chỉ định
     * Cập nhật cả vị trí và hướng hiện tại của player
     * @param direction - hướng di chuyển ("up", "down", "left", "right")
     * @param maxRow - số hàng tối đa của mê cung (hiện tại không sử dụng)
     * @param maxCol - số cột tối đa của mê cung (hiện tại không sử dụng)
     */
    public void move(String direction, int maxRow, int maxCol) {
        this.currentDirection = direction; // Cập nhật hướng hiện tại
        
        // Switch case để xử lý di chuyển theo từng hướng
        switch (direction) {
            case "up":      // Di chuyển lên trên
                row--;      // Giảm tọa độ hàng (di chuyển lên)
                break;
            case "down":    // Di chuyển xuống dưới  
                row++;      // Tăng tọa độ hàng (di chuyển xuống)
                break;
            case "left":    // Di chuyển sang trái
                col--;      // Giảm tọa độ cột (di chuyển sang trái)
                break;
            case "right":   // Di chuyển sang phải
                col++;      // Tăng tọa độ cột (di chuyển sang phải)
                break;
            // Không có default case - nếu direction không hợp lệ thì không di chuyển
        }
    }
    
    /**
     * Getter method để lấy vị trí hàng hiện tại của player
     * @return vị trí hàng hiện tại (tọa độ y)
     */
    public int getRow() { 
        return row; // Trả về tọa độ hàng hiện tại
    }
    
    /**
     * Getter method để lấy vị trí cột hiện tại của player
     * @return vị trí cột hiện tại (tọa độ x)
     */
    public int getCol() { 
        return col; // Trả về tọa độ cột hiện tại
    }
    
    /**
     * Getter method để lấy hướng di chuyển hiện tại của player
     * @return hướng hiện tại dưới dạng chuỗi ("up", "down", "left", "right")
     */
    public String getCurrentDirection() { 
        return currentDirection; // Trả về hướng di chuyển hiện tại
    }
    
    /**
     * Lấy đường dẫn đến file hình ảnh tương ứng với hướng hiện tại
     * Mỗi hướng sẽ có một hình ảnh khác nhau để hiển thị
     * @return đường dẫn đến file hình ảnh (dạng String)
     */
    public String getCurrentImagePath() {
        // Ví dụ: "right" -> "images/kright.png"
        return "images/k" + currentDirection + ".png";
    }
}