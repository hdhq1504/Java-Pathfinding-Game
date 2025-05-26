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
 *
 * @author 11a5h
 */
public class GameJFrame extends javax.swing.JFrame implements KeyListener {
    private MazeGenerator maze;
    private Player player;
    private PathFinder pathFinder;
    private int size = 50;
    private int cellSize = 80;
    private int playerRow = 0;
    private int playerCol = 0;
    private int score = 0;
    private Timer gameTimer;
    private int initialTime = 300; // 5 phút
    private int timeRemaining = 300;
    private boolean hintUsed = false;
    private List<Collectible> collectibles = new ArrayList<>();
    private boolean gameStarted = false;
    private boolean gameWon = false;
    private int win = 0;
    
    private final String wallImgPath = "images/wall.png";
    private final String floorImgPath = "images/floor.png";
    private final String startImgPath = "";
    private final String exitImgPath = "images/DoorWin.png";
    private final int PANEL_WIDTH = 900;
    private final int PANEL_HEIGHT = 900;

    /**
     * Creates new form GameJFrame
     */
    public GameJFrame() { 
        initComponents();
        setUpMazePanel();
        this.setLocationRelativeTo(null);
        this.addKeyListener(this);
        this.setFocusable(true);
    }
    
    private void setUpMazePanel() {
        mazePanel.removeAll();
        
        calculateCellSize();
        
        drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (maze != null) {
                    Image[][] tiles = maze.getTileImages();
                    int rows = maze.getRows(), cols = maze.getCols();
                    
                    for (int r = 0; r < rows; r++) {
                        for (int c = 0; c < cols; c++) {
                            if (tiles[r][c] != null) {
                                g.drawImage(tiles[r][c], c * cellSize, r * cellSize, cellSize, cellSize, null);
                            }
                        }
                    }
                    
                    if (pathFinder != null) {
                        pathFinder.drawPathHighlights(g, cellSize);
                    }
                    
                    for (Collectible collectible : collectibles) {
                        collectible.draw(g, cellSize);
                    }
                    
                    if (gameStarted && player != null && !gameWon) {
                        try {
                            ClassLoader classLoader = getClass().getClassLoader();
                            Image playerImg = new ImageIcon(classLoader.getResource(player.getCurrentImagePath())).getImage();
                            g.drawImage(playerImg, player.getCol() * cellSize, player.getRow() * cellSize, cellSize, cellSize, null);
                        } catch (Exception e) {
                            System.err.println("Không thể tải hình ảnh player: " + e.getMessage());
                        }
                    }
                }
            }
        };
        
        drawPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        mazePanel.setLayout(new BorderLayout());
        mazePanel.add(drawPanel, BorderLayout.CENTER);
        mazePanel.revalidate();
        mazePanel.repaint();
    }
    
    private void calculateCellSize() {
         int cellWidth = PANEL_WIDTH / size;
         int cellHeight = PANEL_HEIGHT / size;
         cellSize = Math.min(cellWidth, cellHeight);
    }
    
    private void generateCollectibles() {
        collectibles.clear();
        int numCollectibles = Math.max(3, size / 3);

        for (int i = 0; i < numCollectibles; i++) {
            int r, c;
            int attempts = 0;
            do {
                r = (int) (Math.random() * maze.getRows());
                c = (int) (Math.random() * maze.getCols());
                attempts++;
                if (attempts > 100) {
                    break;
                }
            } while ((r == maze.getStartRow() && c == maze.getStartCol())
                    || (r == maze.getExitRow() && c == maze.getExitCol())
                    || !isWalkablePosition(r, c)
                    || isCollectibleAt(r, c));

            if (isWalkablePosition(r, c)
                    && !(r == maze.getStartRow() && c == maze.getStartCol())
                    && !(r == maze.getExitRow() && c == maze.getExitCol())
                    && !isCollectibleAt(r, c)) {
                try {
                    ClassLoader classLoader = getClass().getClassLoader();
                    Image collectibleImg = new ImageIcon(classLoader.getResource("images/coin.png")).getImage();
                    collectibles.add(new Collectible(r, c, collectibleImg, 5));
                } catch (Exception e) {
                    System.err.println("Không thể tải hình ảnh collectible: " + e.getMessage());
                }
            }
        }
    }
    
    private boolean isCollectibleAt(int row, int col) {
        for (Collectible collectible : collectibles) {
            if (collectible.isAt(row, col)) return true;
        }
        return false;
    }
    
    private boolean isWalkablePosition(int row, int col) {
        if (maze == null
                || row < 0 || row >= maze.getRows()
                || col < 0 || col >= maze.getCols()) {
            return false;
        }
        MazeGenerator.Cell[][] grid = maze.getGrid();
        
        return grid[row][col].getValue() == 1;
    }
    
    private void startGameTimer() {
        gameTimer = new Timer(1000, e -> {
            timeRemaining--;
            updateGameInfo();
            
            if (timeRemaining <= 0) {
                gameTimer.stop();
                gameOver(false);
            }
        });
        gameTimer.start();
    }
    
    private void updateGameInfo() {
        timeProgressBar.setValue(timeRemaining);
        int phut = timeRemaining / 60;
        int giay = timeRemaining % 60;
        String tGian = String.format("%dm%02ds", phut, giay);
        timeProgressBar.setString(tGian);
        lblScore.setText("ĐIỂM: " + score);
    }
    
    private void gameOver(boolean won) {
        gameStarted = false;
        gameWon = won;

        if (gameTimer != null && gameTimer.isRunning()) gameTimer.stop();

        String message;
        if (won) {
            win++;                        // Tăng số lần thắng
            checkDifficultyProgression(); // Kiểm tra số lần thắng = 5, giảm 10s
            
            message = "CHÚC MỪNG BẠN ĐÃ THẮNG!\n" + "ĐIỂM SỐ: " + score + "\n" + "TỔNG SỐ MÀN CHƠI THẮNG: " + win;
            
            // Sau khi hiển thị ẩn message, tạo mê cung mới
            try {
                playerRow = 0;
                playerCol = 0;
                maze = new MazeGenerator(size, size, wallImgPath, floorImgPath, startImgPath, exitImgPath);
                drawPanel.setPreferredSize(new Dimension(size * cellSize, size * cellSize));
                mazePanel.revalidate();
                mazePanel.repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "LỖI TẢI ẢNH", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Xóa highlight đường đi màn chơi trước
            hintUsed = false;
            pathFinder = null;
            
        } else {
            message = "GAME OVER!\n" + "HẾT THỜI GIAN RỒI!\n" 
                    + "ĐIỂM SỐ CUỐI: " + score;
        }

        JOptionPane.showMessageDialog(this, message, won ? "THẮNG!" : "THUA!",
                won ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        btnStart.setEnabled(true);
    }
    
    private void checkDifficultyProgression() {
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
    
    private void movePlayer(String direction) {
        int newRow = player.getRow();
        int newCol = player.getCol();
        
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
        
        if (isValidPlayerMove(newRow, newCol)) {
            player.move(direction, maze.getRows(), maze.getCols());
            
            checkCollectibles();
            
            if (player.getRow() == maze.getExitRow() && player.getCol() == maze.getExitCol()) {
                score += 50;
                gameOver(true);
            }
            
            updateGameInfo();
            drawPanel.repaint();
        }
    }
    
    private boolean isValidPlayerMove(int row, int col) {
        return isWalkablePosition(row, col);
    }
    
    private void checkCollectibles() {
        for (Collectible collectible : collectibles) {
            if (collectible.isAt(player.getRow(), player.getCol())) {
                collectible.collect();
                int point = collectible.getPoints();
                score += point;
                
                System.out.println("Thu thập đồng xu! Nhận được " + score + " điểm");
                break;
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
        btnReset.setText("RESET");
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

    private void btnHintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHintActionPerformed
        // TODO add your handling code here:
        if (!gameStarted) {
            JOptionPane.showMessageDialog(this, "VUI LÒNG BẮT ĐẦU GAME TRƯỚC!", "THÔNG BÁO", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (hintUsed) {
            JOptionPane.showMessageDialog(this, "BẠN ĐÃ SỬ DỤNG GỢI Ý!", "THÔNG BÁO", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (gameWon) {
            JOptionPane.showMessageDialog(this, "GAME KẾT THÚC!", "THÔNG BÁO", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        hintUsed = true;
        score = Math.max(0, score - 10);

        pathFinder = new PathFinder(maze, player.getRow(), player.getCol(), maze.getExitRow(), maze.getExitCol());
        List<PathFinder.Node> path = pathFinder.findPath();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "KHÔNG TÌM THẤY ĐƯỜNG ĐI!", "THÔNG BÁO", JOptionPane.ERROR_MESSAGE);
        }

        updateGameInfo();
        drawPanel.repaint();
        this.requestFocus();
    }//GEN-LAST:event_btnHintActionPerformed

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

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        // TODO add your handling code here:
        if (maze == null) {
            try {
                playerRow = 0;
                playerCol = 0;
                maze = new MazeGenerator(size, size, wallImgPath, floorImgPath, startImgPath, exitImgPath);
                drawPanel.setPreferredSize(new Dimension(size * cellSize, size * cellSize));
                mazePanel.revalidate();
                mazePanel.repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "LỖI TẢI ẢNH", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (gameStarted) {
            JOptionPane.showMessageDialog(this, "GAME ĐÃ ĐƯỢC BẮT ĐẦU!", "THÔNG BÁO", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        gameStarted = true;
        gameWon = false;
        score = 0;
        timeRemaining = initialTime;
        hintUsed = false;
        player = new Player(maze.getStartRow(), maze.getStartCol());
        pathFinder = new PathFinder(maze);
        generateCollectibles();
        startGameTimer();

        btnStart.setEnabled(false);
        updateGameInfo();
        drawPanel.repaint();
        this.requestFocus();

        JOptionPane.showMessageDialog(
            this,
            "GAME BẮT ĐẦU!\nSỬ DỤNG CÁC PHÍM MŨI TÊN ĐỂ DI CHUYỂN\n" +
            "TÌM ĐƯỜNG ĐẾN CỬA RA TRONG THỜI GIAN QUY ĐỊNH!",
            "",
            JOptionPane.INFORMATION_MESSAGE
        );
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
        int choice = JOptionPane.showConfirmDialog(
            this,
            "BẠN CÓ MUỐN RESET GAME KHÔNG?\nTẤT CẢ TIẾN TRÌNH SẼ BỊ MẤT!",
            "",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (gameTimer != null && gameTimer.isRunning()) {
                gameTimer.stop();
            }

            gameStarted = false;
            gameWon = false;
            score = 0;
            timeRemaining = initialTime;
            hintUsed = false;
            player = null;
            pathFinder = null;
            collectibles.clear();

            btnStart.setEnabled(true);

            updateGameInfo();
            drawPanel.repaint();

            JOptionPane.showMessageDialog(this, "GAME ĐÃ ĐƯỢC RESET!", "THÔNG BÁO", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnResetActionPerformed
    
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