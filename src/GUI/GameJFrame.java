package GUI;

import POJO.Player;
import POJO.Collectible;
import DAO.PathFinder;
import DAO.MazeGenerator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.ImageIcon;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 * Lớp GameJFrame - Giao diện chính của trò chơi tìm đường trong mê cung
 * Kế thừa từ JFrame và implements KeyListener để xử lý sự kiện bàn phím
 * @author 11a5h
 */
public class GameJFrame extends javax.swing.JFrame implements KeyListener {
    // Đối tượng tạo mê cung
    private MazeGenerator maze;
    
    // Đối tượng người chơi
    private Player player;
    
    // Đối tượng tìm đường (dùng cho gợi ý)
    private PathFinder pathFinder;
    
    // Kích thước mê cung (số ô theo hàng/cột)
    private int size = 50;
    
    // Kích thước mỗi ô trong mê cung (pixel)
    private int cellSize = 80;
    
    // Vị trí hàng và cột của người chơi
    private int playerRow = 0, playerCol = 0;
    
    // Điểm số hiện tại của người chơi
    private int score = 0;
    
    // Tổng điểm tích lũy qua các màn chơi
    private int total = 0;
    
    // Timer để đếm ngược thời gian
    private Timer gameTimer;
    
    // Thời gian ban đầu cho mỗi màn (giây)
    private int initialTime = 300; // 5 phút
    
    // Thời gian còn lại trong màn hiện tại
    private int timeRemaining = 300;
    
    // Kiểm tra xem đã sử dụng gợi ý chưa
    private boolean hintUsed = false;
    
    // Danh sách các vật phẩm có thể thu thập trong mê cung
    private List<Collectible> collectibles = new ArrayList<>();
    
    // Trạng thái game đã bắt đầu chưa
    private boolean gameStarted = false;
    
    // Trạng thái đã thắng game chưa
    private boolean gameWon = false;
    
    // Số lần thắng (dùng để tăng độ khó)
    private int win = 0;
    
    // Đường dẫn đến các hình ảnh
    private final String wallImgPath = "images/wall.png";      // Hình tường
    private final String floorImgPath = "images/floor.png";    // Hình sàn
    private final String startImgPath = "";                    // Hình điểm bắt đầu (trống)
    private final String exitImgPath = "images/DoorWin.png";   // Hình cửa ra
    
    // Kích thước panel hiển thị mê cung
    private final int PANEL_WIDTH = 900, PANEL_HEIGHT = 900;

    /**
     * Creates new form GameJFrame
     */
    public GameJFrame() { 
        // Khởi tạo các components của giao diện
        initComponents();
        
        // Thiết lập panel hiển thị mê cung
        setUpMazePanel();
        
        // Đặt cửa sổ ở giữa màn hình
        this.setLocationRelativeTo(null);
        
        // Thêm listener để lắng nghe sự kiện bàn phím
        this.addKeyListener(this);
        
        // Cho phép nhận focus để có thể bắt sự kiện phím
        this.setFocusable(true);
    }
    
