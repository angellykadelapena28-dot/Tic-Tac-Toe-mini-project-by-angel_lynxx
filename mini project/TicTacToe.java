import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class TicTacToe extends JFrame {
    private static final char EMPTY = ' ';
    private static final char PLAYER_X = 'X';
    private static final char PLAYER_O = 'O';

    private final Random random = new Random();
    private char[][] board = new char[3][3];
    private char currentSymbol = PLAYER_X;
    private boolean gameOver = false;
    private int gameMode = 1; // 1 = Player vs Player, 2 = Player vs Bot
    private int difficulty = 1; // 1 = Easy, 2 = Medium, 3 = Hard

    private String player1Name = "Player 1";
    private String player2Name = "Player 2";
    private int p1Wins = 0;
    private int p2Wins = 0;
    private int draws = 0;

    private final JButton[][] cells = new JButton[3][3];
    private final JLabel statusLabel = new JLabel("Choose your settings and start a match.");
    private final JLabel scoreLabel = new JLabel();
    private final JTextField player1Field = new JTextField("Player 1", 12);
    private final JTextField player2Field = new JTextField("Player 2", 12);
    private final JComboBox<String> modeCombo = new JComboBox<>(new String[]{"Player vs Player", "Player vs Bot"});
    private final JComboBox<String> difficultyCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
    private final JButton newGameButton = new JButton("Start Match");
    private final JButton resetScoresButton = new JButton("Reset Score");

    public TicTacToe() {
        super("Tic-Tac-Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel settingsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        settingsPanel.add(new JLabel("Player 1"));
        settingsPanel.add(player1Field);
        settingsPanel.add(new JLabel("Player 2"));
        settingsPanel.add(player2Field);

        JPanel modePanel = new JPanel(new GridLayout(1, 4, 10, 10));
        modePanel.add(new JLabel("Mode:"));
        modePanel.add(modeCombo);
        modePanel.add(new JLabel("Difficulty:"));
        modePanel.add(difficultyCombo);

        topPanel.add(settingsPanel);
        topPanel.add(modePanel);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controls.add(newGameButton);
        controls.add(resetScoresButton);

        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                JButton cell = new JButton();
                cell.setFont(new Font("Arial", Font.BOLD, 36));
                cell.setFocusPainted(false);
                final int r = row;
                final int c = col;
                cell.addActionListener(e -> handleMove(r, c));
                cells[row][col] = cell;
                boardPanel.add(cell);
            }
        }

        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoPanel.add(statusLabel, BorderLayout.NORTH);
        infoPanel.add(scoreLabel, BorderLayout.CENTER);

        newGameButton.addActionListener(e -> startNewMatch());
        resetScoresButton.addActionListener(e -> resetScores());
        modeCombo.addActionListener(e -> updateModeSettings());

        add(topPanel, BorderLayout.NORTH);
        add(controls, BorderLayout.CENTER);
        add(boardPanel, BorderLayout.WEST);
        add(infoPanel, BorderLayout.SOUTH);

        updateModeSettings();
        resetScores();
        initializeBoard();
        pack();
        setLocationRelativeTo(null);
    }

    private void updateModeSettings() {
        gameMode = modeCombo.getSelectedIndex() + 1;
        difficulty = difficultyCombo.getSelectedIndex() + 1;

        if (gameMode == 1) {
            player2Field.setText("Player 2");
            player2Field.setEnabled(true);
            player2Field.setEditable(true);
        } else {
            player2Field.setText("Bot");
            player2Field.setEnabled(false);
            player2Field.setEditable(false);
        }
        difficultyCombo.setEnabled(gameMode == 2);
    }

    private void startNewMatch() {
        player1Name = player1Field.getText().trim();
        player2Name = player2Field.getText().trim();
        if (player1Name.isEmpty()) player1Name = "Player 1";
        if (player2Name.isEmpty()) player2Name = (gameMode == 2) ? "Bot" : "Player 2";

        initializeBoard();
        currentSymbol = PLAYER_X;
        gameOver = false;
        statusLabel.setText(player1Name + "'s turn (X)");
        updateScoreboard();
        setBoardEnabled(true);
    }

    private void resetScores() {
        p1Wins = 0;
        p2Wins = 0;
        draws = 0;
        updateScoreboard();
    }

    private void initializeBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                board[row][col] = EMPTY;
                cells[row][col].setText("");
                cells[row][col].setEnabled(false);
                cells[row][col].setBackground(new JButton().getBackground());
            }
        }
    }

    private void setBoardEnabled(boolean enabled) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                cells[row][col].setEnabled(enabled && board[row][col] == EMPTY && !gameOver);
            }
        }
    }

    private void handleMove(int row, int col) {
        if (gameOver || board[row][col] != EMPTY) return;

        board[row][col] = currentSymbol;
        cells[row][col].setText(String.valueOf(currentSymbol));
        cells[row][col].setEnabled(false);

        if (checkWin(currentSymbol)) {
            gameOver = true;
            if (currentSymbol == PLAYER_X) {
                p1Wins++;
                statusLabel.setText(player1Name + " wins!");
            } else {
                p2Wins++;
                statusLabel.setText(player2Name + " wins!");
            }
            updateScoreboard();
            setBoardEnabled(false);
            return;
        }

        if (isBoardFull()) {
            gameOver = true;
            draws++;
            statusLabel.setText("It's a draw!");
            updateScoreboard();
            setBoardEnabled(false);
            return;
        }

        currentSymbol = (currentSymbol == PLAYER_X) ? PLAYER_O : PLAYER_X;
        if (gameMode == 2 && currentSymbol == PLAYER_O) {
            statusLabel.setText("Bot is thinking...");
            setBoardEnabled(false);
            @SuppressWarnings("Convert2Lambda")
            Timer timer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    makeBotMove();
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            statusLabel.setText(getCurrentPlayerName() + "'s turn (" + currentSymbol + ")");
            setBoardEnabled(true);
        }
    }

    private String getCurrentPlayerName() {
        return (currentSymbol == PLAYER_X) ? player1Name : player2Name;
    }

    private void makeBotMove() {
        if (gameOver) return;

        int[] move = chooseBotMove();
        if (move == null) return;

        int row = move[0];
        int col = move[1];
        board[row][col] = PLAYER_O;
        cells[row][col].setText("O");
        cells[row][col].setEnabled(false);

        if (checkWin(PLAYER_O)) {
            gameOver = true;
            p2Wins++;
            statusLabel.setText("Bot wins!");
            updateScoreboard();
            setBoardEnabled(false);
            return;
        }

        if (isBoardFull()) {
            gameOver = true;
            draws++;
            statusLabel.setText("It's a draw!");
            updateScoreboard();
            setBoardEnabled(false);
            return;
        }

        currentSymbol = PLAYER_X;
        statusLabel.setText(player1Name + "'s turn (X)");
        setBoardEnabled(true);
    }

    private int[] chooseBotMove() {
        if (difficulty == 1) return makeRandomMove();
        if (difficulty == 2) return makeMediumMove();
        return makeHardMove();
    }

    private int[] makeRandomMove() {
        int row;
        int col;
        do {
            row = random.nextInt(3);
            col = random.nextInt(3);
        } while (board[row][col] != EMPTY);
        return new int[]{row, col};
    }

    private int[] makeMediumMove() {
        if (findWinningMove(PLAYER_O) != null) return findWinningMove(PLAYER_O);
        if (findWinningMove(PLAYER_X) != null) return findWinningMove(PLAYER_X);
        return makeRandomMove();
    }

    private int[] findWinningMove(char symbol) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row][col] == EMPTY) {
                    board[row][col] = symbol;
                    boolean winning = checkWin(symbol);
                    board[row][col] = EMPTY;
                    if (winning) {
                        return new int[]{row, col};
                    }
                }
            }
        }
        return null;
    }

    private int[] makeHardMove() {
        int bestScore = Integer.MIN_VALUE;
        int bestRow = -1;
        int bestCol = -1;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row][col] == EMPTY) {
                    board[row][col] = PLAYER_O;
                    int score = minimax(board, 0, false);
                    board[row][col] = EMPTY;

                    if (score > bestScore) {
                        bestScore = score;
                        bestRow = row;
                        bestCol = col;
                    }
                }
            }
        }

        if (bestRow == -1 || bestCol == -1) {
            return makeRandomMove();
        }
        return new int[]{bestRow, bestCol};
    }

    private int minimax(char[][] currentBoard, int depth, boolean isMaximizing) {
        if (checkWinBoard(currentBoard, PLAYER_O)) return 10 - depth;
        if (checkWinBoard(currentBoard, PLAYER_X)) return depth - 10;
        if (isBoardFull(currentBoard)) return 0;

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (currentBoard[row][col] == EMPTY) {
                        currentBoard[row][col] = PLAYER_O;
                        int score = minimax(currentBoard, depth + 1, false);
                        currentBoard[row][col] = EMPTY;
                        bestScore = Math.max(score, bestScore);
                    }
                }
            }
            return bestScore;
        }

        int bestScore = Integer.MAX_VALUE;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (currentBoard[row][col] == EMPTY) {
                    currentBoard[row][col] = PLAYER_X;
                    int score = minimax(currentBoard, depth + 1, true);
                    currentBoard[row][col] = EMPTY;
                    bestScore = Math.min(score, bestScore);
                }
            }
        }
        return bestScore;
    }

    private boolean checkWin(char symbol) {
        return checkWinBoard(board, symbol);
    }

    /**
     * @param targetBoard
     * @param symbol
     * @return
     */
    private boolean checkWinBoard(char[][] targetBoard, char symbol) {
        for (int i = 0; i < 3; i++) {
            if (targetBoard[i][0] == symbol && targetBoard[i][1] == symbol && targetBoard[i][2] == symbol) return true;
            if (targetBoard[0][i] == symbol && targetBoard[1][i] == symbol && targetBoard[2][i] == symbol) return true;
        }
        if (targetBoard[0][0] == symbol && targetBoard[1][1] == symbol && targetBoard[2][2] == symbol) return true;
        return targetBoard[0][2] == symbol && targetBoard[1][1] == symbol && targetBoard[2][0] == symbol;
    }

    public TicTacToe(String title) throws HeadlessException {
        super(title);
    }

    private boolean isBoardFull() {
        return isBoardFull(board);
    }

    private boolean isBoardFull(char[][] targetBoard) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (targetBoard[row][col] == EMPTY) return false;
            }
        }
        return true;
    }

    private void updateScoreboard() {
        scoreLabel.setText(String.format("%s (X): %d    %s (O): %d    Draws: %d",
                player1Name, p1Wins, player2Name, p2Wins, draws));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TicTacToe game = new TicTacToe();
            game.setVisible(true);
        });
    }

    public char[][] getBoard() {
        return board;
    }

    public void setBoard(char[][] board) {
        this.board = board;
    }
}