package org.example.logistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class TicTacToe extends JFrame {
    private final JButton[][] board = new JButton[3][3];
    private final JLabel statusLabel = new JLabel("Turn: X");
    private final JLabel scoreLabel = new JLabel("X: 0 | O: 0 | Draws: 0");
    private final JRadioButton pvcRadio = new JRadioButton("PvC");
    private final JButton themeButton = new JButton("Switch Theme");

    private char currentPlayer = 'X';
    private boolean gameOver = false;
    private int xWins = 0, oWins = 0, draws = 0;
    private boolean darkMode = false;

    public TicTacToe() {
        super("Tic Tac Toe (Swing) — Minimax AI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(2, 1));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 18f));
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.PLAIN, 16f));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        top.add(statusLabel);
        top.add(scoreLabel);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(3, 3, 8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        Font f = new Font(Font.SANS_SERIF, Font.BOLD, 56);
        Dimension cellSize = new Dimension(150, 150);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton b = new JButton("");
                b.setFont(f);
                b.setFocusPainted(false);
                b.setPreferredSize(cellSize);
                int rr = r, cc = c;
                b.addActionListener((ActionEvent e) -> handlePlayerMove(rr, cc));
                board[r][c] = b;
                center.add(b);
            }
        }
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        ButtonGroup modeGroup = new ButtonGroup();
        JRadioButton pvpRadio = new JRadioButton("PvP", true);
        modeGroup.add(pvpRadio);
        modeGroup.add(pvcRadio);
        bottom.add(new JLabel("Mode:"));
        bottom.add(pvpRadio);
        bottom.add(pvcRadio);
        JButton resetButton = new JButton("Reset Board");
        bottom.add(resetButton);
        JButton resetScoreButton = new JButton("Reset Scores");
        bottom.add(resetScoreButton);
        bottom.add(themeButton);

        resetButton.addActionListener(e -> resetBoard());
        resetScoreButton.addActionListener(e -> {
            xWins = oWins = draws = 0;
            updateScore();
        });
        themeButton.addActionListener(e -> toggleTheme());

        add(bottom, BorderLayout.SOUTH);

        applyTheme();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void handlePlayerMove(int r, int c) {
        if (gameOver) return;
        if (!board[r][c].getText().isEmpty()) return;

        board[r][c].setText(String.valueOf(currentPlayer));
        if (checkWin(currentPlayer)) {
            gameOver = true;
            if (currentPlayer == 'X') xWins++;
            else oWins++;
            statusLabel.setText("Winner: " + currentPlayer);
            updateScore();
            return;
        }
        if (isDraw()) {
            gameOver = true;
            draws++;
            statusLabel.setText("Draw");
            updateScore();
            return;
        }
        togglePlayer();

        if (pvcRadio.isSelected() && currentPlayer == 'O') {
            computerMoveMinimax();
        }
    }

    private void computerMoveMinimax() {
        if (gameOver) return;
        char[][] state = readState();
        Move best = findBestMoveMinimax(state);

        if (best != null) {
            board[best.r()][best.c()].setText("O");
        }

        if (checkWin('O')) {
            gameOver = true;
            oWins++;
            statusLabel.setText("Winner: O");
            updateScore();
            return;
        }
        if (isDraw()) {
            gameOver = true;
            draws++;
            statusLabel.setText("Draw");
            updateScore();
            return;
        }
        togglePlayer();
        statusLabel.setText("Turn: " + currentPlayer);
    }

    private Move findBestMoveMinimax(char[][] state) {
        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;

        for (int[] cell : emptyCells(state)) {
            int r = cell[0], c = cell[1];
            state[r][c] = 'O';
            int score = minimax(state, false, 'O', 'X', 0);
            state[r][c] = '\0';

            if (score > bestScore) {
                bestScore = score;
                bestMove = new Move(r, c);
            }
        }
        return bestMove;
    }

    private int minimax(char[][] state, boolean isMaximizing, char me, char opp, int depth) {
        Character w = winner(state);
        if (w != null) {
            if (w == me) return 10 - depth;
            if (w == opp) return depth - 10;
        }
        if (isFull(state)) return 0;

        int best;
        if (isMaximizing) {
            best = Integer.MIN_VALUE;
            for (int[] cell : emptyCells(state)) {
                int r = cell[0], c = cell[1];
                state[r][c] = me;
                int score = minimax(state, false, me, opp, depth + 1);
                state[r][c] = '\0';
                best = Math.max(best, score);
            }
        } else {
            best = Integer.MAX_VALUE;
            for (int[] cell : emptyCells(state)) {
                int r = cell[0], c = cell[1];
                state[r][c] = opp;
                int score = minimax(state, true, me, opp, depth + 1);
                state[r][c] = '\0';
                best = Math.min(best, score);
            }
        }
        return best;
    }

    private char[][] readState() {
        char[][] s = new char[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                String t = board[r][c].getText();
                s[r][c] = t.isEmpty() ? '\0' : t.charAt(0);
            }
        }
        return s;
    }

    private List<int[]> emptyCells(char[][] s) {
        List<int[]> res = new ArrayList<>();
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (s[r][c] == '\0')
                    res.add(new int[]{r, c});
        return res;
    }

    private boolean isFull(char[][] s) {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (s[r][c] == '\0')
                    return false;
        return true;
    }

    private Character winner(char[][] s) {
        for (int r = 0; r < 3; r++)
            if (s[r][0] != '\0' && s[r][0] == s[r][1] && s[r][1] == s[r][2])
                return s[r][0];
        for (int c = 0; c < 3; c++)
            if (s[0][c] != '\0' && s[0][c] == s[1][c] && s[1][c] == s[2][c])
                return s[0][c];
        if (s[0][0] != '\0' && s[0][0] == s[1][1] && s[1][1] == s[2][2]) return s[0][0];
        if (s[0][2] != '\0' && s[0][2] == s[1][1] && s[1][1] == s[2][0]) return s[0][2];
        return null;
    }

    private boolean checkWin(char player) {
        String p = String.valueOf(player);
        for (int r = 0; r < 3; r++)
            if (eq(board[r][0], p) && eq(board[r][1], p) && eq(board[r][2], p)) return true;
        for (int c = 0; c < 3; c++)
            if (eq(board[0][c], p) && eq(board[1][c], p) && eq(board[2][c], p)) return true;
        return (eq(board[0][0], p) && eq(board[1][1], p) && eq(board[2][2], p))
                || (eq(board[0][2], p) && eq(board[1][1], p) && eq(board[2][0], p));
    }

    private boolean eq(JButton b, String s) {
        return s.equals(b.getText());
    }

    private boolean isDraw() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (board[r][c].getText().isEmpty())
                    return false;
        return true;
    }

    private void togglePlayer() {
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
        statusLabel.setText("Turn: " + currentPlayer);
    }

    private void resetBoard() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                board[r][c].setText("");
        currentPlayer = 'X';
        gameOver = false;
        statusLabel.setText("Turn: X");
    }

    private void updateScore() {
        scoreLabel.setText("X: " + xWins + " | O: " + oWins + " | Draws: " + draws);
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        applyTheme();
    }

    private void applyTheme() {
        Color bg, fg, cellBg;
        if (darkMode) {
            bg = new Color(32, 32, 36);
            fg = new Color(230, 230, 230);
            cellBg = new Color(48, 48, 54);
            themeButton.setText("Switch to Light");
        } else {
            bg = new Color(245, 245, 245);
            fg = new Color(20, 20, 20);
            cellBg = Color.WHITE;
            themeButton.setText("Switch to Dark");
        }

        getContentPane().setBackground(bg);
        statusLabel.setForeground(fg);
        scoreLabel.setForeground(fg);

        for (Component comp : getContentPane().getComponents()) {
            comp.setBackground(bg);
            if (comp instanceof JPanel panel) {
                for (Component child : panel.getComponents()) {
                    if (child instanceof JLabel lbl) {
                        lbl.setForeground(fg);
                    } else if (child instanceof JRadioButton rb) {
                        rb.setBackground(bg);
                        rb.setForeground(fg);
                    } else if (child instanceof JButton btn) {
                        btn.setBackground(darkMode ? new Color(64, 64, 72) : new Color(230, 230, 230));
                        btn.setForeground(fg);
                    }
                }
            }
        }

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton b = board[r][c];
                b.setBackground(cellBg);
                b.setForeground(fg);
                b.setBorder(BorderFactory
                        .createLineBorder(darkMode ? new Color(80, 80, 88)
                                : new Color(200, 200, 200), 2));
            }
        }
        repaint();
    }
}

