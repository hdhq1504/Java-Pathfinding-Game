package POJO;

import java.awt.Graphics;
import java.awt.Image;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Lớp Collectible đại diện cho các vật phẩm có thể thu thập trong trò chơi maze
 * Mỗi collectible có vị trí, hình ảnh, điểm số và trạng thái đã thu thập hay chưa
 * @author 11a5h
 */
public class Collectible {
    // Vị trí của collectible trên lưới maze (hàng và cột)
    private int row, col;
    
    // Hình ảnh hiển thị của collectible
    private Image image;
    
    // Số điểm mà người chơi nhận được khi thu thập vật phẩm này
    private int points;
    
    // Trạng thái đã thu thập hay chưa (mặc định là false - chưa thu thập)
    private boolean collected = false;

    /**
     * Constructor khởi tạo một collectible mới
     * @param row - vị trí hàng của collectible
     * @param col - vị trí cột của collectible  
     * @param image - hình ảnh hiển thị của collectible
     * @param points - số điểm nhận được khi thu thập
     */
    public Collectible(int row, int col, Image image, int points) {
        this.row = row;          // Gán vị trí hàng
        this.col = col;          // Gán vị trí cột
        this.image = image;      // Gán hình ảnh
        this.points = points;    // Gán điểm số
    }

    /**
     * Kiểm tra xem collectible có ở vị trí được chỉ định hay không
     * và chưa bị thu thập
     * @param r - hàng cần kiểm tra
     * @param c - cột cần kiểm tra
     * @return true nếu collectible ở vị trí (r,c) và chưa bị thu thập
     */
    public boolean isAt(int r, int c) {
        // Trả về true nếu vị trí khớp và chưa bị thu thập
        return row == r && col == c && !collected;
    }

    /**
     * Đánh dấu collectible đã được thu thập
     * Thay đổi trạng thái collected thành true
     */
    public void collect() {
        collected = true;
    }

    /**
     * Vẽ collectible lên màn hình nếu chưa bị thu thập
     * @param g - đối tượng Graphics để vẽ
     * @param cellSize - kích thước của mỗi ô trong lưới
     */
    public void draw(Graphics g, int cellSize) {
        // Chỉ vẽ nếu chưa bị thu thập
        if (!collected) {
            // Vẽ hình ảnh tại vị trí tương ứng với kích thước ô
            // col * cellSize: tọa độ x
            // row * cellSize: tọa độ y
            // cellSize, cellSize: chiều rộng và chiều cao
            g.drawImage(image, col * cellSize, row * cellSize, cellSize, cellSize, null);
        }
    }
    
    /**
     * Getter method để kiểm tra trạng thái đã thu thập
     * @return true nếu đã thu thập, false nếu chưa
     */
    public boolean isCollected() {
        return collected;
    }
    
    /**
     * Getter method để lấy vị trí hàng của collectible
     * @return vị trí hàng
     */
    public int getRow() {
        return row;
    }
    
    /**
     * Getter method để lấy vị trí cột của collectible
     * @return vị trí cột
     */
    public int getCol() {
        return col;
    }
    
    /**
     * Getter method để lấy số điểm của collectible
     * @return số điểm nhận được khi thu thập
     */
    public int getPoints() {
        return points;
    }
}