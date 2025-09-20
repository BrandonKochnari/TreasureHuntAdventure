/* Written by - Brandon Kochnari
 * Friday, June 14, 2024
 * Program Title: Treasure Hunt Adventure
 * Description: Treasure Hunt Adventure is a grid-based game where players navigate a 10x10 grid to uncover hidden
 * treasures while avoiding obstacles. Using arrow keys, players aim to collect all treasures in the least time and
 * fewest moves. The game features a timer, high scores, background music, and a user-friendly interface with a main
 * menu offering play, rules, and quit options. Icons represent the player, treasures, and obstacles, ensuring an
 * engaging experience as players strive for the best time and move count.
 * Pseudocode:
 * Start
 * 1. Initialize Game
 *      a. Set up game grid (10x10)
 *      b. Initialize player position, moves, and treasures remaining
 *      c. Load high scores from file
 *      d. Load images for player, treasure, and obstacles
 *      e. Set up main menu
 * 2. Main Menu
 *      a. Display title
 *      b. Display options
 *          i. Play
 *              A. Initialize game
 *              B. Switch to playing mode
 *              C. Start game timer
 *          ii. Rules
 *              A. Display game rules
 *              B. Return to main menu
 *          iii. Quit
 *              A. Exit the game
 * 3. Game Loop
 *      a. Handle player movement
 *          i. Use arrow keys for movement
 *          ii. Check for boundaries and obstacles
 *          iii. Update player position
 *      b. Check for treasures
 *          i. If player finds a treasure, update treasures remaining
 *          ii. Pause and resume timer
 *      c. Update game status
 *          i. Update UI with player position, moves, and treasures remaining
 *          ii. Check if all treasures are found
 *              A. If yes, stop timer and display win message
 *              B. Record high score
 *              C. Ask if player wants to play again
 *                  1. If yes, reinitialize game
 *                  2. If no, exit the game
 * 4. Timer Management
 *      a. Start timer when game begins
 *      b. Update timer label every second
 *      c. Stop timer when game ends
 *      d. Pause and resume timer as needed
 * 5. High Score Management
 *      a. Load high scores from file
 *      b. Add new high score
 *          i. Sort high scores by time and moves
 *      c. Save high scores to file
 *      d. Display high scores
 * 6. UI Updates
 *      a. Load and display images for player, treasures, and obstacles
 *      b. Update grid cells based on game state
 *      c. Display status label with time, moves, and treasures remaining
 * End
 */


import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.sound.sampled.*;

public class TreasureHuntAdventure extends JFrame {
    // Game grid size and elements
    private final int gridSize = 10;
    private final int[][] grid = new int[gridSize][gridSize];
    private int playerX, playerY;
    private int moves;
    private int treasuresRemaining;
    private final List<HighScore> highScores = new ArrayList<>();
    private final JLabel[][] gridLabels = new JLabel[gridSize][gridSize];
    private final Random random = new Random();
    private Timer timer;
    private long startTime;
    private long elapsedTime;
    private boolean timerStarted = false;
    private final JLabel statusLabel = new JLabel("Time: 0s | Moves: 0 | Treasures remaining: 0");
    private ImageIcon playerIcon;
    private ImageIcon treasureIcon;
    private ImageIcon obstacleIcon;
    private long pausedTime;
    private enum GameState {
        MENU,
        RULES,
        PLAYING
    }

    private GameState currentState;


