import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class tictactoe2 extends JFrame {

    // --- COLOR PALETTE ---
    private static final Color BG_DARK_PINK   = new Color(248, 200, 220); // Main Background
    private static final Color CELL_PINK      = new Color(255, 230, 240); // Grid Tile Default
    private static final Color CELL_HOVER     = new Color(255, 215, 230); // Grid Tile Hover
    private static final Color HEADER_PINK    = new Color(216, 112, 147); // Dark Pink Text/Border
    private static final Color ACCENT_ROSE    = new Color(199, 21, 133);  // Deep Rose Pink
    
    // Fixed Colors for X (Red) and O (Green)
    private static final Color TEXT_X_COLOR   = new Color(220, 50, 50);   // Crimson Red
    private static final Color TEXT_O_COLOR   = new Color(34, 139, 34);   // Forest Green

    // Game Logic Variables
    private char[][] board = new char[3][3];
    private boolean isPlayerXTurn = true;
    private boolean gameOver = false;

    private int gameMode = 1; // 1: Player vs Player, 2: Player vs Bot
    private int difficulty = 1; // 1: Easy, 2: Medium, 3: Hard

    private String player1Name = "Player 1";
    private String player2Name = "Player 2";
    private int p1Wins = 0;
    private int p2Wins = 0;
    private int draws = 0;

    // Swing Components
    private final JButton[][] gridButtons = new JButton[3][3];
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JRadioButton pvpRadio;
    private JRadioButton botRadio;
    private JComboBox<String> diffDropdown;
    private JTextField p1NameField;
    private JTextField p2NameField;
    private ConfettiPanel confettiPanel;

    private Random random = new Random();

    public tictactoe2() {
        setTitle("Tic-Tac-Toe Pink Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(520, 720));
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
        resetGame();
        pack();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_DARK_PINK);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- TOP PANEL: Settings & Status ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("🌸 TIC-TAC-TOE 🌸", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(ACCENT_ROSE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(10));

        // Mode Selection
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        modePanel.setOpaque(false);

        pvpRadio = new JRadioButton("Player vs Player", true);
        botRadio = new JRadioButton("Player vs Bot");
        pvpRadio.setOpaque(false);
        botRadio.setOpaque(false);
        pvpRadio.setForeground(ACCENT_ROSE);
        botRadio.setForeground(ACCENT_ROSE);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(pvpRadio);
        modeGroup.add(botRadio);

        modePanel.add(pvpRadio);
        modePanel.add(botRadio);

        // Difficulty Selection
        String[] diffs = {"Easy", "Medium", "Hard"};
        diffDropdown = new JComboBox<>(diffs);
        diffDropdown.setEnabled(false);
        diffDropdown.addActionListener(e -> difficulty = diffDropdown.getSelectedIndex() + 1);
        modePanel.add(new JLabel(" Difficulty:"));
        modePanel.add(diffDropdown);

        topPanel.add(modePanel);
        topPanel.add(Box.createVerticalStrut(8));

        // Player Name Fields
        JPanel namesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        namesPanel.setOpaque(false);

        p1NameField = new JTextField("Player 1", 8);
        p2NameField = new JTextField("Player 2", 8);
        addPlayerNameListener(p1NameField);
        addPlayerNameListener(p2NameField);

        namesPanel.add(new JLabel("P1 (X):"));
        namesPanel.add(p1NameField);
        namesPanel.add(new JLabel("P2 (O):"));
        namesPanel.add(p2NameField);

        JButton applyConfigBtn = new JButton("Apply / Reset Setup");
        applyConfigBtn.setBackground(CELL_PINK);
        applyConfigBtn.setForeground(ACCENT_ROSE);
        namesPanel.add(applyConfigBtn);

        topPanel.add(namesPanel);
        topPanel.add(Box.createVerticalStrut(10));

        // Scoreboard & Status Displays
        scoreLabel = new JLabel("", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        scoreLabel.setForeground(HEADER_PINK);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel("Player 1's Turn (X)", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusLabel.setForeground(ACCENT_ROSE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(scoreLabel);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(statusLabel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL: 3x3 Grid ---
        confettiPanel = new ConfettiPanel();
        confettiPanel.setOpaque(false);
        confettiPanel.setPreferredSize(new Dimension(380, 380));

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton btn = createGridButton(r, c);
                gridButtons[r][c] = btn;
                confettiPanel.add(btn);
            }
        }
        mainPanel.add(confettiPanel, BorderLayout.CENTER);

        // --- BOTTOM PANEL: Reset Action ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.setPreferredSize(new Dimension(0, 60));

        JButton resetBtn = new JButton("Play Next Match");
        resetBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        resetBtn.setBackground(HEADER_PINK);
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.addActionListener(e -> resetGame());

        bottomPanel.add(resetBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Action Listeners
        pvpRadio.addActionListener(e -> toggleBotOptions());
        botRadio.addActionListener(e -> toggleBotOptions());
        applyConfigBtn.addActionListener(e -> applyConfiguration());
    }

    @SuppressWarnings("Convert2Lambda")
    private JButton createGridButton(int r, int c) {
        JButton btn = new JButton("");
        btn.setFont(new Font("SansSerif", Font.BOLD, 48));
        btn.setBackground(CELL_PINK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(HEADER_PINK, 2));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @SuppressWarnings("override")
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled() && btn.getText().isEmpty()) {
                    btn.setBackground(CELL_HOVER);
                }
            }
            @SuppressWarnings("override")
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled() && btn.getText().isEmpty()) {
                    btn.setBackground(CELL_PINK);
                }
            }
        });

        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCellClick(r, c);
            }
        });
        return btn;
    }

    private void toggleBotOptions() {
        boolean isBot = botRadio.isSelected();
        gameMode = isBot ? 2 : 1;
        diffDropdown.setEnabled(isBot);
        p2NameField.setEnabled(!isBot);
        if (isBot) {
            p2NameField.setText("Bot");
            player2Name = "Bot";
        } else {
            p2NameField.setText("Player 2");
            player2Name = "Player 2";
        }
        updateScoreboard();
    }

    private void addPlayerNameListener(JTextField nameField) {
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                syncPlayerNames();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                syncPlayerNames();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                syncPlayerNames();
            }
        });
    }

    private void syncPlayerNames() {
        String firstName = p1NameField.getText().trim();
        String secondName = p2NameField.getText().trim();
        player1Name = firstName.isEmpty() ? "Player 1" : firstName;
        player2Name = secondName.isEmpty() ? "Player 2" : secondName;
        updateScoreboard();
    }

    private void applyConfiguration() {
        gameMode = pvpRadio.isSelected() ? 1 : 2;
        difficulty = diffDropdown.getSelectedIndex() + 1;

        player1Name = p1NameField.getText().trim().isEmpty() ? "Player 1" : p1NameField.getText().trim();
        player2Name = p2NameField.getText().trim().isEmpty() ? "Player 2" : p2NameField.getText().trim();

        p1Wins = 0;
        p2Wins = 0;
        draws = 0;

        resetGame();
    }

    private void resetGame() {
        confettiPanel.stopAnimation();
        board = new char[3][3];
        isPlayerXTurn = true;
        gameOver = false;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                gridButtons[r][c].setText("");
                gridButtons[r][c].setEnabled(true);
                gridButtons[r][c].setBackground(CELL_PINK);
            }
        }

        updateScoreboard();
        statusLabel.setText(player1Name + "'s Turn (X)");
    }

    private void updateScoreboard() {
        scoreLabel.setText(String.format("%s: %d  |  %s: %d  |  Draws: %d",
                player1Name, p1Wins, player2Name, p2Wins, draws));
    }

    private void handleCellClick(int r, int c) {
        if (gameOver || board[r][c] != '\0') return;

        makeMove(r, c, isPlayerXTurn ? 'X' : 'O');

        if (checkEndCondition()) return;

        if (gameMode == 1) {
            isPlayerXTurn = !isPlayerXTurn;
            String currentName = isPlayerXTurn ? player1Name : player2Name;
            char currentSymbol = isPlayerXTurn ? 'X' : 'O';
            statusLabel.setText(currentName + "'s Turn (" + currentSymbol + ")");
        } else {
            isPlayerXTurn = false;
            statusLabel.setText(player2Name + " is thinking...");

            Timer botDelay = new Timer(300, e -> {
                executeBotMove();
                if (!checkEndCondition()) {
                    isPlayerXTurn = true;
                    statusLabel.setText(player1Name + "'s Turn (X)");
                }
            });
            botDelay.setRepeats(false);
            botDelay.start();
        }
    }

    private void makeMove(int r, int c, char symbol) {
        board[r][c] = symbol;
        gridButtons[r][c].setText(String.valueOf(symbol));
        // Ensures X stays red and O stays green
        gridButtons[r][c].setForeground(symbol == 'X' ? TEXT_X_COLOR : TEXT_O_COLOR);
    }

    private boolean checkEndCondition() {
        char winner = getWinner();
        if (winner != '\0') {
            gameOver = true;
            String winnerName = (winner == 'X') ? player1Name : player2Name;
            if (winner == 'X') p1Wins++; else p2Wins++;

            statusLabel.setText("🎉 " + winnerName + " Wins!");
            updateScoreboard();
            disableAllCells();
            confettiPanel.startAnimation();
            return true;
        }

        if (isBoardFull()) {
            gameOver = true;
            draws++;
            statusLabel.setText("🤝 It's a Draw!");
            updateScoreboard();
            disableAllCells();
            return true;
        }

        return false;
    }

    private void disableAllCells() {
        UIManager.put("Button.disabledText", null); // Retain original foreground colors when buttons disable
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                gridButtons[r][c].setEnabled(false);
                if (board[r][c] == 'X') {
                    gridButtons[r][c].setForeground(TEXT_X_COLOR);
                } else if (board[r][c] == 'O') {
                    gridButtons[r][c].setForeground(TEXT_O_COLOR);
                }
            }
        }
    }

    // --- BOT AI DIFFICULTIES ---

    private void executeBotMove() {
        switch (difficulty) {
            case 1 -> makeRandomBotMove();
            case 2 -> makeMediumBotMove();
            default -> makeHardBotMove();
        }
    }

    private void makeRandomBotMove() {
        List<int[]> emptyCells = getEmptyCells();
        if (!emptyCells.isEmpty()) {
            int[] choice = emptyCells.get(random.nextInt(emptyCells.size()));
            makeMove(choice[0], choice[1], 'O');
        }
    }

    private void makeMediumBotMove() {
        int[] move = findWinningCell('O');
        if (move != null) {
            makeMove(move[0], move[1], 'O');
            return;
        }
        move = findWinningCell('X');
        if (move != null) {
            makeMove(move[0], move[1], 'O');
            return;
        }
        makeRandomBotMove();
    }

    private int[] findWinningCell(char symbol) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == '\0') {
                    board[r][c] = symbol;
                    boolean wins = (getWinner() == symbol);
                    board[r][c] = '\0';
                    if (wins) return new int[]{r, c};
                }
            }
        }
        return null;
    }

    private void makeHardBotMove() {
        int bestScore = Integer.MIN_VALUE;
        int bestRow = -1;
        int bestCol = -1;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == '\0') {
                    board[r][c] = 'O';
                    int score = minimax(board, 0, false);
                    board[r][c] = '\0';

                    if (score > bestScore) {
                        bestScore = score;
                        bestRow = r;
                        bestCol = c;
                    }
                }
            }
        }
        if (bestRow != -1 && bestCol != -1) {
            makeMove(bestRow, bestCol, 'O');
        }
    }

    private int minimax(char[][] currentBoard, int depth, boolean isMaximizing) {
        char winner = getWinner();
        if (winner == 'O') return 10 - depth;
        if (winner == 'X') return depth - 10;
        if (isBoardFull()) return 0;

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (currentBoard[r][c] == '\0') {
                        currentBoard[r][c] = 'O';
                        int score = minimax(currentBoard, depth + 1, false);
                        currentBoard[r][c] = '\0';
                        bestScore = Math.max(score, bestScore);
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (currentBoard[r][c] == '\0') {
                        currentBoard[r][c] = 'X';
                        int score = minimax(currentBoard, depth + 1, true);
                        currentBoard[r][c] = '\0';
                        bestScore = Math.min(score, bestScore);
                    }
                }
            }
            return bestScore;
        }
    }

    // --- BOARD STATE CHECKS ---

    private List<int[]> getEmptyCells() {
        List<int[]> list = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == '\0') list.add(new int[]{r, c});
            }
        }
        return list;
    }

    private boolean isBoardFull() {
        return getEmptyCells().isEmpty();
    }

    private char getWinner() {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != '\0' && board[i][0] == board[i][1] && board[i][1] == board[i][2])
                return board[i][0];
            if (board[0][i] != '\0' && board[0][i] == board[1][i] && board[1][i] == board[2][i])
                return board[0][i];
        }
        if (board[0][0] != '\0' && board[0][0] == board[1][1] && board[1][1] == board[2][2])
            return board[0][0];
        if (board[0][2] != '\0' && board[0][2] == board[1][1] && board[1][1] == board[2][0])
            return board[0][2];

        return '\0';
    }

    private final class ConfettiPanel extends JPanel {
        private final List<ConfettiPiece> pieces = new ArrayList<>();
        private final Color[] colors = {
            new Color(255, 75, 110), new Color(255, 209, 102),
            new Color(75, 192, 150), new Color(100, 181, 246), ACCENT_ROSE
        };
        private final Timer animationTimer = new Timer(25, e -> advanceAnimation());

        private ConfettiPanel() {
            super(new GridLayout(3, 3, 8, 8));
        }

        private void startAnimation() {
            pieces.clear();
            int width = Math.max(1, getWidth());
            int height = Math.max(1, getHeight());
            for (int i = 0; i < 90; i++) {
                pieces.add(new ConfettiPiece(
                        random.nextInt(width),
                        -random.nextInt(height),
                        -1.5f + random.nextFloat() * 3f,
                        1.5f + random.nextFloat() * 2.5f,
                        random.nextInt(7) + 5,
                        colors[random.nextInt(colors.length)],
                        random.nextFloat() * (float) Math.PI,
                        -0.12f + random.nextFloat() * 0.24f));
            }
            animationTimer.restart();
            repaint();
        }

        private void stopAnimation() {
            animationTimer.stop();
            pieces.clear();
            repaint();
        }

        private void advanceAnimation() {
            Iterator<ConfettiPiece> iterator = pieces.iterator();
            while (iterator.hasNext()) {
                ConfettiPiece piece = iterator.next();
                piece.velocityY += 0.08f;
                piece.x += piece.velocityX;
                piece.y += piece.velocityY;
                piece.rotation += piece.rotationSpeed;
                if (piece.y > getHeight() || piece.x < -20 || piece.x > getWidth() + 20) {
                    iterator.remove();
                }
            }
            repaint();
            if (pieces.isEmpty()) {
                animationTimer.stop();
            }
        }

        @Override
        protected void paintChildren(Graphics graphics) {
            super.paintChildren(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (ConfettiPiece piece : pieces) {
                    AffineTransform originalTransform = g2.getTransform();
                    g2.rotate(piece.rotation, piece.x + piece.size / 2.0, piece.y + piece.size / 2.0);
                    g2.setColor(piece.color);
                    g2.fillRect(Math.round(piece.x), Math.round(piece.y), piece.size, Math.max(3, piece.size / 2));
                    g2.setTransform(originalTransform);
                }
            } finally {
                g2.dispose();
            }
        }
    }

    private static class ConfettiPiece {
        private float x;
        private float y;
        private float velocityX;
        private float velocityY;
        private final int size;
        private final Color color;
        private float rotation;
        private final float rotationSpeed;

        private ConfettiPiece(float x, float y, float velocityX, float velocityY,
                int size, Color color, float rotation, float rotationSpeed) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.size = size;
            this.color = color;
            this.rotation = rotation;
            this.rotationSpeed = rotationSpeed;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new tictactoe2().setVisible(true));
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }
}