    /**
     * Thiết lập panel hiển thị mê cung
     */
    private void setUpMazePanel() {
        // Xóa tất cả components hiện tại trong mazePanel
        mazePanel.removeAll();
        
        // Tính toán kích thước ô dựa trên kích thước panel
        calculateCellSize();
        
        // Tạo panel vẽ mê cung với method paintComponent được override
        drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Gọi method cha để vẽ background
                super.paintComponent(g);
                
                // Chỉ vẽ khi đã có mê cung
                if (maze != null) {
                    // Lấy mảng hình ảnh các ô từ maze
                    Image[][] tiles = maze.getTileImages();
                    int rows = maze.getRows(), cols = maze.getCols();
                    
                    // Vẽ từng ô của mê cung
                    for (int r = 0; r < rows; r++) {
                        for (int c = 0; c < cols; c++) {
                            if (tiles[r][c] != null) {
                                // Vẽ hình ảnh ô tại vị trí (c*cellSize, r*cellSize)
                                g.drawImage(tiles[r][c], c * cellSize, r * cellSize, cellSize, cellSize, null);
                            }
                        }
                    }
                    
                    // Vẽ đường gợi ý nếu có
                    if (pathFinder != null) {
                        pathFinder.drawPathHighlights(g, cellSize);
                    }
                    
                    // Vẽ tất cả các vật phẩm có thể thu thập
                    for (Collectible collectible : collectibles) {
                        collectible.draw(g, cellSize);
                    }
                    
                    // Vẽ người chơi nếu game đã bắt đầu và chưa thắng
                    if (gameStarted && player != null && !gameWon) {
                        try {
                            // Lấy ClassLoader để load hình ảnh từ resources
                            ClassLoader classLoader = getClass().getClassLoader();
                            // Tạo Image từ đường dẫn hình ảnh của player
                            Image playerImg = new ImageIcon(classLoader.getResource(player.getCurrentImagePath())).getImage();
                            // Vẽ player tại vị trí hiện tại
                            g.drawImage(playerImg, player.getCol() * cellSize, player.getRow() * cellSize, cellSize, cellSize, null);
                        } catch (Exception e) {
                            // In lỗi nếu không thể tải hình ảnh player
                            System.err.println("Không thể tải hình ảnh player: " + e.getMessage());
                        }
                    }
                }
            }
        };
        
        // Đặt kích thước ưa thích cho drawPanel
        drawPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        
        // Thiết lập layout cho mazePanel và thêm drawPanel vào
        mazePanel.setLayout(new BorderLayout());
        mazePanel.add(drawPanel, BorderLayout.CENTER);
        
        // Làm mới và vẽ lại giao diện
        mazePanel.revalidate();
        mazePanel.repaint();
    }
    
    /**
     * Tính toán kích thước mỗi ô dựa trên kích thước panel và số ô
     */
    private void calculateCellSize() {
         // Tính kích thước ô theo chiều rộng
         int cellWidth = PANEL_WIDTH / size;
         // Tính kích thước ô theo chiều cao
         int cellHeight = PANEL_HEIGHT / size;
         // Chọn kích thước nhỏ hơn để đảm bảo mê cung vừa với panel
         cellSize = Math.min(cellWidth, cellHeight);
    }
    
    /**
     * Tạo các vật phẩm có thể thu thập trong mê cung
     */
    private void generateCollectibles() {
        // Xóa tất cả collectibles hiện tại
        collectibles.clear();
        
        // Tính số lượng collectibles (tối thiểu 3, hoặc size/3)
        int numCollectibles = Math.max(3, size / 3);

        // Tạo từng collectible
        for (int i = 0; i < numCollectibles; i++) {
            int r, c;
            int attempts = 0;
            
            // Tìm vị trí hợp lệ cho collectible
            do {
                // Tạo vị trí ngẫu nhiên
                r = (int) (Math.random() * maze.getRows());
                c = (int) (Math.random() * maze.getCols());
                attempts++;
                
                // Tránh vòng lặp vô hạn
                if (attempts > 100) {
                    break;
                }
            } while ((r == maze.getStartRow() && c == maze.getStartCol())    // Không đặt ở điểm bắt đầu
                    || (r == maze.getExitRow() && c == maze.getExitCol())     // Không đặt ở điểm kết thúc
                    || !isWalkablePosition(r, c)                             // Phải ở vị trí có thể đi được
                    || isCollectibleAt(r, c));                               // Không đặt trùng với collectible khác

            // Kiểm tra lại điều kiện và tạo collectible
            if (isWalkablePosition(r, c)
                    && !(r == maze.getStartRow() && c == maze.getStartCol())
                    && !(r == maze.getExitRow() && c == maze.getExitCol())
                    && !isCollectibleAt(r, c)) {
                try {
                    // Load hình ảnh collectible
                    ClassLoader classLoader = getClass().getClassLoader();
                    Image collectibleImg = new ImageIcon(classLoader.getResource("images/coin.png")).getImage();
                    // Tạo collectible mới với 5 điểm
                    collectibles.add(new Collectible(r, c, collectibleImg, 5));
                } catch (Exception e) {
                    System.err.println("Không thể tải hình ảnh collectible: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Kiểm tra xem có collectible tại vị trí (row, col) không
     * @param row hàng cần kiểm tra
     * @param col cột cần kiểm tra
     * @return true nếu có collectible, false nếu không
     */
    private boolean isCollectibleAt(int row, int col) {
        // Duyệt qua tất cả collectibles
        for (Collectible collectible : collectibles) {
            // Nếu có collectible tại vị trí này
            if (collectible.isAt(row, col)) return true;
        }
        return false;
    }
    
    /**
     * Kiểm tra xem vị trí có thể đi được không (không phải tường)
     * @param row hàng cần kiểm tra
     * @param col cột cần kiểm tra
     * @return true nếu có thể đi được, false nếu không
     */
    private boolean isWalkablePosition(int row, int col) {
        // Kiểm tra maze có tồn tại không
        if (maze == null
                || row < 0 || row >= maze.getRows()    // Kiểm tra hàng hợp lệ
                || col < 0 || col >= maze.getCols()) { // Kiểm tra cột hợp lệ
            return false;
        }
        
        // Lấy grid của maze
        MazeGenerator.Cell[][] grid = maze.getGrid();
        
        // Trả về true nếu ô có giá trị 1 (đường đi), false nếu 0 (tường)
        return grid[row][col].getValue() == 1;
    }
    
    /**
     * Bắt đầu timer đếm ngược thời gian
     */
    private void startGameTimer() {
        // Tạo timer chạy mỗi 1000ms (1 giây)
        gameTimer = new Timer(1000, e -> {
            // Giảm thời gian còn lại
            timeRemaining--;
            // Cập nhật giao diện
            updateGameInfo();
            
            // Kiểm tra hết thời gian
            if (timeRemaining <= 0) {
                gameTimer.stop();    // Dừng timer
                gameOver(false);     // Kết thúc game (thua)
            }
        });
        // Bắt đầu timer
        gameTimer.start();
    }
    
    /**
     * Cập nhật thông tin game trên giao diện (thời gian, điểm số)
     */
    private void updateGameInfo() {
        // Cập nhật thanh progress bar thời gian
        timeProgressBar.setValue(timeRemaining);
        
        // Chuyển đổi giây thành phút:giây
        int phut = timeRemaining / 60;
        int giay = timeRemaining % 60;
        String tGian = String.format("%dm%02ds", phut, giay);
        
        // Hiển thị thời gian trên progress bar
        timeProgressBar.setString(tGian);
        
        // Cập nhật điểm số
        lblScore.setText("ĐIỂM: " + score);
    }
    
    /**
     * Xử lý khi game kết thúc
     * @param won true nếu thắng, false nếu thua
     */
    private void gameOver(boolean won) {
        // Đặt trạng thái game
        gameStarted = false;
        gameWon = won;

        // Dừng timer nếu đang chạy
        if (gameTimer != null && gameTimer.isRunning()) gameTimer.stop();

        String message;
        if (won) {
            // Xử lý khi thắng
            win++;                        // Tăng số lần thắng
            total += score;               // Cộng vào tổng điểm sau mỗi màn chơi
            checkDifficultyProgression(); // Kiểm tra số lần thắng = 5, giảm 10s
            
            // Tạo thông báo thắng
            message = "CHÚC MỪNG BẠN ĐÃ THẮNG!\n" + "TỔNG ĐIỂM: " + total + "\n" + "SỐ MÀN CHƠI THẮNG: " + win;
            
            // Tạo mê cung mới cho màn tiếp theo
            try {
                playerRow = 0;
                playerCol = 0;
                // Tạo maze mới với cùng kích thước
                maze = new MazeGenerator(size, size, wallImgPath, floorImgPath, startImgPath, exitImgPath);
                drawPanel.setPreferredSize(new Dimension(size * cellSize, size * cellSize));
                mazePanel.revalidate();
                mazePanel.repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "LỖI TẢI ẢNH", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Reset trạng thái cho màn chơi mới
            hintUsed = false;
            pathFinder = null;
        } else {
            // Tạo thông báo thua
            message = "GAME OVER!\n" + "HẾT THỜI GIAN RỒI!\n" 
                    + "TỔNG ĐIỂM ĐẠT ĐƯỢC: " + total;
        }

        // Hiển thị thông báo kết quả
        JOptionPane.showMessageDialog(this, message, won ? "THẮNG!" : "THUA!",
                won ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        // Kích hoạt lại nút bắt đầu
        btnStart.setEnabled(true);
    }
    
    /**
     * Kiểm tra và tăng độ khó sau mỗi 5 lần thắng
     */
    private void checkDifficultyProgression() {
        // Mỗi 5 lần thắng, giảm thời gian đi 10 giây (tối thiểu 10 giây)
        if (win % 5 == 0 && win > 0) {
            initialTime = Math.max(10, initialTime - 10);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameStarted || player == null || gameWon) return;
        
        String direction = null;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                direction = "up";
                break;
            case KeyEvent.VK_DOWN:
                direction = "down";
                break;
            case KeyEvent.VK_LEFT:
                direction = "left";
                break;
            case KeyEvent.VK_RIGHT:
                direction = "right";
                break;
        }
        
        if (direction != null) {
            movePlayer(direction);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    /**
     * Di chuyển người chơi theo hướng được chỉ định
     * @param direction hướng di chuyển ("up", "down", "left", "right")
     */
    private void movePlayer(String direction) {
        // Lấy vị trí hiện tại của player
        int newRow = player.getRow();
        int newCol = player.getCol();
        
        // Tính toán vị trí mới dựa trên hướng di chuyển
        switch (direction) {
            case "up":
                newRow--;
                break;
            case "down":
                newRow++;
                break;
            case "left":
                newCol--;
                break;
            case "right":
                newCol++;
                break;
        }
        
        // Kiểm tra và thực hiện di chuyển nếu hợp lệ
        if (isValidPlayerMove(newRow, newCol)) {
            // Di chuyển player
            player.move(direction, maze.getRows(), maze.getCols());
            
            // Kiểm tra thu thập vật phẩm
            checkCollectibles();
            
            // Kiểm tra đã đến điểm kết thúc chưa
            if (player.getRow() == maze.getExitRow() && player.getCol() == maze.getExitCol()) {
                score += 50;      // Cộng 50 điểm khi hoàn thành
                gameOver(true);   // Kết thúc game (thắng)
            }
            
            // Cập nhật giao diện
            updateGameInfo();
            drawPanel.repaint();
        }
    }
    
    /**
     * Kiểm tra xem player có thể di chuyển đến vị trí mới không
     * @param row hàng đích
     * @param col cột đích
     * @return true nếu có thể di chuyển, false nếu không
     */
    private boolean isValidPlayerMove(int row, int col) {
        // Sử dụng method isWalkablePosition để kiểm tra
        return isWalkablePosition(row, col);
    }
    
    private void checkCollectibles() {
        // Duyệt qua tất cả collectibles
        for (Collectible collectible : collectibles) {
            // Nếu collectible ở cùng vị trí với player
            if (collectible.isAt(player.getRow(), player.getCol())) {
                // Thu thập vật phẩm
                collectible.collect();
                // Lấy điểm và cộng vào score
                int point = collectible.getPoints();
                score += point;
                break; // Chỉ thu thập một vật phẩm mỗi lần
            }
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mazePanel = new javax.swing.JPanel();
        drawPanel = new javax.swing.JPanel();
        controlPanel = new javax.swing.JPanel();
        btnReset = new javax.swing.JButton();
        btnStart = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        btnHint = new javax.swing.JButton();
        lblTimer = new javax.swing.JLabel();
        timeProgressBar = new javax.swing.JProgressBar();
        lblScore = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Tìm đường về nhà");
        setAutoRequestFocus(false);
        setBackground(new java.awt.Color(16, 35, 63));
        setMinimumSize(new java.awt.Dimension(1400, 960));
        setSize(new java.awt.Dimension(1400, 960));

        mazePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 5));
        mazePanel.setForeground(new java.awt.Color(16, 35, 63));

        javax.swing.GroupLayout drawPanelLayout = new javax.swing.GroupLayout(drawPanel);
        drawPanel.setLayout(drawPanelLayout);
        drawPanelLayout.setHorizontalGroup(
            drawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 290, Short.MAX_VALUE)
        );
        drawPanelLayout.setVerticalGroup(
            drawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout mazePanelLayout = new javax.swing.GroupLayout(mazePanel);
        mazePanel.setLayout(mazePanelLayout);
        mazePanelLayout.setHorizontalGroup(
            mazePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mazePanelLayout.createSequentialGroup()
                .addComponent(drawPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(404, Short.MAX_VALUE))
        );
        mazePanelLayout.setVerticalGroup(
            mazePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(drawPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        controlPanel.setBackground(new java.awt.Color(16, 35, 63));
        controlPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 5));

        btnReset.setBackground(new java.awt.Color(143, 156, 172));
        btnReset.setFont(new java.awt.Font("SVN-Determination Sans", 0, 26)); // NOI18N
        btnReset.setForeground(new java.awt.Color(255, 255, 255));
        btnReset.setText("CHƠI LẠI");
        btnReset.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 5));
        btnReset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        btnStart.setBackground(new java.awt.Color(143, 156, 172));
        btnStart.setFont(new java.awt.Font("SVN-Determination Sans", 0, 26)); // NOI18N
        btnStart.setForeground(new java.awt.Color(255, 255, 255));
        btnStart.setText("BẮT ĐẦU");
        btnStart.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 5));
        btnStart.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnExit.setBackground(new java.awt.Color(143, 156, 172));
        btnExit.setFont(new java.awt.Font("SVN-Determination Sans", 0, 26)); // NOI18N
        btnExit.setForeground(new java.awt.Color(255, 255, 255));
        btnExit.setText("THOÁT");
        btnExit.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 5));
        btnExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        btnHint.setBackground(new java.awt.Color(143, 156, 172));
        btnHint.setFont(new java.awt.Font("SVN-Determination Sans", 0, 26)); // NOI18N
        btnHint.setForeground(new java.awt.Color(255, 255, 255));
        btnHint.setText("GỢI Ý");
        btnHint.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 5));
        btnHint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnHint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHintActionPerformed(evt);
            }
        });

        lblTimer.setFont(new java.awt.Font("SVN-Determination Sans", 0, 32)); // NOI18N
        lblTimer.setForeground(new java.awt.Color(237, 189, 42));
        lblTimer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTimer.setText("THỜI GIAN");

        timeProgressBar.setBackground(new java.awt.Color(254, 108, 0));
        timeProgressBar.setFont(new java.awt.Font("SVN-Determination Sans", 0, 20)); // NOI18N
        timeProgressBar.setMaximum(300);
        timeProgressBar.setValue(300);
        timeProgressBar.setString("");
        timeProgressBar.setStringPainted(true);

        lblScore.setFont(new java.awt.Font("SVN-Determination Sans", 0, 30)); // NOI18N
        lblScore.setForeground(new java.awt.Color(237, 189, 42));
        lblScore.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblScore.setText("ĐIỂM:");

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblScore, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(timeProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                            .addComponent(btnReset, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnHint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTimer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTimer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblScore, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 166, Short.MAX_VALUE)
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(btnHint, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58)
                .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(mazePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mazePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnHintActionPerformed(java.awt.event.ActionEvent evt) {
        // Kiểm tra game đã bắt đầu chưa
        if (!gameStarted) {
            JOptionPane.showMessageDialog(this, "VUI LÒNG BẮT ĐẦU GAME TRƯỚC!", "THÔNG BÁO", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kiểm tra đã sử dụng gợi ý chưa
        if (hintUsed) {
            JOptionPane.showMessageDialog(this, "BẠN ĐÃ SỬ DỤNG GỢI Ý!", "THÔNG BÁO", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Đánh dấu đã sử dụng gợi ý và trừ điểm
        hintUsed = true;
        score = Math.max(0, score - 10); // Trừ 10 điểm, không để âm

        // Tạo PathFinder để tìm đường từ vị trí hiện tại đến exit
        pathFinder = new PathFinder(maze, player.getRow(), player.getCol(), maze.getExitRow(), maze.getExitCol());
        List<PathFinder.Node> path = pathFinder.findPath();
        
        // Kiểm tra có tìm thấy đường đi không
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "KHÔNG TÌM THẤY ĐƯỜNG ĐI!", "THÔNG BÁO", JOptionPane.ERROR_MESSAGE);
        }

        // Cập nhật giao diện và lấy lại focus
        updateGameInfo();
        drawPanel.repaint();
        this.requestFocus();
    }                                       

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        int choice = JOptionPane.showConfirmDialog(
            this,
            "BẠN MUỐN THOÁT KHỎI TRÒ CHƠI","XÁC NHẬN",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            this.setVisible(false);
            new Home().setVisible(true);
        }
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {
        // Tạo maze mới nếu chưa có
        if (maze == null) {
            try {
                playerRow = 0;
                playerCol = 0;
                // Tạo maze với kích thước và hình ảnh đã định
                maze = new MazeGenerator(size, size, wallImgPath, floorImgPath, startImgPath, exitImgPath);
                drawPanel.setPreferredSize(new Dimension(size * cellSize, size * cellSize));
                mazePanel.revalidate();
                mazePanel.repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "LỖI TẢI ẢNH", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Kiểm tra game đã bắt đầu chưa
        if (gameStarted) {
            JOptionPane.showMessageDialog(this, "GAME ĐÃ ĐƯỢC BẮT ĐẦU!", "THÔNG BÁO", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Khởi tạo trạng thái game mới
        gameStarted = true;
        gameWon = false;
        score = 0;
        timeRemaining = initialTime;
        hintUsed = false;
        
        // Tạo player mới tại vị trí bắt đầu của maze
        player = new Player(maze.getStartRow(), maze.getStartCol());
        
        // Tạo PathFinder mới
        pathFinder = new PathFinder(maze);
        
        // Tạo các vật phẩm có thể thu thập
        generateCollectibles();
        
        // Bắt đầu timer đếm ngược
        startGameTimer();

        // Vô hiệu hóa nút bắt đầu
        btnStart.setEnabled(false);
        
        // Cập nhật giao diện
        updateGameInfo();
        drawPanel.repaint();
        this.requestFocus();

        // Hiển thị hướng dẫn chơi
        JOptionPane.showMessageDialog(
            this,
            "GAME BẮT ĐẦU!\nSỬ DỤNG CÁC PHÍM MŨI TÊN ĐỂ DI CHUYỂN\n" +
            "TÌM ĐƯỜNG ĐẾN CỬA RA TRONG THỜI GIAN QUY ĐỊNH!",
            "",
            JOptionPane.INFORMATION_MESSAGE
        );
    }                                        

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {
        // Hiển thị dialog xác nhận reset
        int choice = JOptionPane.showConfirmDialog(
            this,
            "BẠN CÓ MUỐN CHƠI LẠI GAME KHÔNG?",
            "",
            JOptionPane.YES_NO_OPTION
        );

        // Nếu chọn Yes, reset toàn bộ game
        if (choice == JOptionPane.YES_OPTION) {
            // Dừng timer nếu đang chạy
            if (gameTimer != null && gameTimer.isRunning()) {
                gameTimer.stop();
            }

            // Reset tất cả trạng thái về ban đầu
            gameStarted = false;
            gameWon = false;
            score = 0;
            timeRemaining = initialTime;
            hintUsed = false;
            player = null;
            pathFinder = null;
            collectibles.clear();

            // Kích hoạt lại nút bắt đầu
            btnStart.setEnabled(true);

            // Cập nhật giao diện
            updateGameInfo();
            drawPanel.repaint();

            // Thông báo đã reset
            JOptionPane.showMessageDialog(this, "GAME ĐÃ ĐƯỢC RESET!", "THÔNG BÁO", JOptionPane.INFORMATION_MESSAGE);
        }
    }                                        
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GameJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GameJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GameJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GameJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GameJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnHint;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnStart;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JPanel drawPanel;
    private javax.swing.JLabel lblScore;
    private javax.swing.JLabel lblTimer;
    private javax.swing.JPanel mazePanel;
    private javax.swing.JProgressBar timeProgressBar;
    // End of variables declaration//GEN-END:variables

}