    // Method to play background music
    private void playMusic() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("src/ICS4U/FinalProject/assets/BG_Music.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Play continuously
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // Method to resize images
    private ImageIcon resizeImageIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image image = icon.getImage();
        Image resizedImage = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    // Method to load images for the game
    private void loadImages() {
        playerIcon = resizeImageIcon("src/ICS4U/FinalProject/assets/player.png", 70, 70);
        treasureIcon = resizeImageIcon("src/ICS4U/FinalProject/assets/treasure.png", 75, 75);
        obstacleIcon = resizeImageIcon("src/ICS4U/FinalProject/assets/obstacle.png", 95, 95);
    }

    // Constructor to set up the game
    public TreasureHuntAdventure() {
        setTitle("Treasure Hunt Adventure");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        loadHighScores(); // Load high scores at the beginning
        initializeMenu();
        playMusic();
        initializeGame();
        initializeUI();
        currentState = GameState.MENU;
        setVisible(true);
    }

    // Method for movement
    private final KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (currentState == GameState.PLAYING) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> movePlayer(-1, 0);
                    case KeyEvent.VK_DOWN -> movePlayer(1, 0);
                    case KeyEvent.VK_LEFT -> movePlayer(0, -1);
                    case KeyEvent.VK_RIGHT -> movePlayer(0, 1);
                }
            }
        }
    };

    // Method to initialize the main menu
    private void initializeMenu() {
        getContentPane().removeAll();
        currentState = GameState.MENU; // Update state
        removeKeyListener(keyAdapter); // Remove key listener when in menu

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel("Treasure Hunt Adventure", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playButton = new JButton("Play");
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(e -> {
            getContentPane().removeAll();
            initializeGame();
            initializeUI();
            loadHighScores();
            currentState = GameState.PLAYING; // Update state
            revalidate();
            repaint();
            setVisible(true);
            addKeyListener(keyAdapter); // Add key listener when game starts
            setFocusable(true);
            requestFocusInWindow();
        });

        JButton rulesButton = new JButton("Rules");
        rulesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        rulesButton.addActionListener(e -> showRules());

        JButton quitButton = new JButton("Quit");
        quitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitButton.addActionListener(e -> System.exit(0));

        menuPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        menuPanel.add(titleLabel);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(playButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(rulesButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(quitButton);

        add(menuPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
        setVisible(true);
    }

    // Method to show the game rules
    private void showRules() {
        getContentPane().removeAll();
        currentState = GameState.RULES; // Update state
        removeKeyListener(keyAdapter); // Remove key listener when in rules

        JPanel rulesPanel = new JPanel();
        rulesPanel.setLayout(new BorderLayout());
        JTextArea rulesText = new JTextArea(
                """
                        Rules:
                        1. Use arrow keys to move the player (P).
                        2. Find all the treasures (T) to win.
                        3. Avoid all obstacles (X).
                        4. Collect all treasures in the least possible time to win."""
        );
        rulesText.setEditable(false);
        rulesPanel.add(rulesText, BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> initializeMenu());
        rulesPanel.add(backButton, BorderLayout.SOUTH);

        add(rulesPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
        setVisible(true);
    }

    // Method to initialize the game
    private void initializeGame() {
        moves = 0;
        treasuresRemaining = 0;
        timerStarted = false; // Reset timer status
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = 0;
            }
        }
        placePlayer();
        placeTreasures();
        placeObstacles();
    }

    // Method to start the game timer
    private void startTimer() {
        startTime = System.currentTimeMillis();
        timer = new Timer(1000, e -> updateTimerLabel());
        timer.start();
    }

    // Method to stop the game timer
    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // Elapsed time in seconds
    }

    // Method to pause the timer
    private void pauseTimer() {
        if (timer != null) {
            timer.stop();
            pausedTime = System.currentTimeMillis();
        }
    }

    // Method to resume the timer
    private void resumeTimer() {
        if (timer != null) {
            startTime += (System.currentTimeMillis() - pausedTime);
            timer.start();
        }
    }

    // Method to update the timer label
    private void updateTimerLabel() {
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        statusLabel.setText("Time: " + elapsedTime + "s | Moves: " + moves + " | Treasures remaining: " + treasuresRemaining);
    }

    // Method to place the player on the grid
    private void placePlayer() {
        playerX = random.nextInt(gridSize);
        playerY = random.nextInt(gridSize);
        grid[playerX][playerY] = 1;
    }

    // Method to place treasures on the grid
    private void placeTreasures() {
        int treasures = 7;
        treasuresRemaining = treasures;
        while (treasures > 0) {
            int x = random.nextInt(gridSize);
            int y = random.nextInt(gridSize);
            if (grid[x][y] == 0) {
                grid[x][y] = 2;
                treasures--;
            }
        }
    }

    // Method to place obstacles on the grid
    private void placeObstacles() {
        int obstacles = 15 + random.nextInt(15);
        List<int[]> emptyCells = new ArrayList<>();

        // Collect all empty cells
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (grid[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        while (obstacles > 0 && !emptyCells.isEmpty()) {
            int[] cell = emptyCells.remove(random.nextInt(emptyCells.size()));
            grid[cell[0]][cell[1]] = 3; // Place an obstacle

            if (!allTreasuresReachable()) {
                grid[cell[0]][cell[1]] = 0; // Remove the obstacle if it blocks all paths to any treasure
            } else {
                obstacles--;
            }
        }
    }

    private boolean allTreasuresReachable() {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        boolean[][] visited = new boolean[gridSize][gridSize];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{playerX, playerY});
        visited[playerX][playerY] = true;

        int treasuresFound = 0;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];

            if (grid[x][y] == 2) {
                treasuresFound++;
            }

            for (int[] direction : directions) {
                int newX = x + direction[0];
                int newY = y + direction[1];

                if (newX >= 0 && newX < gridSize && newY >= 0 && newY < gridSize && !visited[newX][newY] && grid[newX][newY] != 3) {
                    visited[newX][newY] = true;
                    queue.add(new int[]{newX, newY});
                }
            }
        }

        return treasuresFound == treasuresRemaining;
    }

    // Method to initialize the game UI
    private void initializeUI() {
        loadImages(); // Load images

        JPanel gridPanel = new JPanel(new GridLayout(gridSize, gridSize));
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                gridLabels[i][j] = new JLabel();
                gridLabels[i][j].setOpaque(true);
                gridLabels[i][j].setBackground(Color.WHITE);
                gridLabels[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                gridLabels[i][j].setHorizontalAlignment(SwingConstants.CENTER);
                gridLabels[i][j].setPreferredSize(new Dimension(50, 50));
                gridPanel.add(gridLabels[i][j]);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(statusLabel, BorderLayout.NORTH);

        updateUI();
    }

    // Method to update the game UI
    private void updateUI() {
        int radius = 1; // The radius within which to reveal cells
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (Math.abs(playerX - i) <= radius && Math.abs(playerY - j) <= radius) {
                    switch (grid[i][j]) {
                        case 0 -> {
                            gridLabels[i][j].setBackground(Color.WHITE);
                            gridLabels[i][j].setIcon(null);
                        }
                        case 1 -> {
                            gridLabels[i][j].setBackground(Color.WHITE);
                            gridLabels[i][j].setIcon(playerIcon);
                        }
                        case 2 -> {
                            gridLabels[i][j].setBackground(Color.WHITE);
                            gridLabels[i][j].setIcon(treasureIcon);
                        }
                        case 3 -> {
                            gridLabels[i][j].setBackground(Color.WHITE);
                            gridLabels[i][j].setIcon(obstacleIcon);
                        }
                    }
                } else {
                    gridLabels[i][j].setBackground(Color.GRAY); // Hidden cells
                    gridLabels[i][j].setIcon(null);
                }
            }
        }
        statusLabel.setText("Time: " + elapsedTime + "s | Moves: " + moves + " | Treasures remaining: " + treasuresRemaining);
    }

    // Method to move the player
    private void movePlayer(int dx, int dy) {
        if (!timerStarted) {
            startTimer();
            timerStarted = true;
        }

        int newX = playerX + dx;
        int newY = playerY + dy;

        if (newX < 0 || newX >= gridSize || newY < 0 || newY >= gridSize) {
            JOptionPane.showMessageDialog(this, "You can't move outside the grid!", "Invalid Move", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (grid[newX][newY] == 3) {
            stopTimer();
            JOptionPane.showMessageDialog(this, "You hit an obstacle! You lose. Time: " + elapsedTime + "s", "Game Over", JOptionPane.ERROR_MESSAGE);
            int option = JOptionPane.showConfirmDialog(this, "Do you want to play again?", "Play Again", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                initializeGame();
                updateUI();
            } else {
                System.exit(0);
            }
            return;
        }

        grid[playerX][playerY] = 0;
        playerX = newX;
        playerY = newY;

        if (grid[playerX][playerY] == 2) {
            treasuresRemaining--;
            pauseTimer();
            JOptionPane.showMessageDialog(this, "You found a treasure!", "Treasure Found", JOptionPane.INFORMATION_MESSAGE);
            resumeTimer();
        }

        grid[playerX][playerY] = 1;
        moves++;
        updateUI();
        checkGameStatus();
    }

    // Method to check the game status
    private void checkGameStatus() {
        if (treasuresRemaining == 0) {
            stopTimer();
            String playerName = JOptionPane.showInputDialog(this, "YOU FOUND ALL THE TREASURE IN " + elapsedTime + " SECONDS AND " + moves + " MOVES! ENTER YOUR NAME:", "Congratulations You Win!", JOptionPane.INFORMATION_MESSAGE);
            if (playerName != null && !playerName.trim().isEmpty()) {
                addHighScore(playerName, (int) elapsedTime, moves);
            }
            int option = JOptionPane.showConfirmDialog(this, "DO YOU WANT TO PLAY AGAIN?", "Play Again", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                initializeGame();
                updateUI();
            } else {
                System.exit(0);
            }
        }
    }

    // Method to load high scores from file
    private void loadHighScores() {
        highScores.clear(); // Clear the existing list to avoid duplication
        try (BufferedReader reader = new BufferedReader(new FileReader("src/ICS4U/FinalProject/highscores.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" - ");
                if (parts.length == 2) {
                    String[] details = parts[1].split(", ");
                    if (details.length == 2) {
                        int time = Integer.parseInt(details[0].trim().replace(" seconds", ""));
                        int moves = Integer.parseInt(details[1].trim().replace(" moves", ""));
                        highScores.add(new HighScore(parts[0], time, moves));
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // Method to add a high score and save it
    private void addHighScore(String name, int time, int moves) {
        loadHighScores(); // Reload existing high scores
        highScores.add(new HighScore(name, time, moves));
        highScores.sort((hs1, hs2) -> {
            if (hs1.getTime() != hs2.getTime()) {
                return Integer.compare(hs1.getTime(), hs2.getTime());
            } else {
                return Integer.compare(hs1.getMoves(), hs2.getMoves());
            }
        }); // Sort by time first, then by moves
        saveHighScores();
        displayHighScores();
    }

    // Method to save high scores to file
    private void saveHighScores() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/ICS4U/FinalProject/highscores.txt"))) {
            for (HighScore hs : highScores) {
                writer.write(hs.getName() + " - " + hs.getTime() + " seconds, " + hs.getMoves() + " moves");
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to display high scores
    private void displayHighScores() {
        StringBuilder highScoreMessage = new StringBuilder("High Scores:\n");
        for (HighScore hs : highScores) {
            highScoreMessage.append(hs.getName())
                    .append(" - ")
                    .append(hs.getTime())
                    .append(" seconds, ")
                    .append(hs.getMoves())
                    .append(" moves\n");
        }
        JOptionPane.showMessageDialog(this, highScoreMessage.toString(), "High Scores", JOptionPane.INFORMATION_MESSAGE);
    }

    // Main method to start the game
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TreasureHuntAdventure::new);
    }
}

// HighScore class to store high score data
class HighScore {
    private final String name;
    private final int time;
    private final int moves;

    public HighScore(String name, int time, int moves) {
        this.name = name;
        this.time = time;
        this.moves = moves;
    }

    public String getName() {
        return name;
    }

    public int getTime() {
        return time;
    }

    public int getMoves() {
        return moves;
    }
}