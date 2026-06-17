package Game_folder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List; 
import java.util.Map;
import java.util.Set;

/*
 * ============================================================
 * MAIN GAME CLASS: Fanidoku
 * ============================================================
 *
 * This class contains the entire implementation of a 3x3 puzzle
 * game inspired by Pokédoku.
 *
 * CORE IDEA:
 * ----------
 * Each row and column has a requirement (attribute + value).
 * A player must click a cell and type a character name that
 * satisfies BOTH:
 *   - That row's requirement
 *   - That column's requirement
 *
 * If correct:
 *   ✅ The cell is filled with the name
 *   ✅ Cell turns green
 *   ✅ Score increases
 *
 * If incorrect:
 *   ❌ Player loses a life
 *   ❌ Cell turns red
 *
 * GAME ENDS WHEN:
 *   ✅ All 9 cells are filled → WIN
 *   ❌ Lives reach 0 → LOSE
 *   ❌ Timer hits 0 → LOSE
 *
 * ============================================================
 */
public class MainGame {

    // ============================================================
    // UI COMPONENTS: GRID + LABELS
    // ============================================================

    /*
     * This is the main 3x3 playable grid.
     * Each element is a JLabel that acts as a clickable cell.
     */
    static JLabel[][] grid = new JLabel[3][3];

    /*
     * These arrays hold the row and column labels.
     * They display rules like:
     *   "Race: Hollow"
     */
    static JLabel[] rowLabels = new JLabel[3];
    static JLabel[] colLabels = new JLabel[3];

    /*
     * Game info labels shown at the top.
     */
    static JLabel scoreLabel;
    static JLabel timerLabel;
    static JLabel livesLabel;

    // ============================================================
    // DATA FROM CSV FILE
    // ============================================================

    /*
     * This stores ALL data loaded from your CSV.
     * Each entry = one character.
     * Each Map holds key-value pairs:
     *   Example:
     *     "Name" → "Ichigo"
     *     "Race" → "Shinigami"
     */
    static List<Map<String, String>> data;

    /*
     * These arrays define WHAT categories are used
     * for rows and columns.
     *
     * Example:
     *   rowCategories[0] = "Race"
     */
    static String[] rowCategories = new String[3];
    static String[] colCategories = new String[3];

    /*
     * These arrays define WHICH VALUES inside a category are used.
     *
     * Example:
     *   rowValues[0] = "Hollow"
     */
    static String[] rowValues = new String[3];
    static String[] colValues = new String[3];

    // ============================================================
    // GAME STATE VARIABLES
    // ============================================================

    /*
     * Stores every name already used in the grid.
     * Prevents duplicate answers.
     */
    static Set<String> usedAnswers = new HashSet<>();

    /*
     * Core game stats.
     */
    static int score = 0;
    static int lives = 3;
    static int timeLeft = 300;

    /*
     * Timer that updates every second.
     */
    static javax.swing.Timer timer;

    // ============================================================
    // ENTRY POINT (PROGRAM START)
    // ============================================================
    public static void main(String[] args) {

        /*
         * Swing UI must run on the event dispatch thread.
         */
        SwingUtilities.invokeLater(MainGame::createUI);
    }

    // ============================================================
    // FILTER INVALID CATEGORIES
    // ============================================================

    /*
     * This removes columns that should NEVER become attributes.
     *
     * Why?
     *  - Name is used for guessing, not filtering
     *  - Image is visual, not logical
     *  - Difficulty is for scoring ONLY
     */
    static void filterInvalidCategories(List<String> keys) {
        keys.removeIf(key ->
                key.equalsIgnoreCase("Name") ||
                key.equalsIgnoreCase("Image") ||
                key.toLowerCase().contains("difficulty")
        );
    }

    // ============================================================
    // MATCHING SYSTEM
    // ============================================================

    /*
     * This checks if a character value matches a condition.
     *
     * It uses .contains() instead of equals to support:
     *   - Multiple attributes:
     *       "Hollow, Arrancar"
     *   - Flexible partial matches
     */
    static boolean matchesValue(String cellVal, String neededVal) {

        if (cellVal == null || neededVal == null) return false;

        return cellVal.toLowerCase().contains(neededVal.toLowerCase());
    }

    /*
     * Prevents invalid values from being used.
     */
    static boolean isValidValue(String val) {

        return val != null &&
               !val.trim().isEmpty() &&
               !val.equalsIgnoreCase("NULL");
    }

    // ============================================================
    // NAME SUGGESTION SYSTEM
    // ============================================================

    /*
     * Provides live autocomplete suggestions.
     *
     * Example:
     * Input: "ich"
     * Output: ["Ichigo", "Ichibei"]
     */
    static List<String> findSimilarNames(String input) {

        List<String> matches = new ArrayList<>();

        if (input == null || input.trim().isEmpty()) return matches;

        input = input.toLowerCase();

        for (Map<String, String> entry : data) {

            String name = entry.get("Name");

            if (!isValidValue(name)) continue;

            if (name.toLowerCase().contains(input)) {
                matches.add(name);
            }
        }

        return matches;
    }

    // ============================================================
    // VALID BOARD CHECK
    // ============================================================

    /*
     * Ensures a specific grid cell has at least ONE valid answer.
     *
     * This prevents impossible puzzles.
     */
    static boolean hasValidAnswer(int i, int j) {

        for (Map<String, String> entry : data) {

            String rVal = entry.get(rowCategories[i]);
            String cVal = entry.get(colCategories[j]);

            if (matchesValue(rVal, rowValues[i]) &&
                matchesValue(cVal, colValues[j])) {

                return true;
            }
        }
        return false;
    }

    // ============================================================
    // UI CREATION
    // ============================================================

    /*
     * Builds entire game window.
     */
    static void createUI() {

        JFrame frame = new JFrame("Fanidoku");

        frame.setSize(900, 900);

        /*
         * Dark themed background for modern look.
         */
        frame.getContentPane().setBackground(new Color(18, 24, 38));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton loadBtn = new JButton("Load CSV");
        loadBtn.addActionListener(e -> loadCSV(frame));

        /*
         * Game info labels setup.
         */
        scoreLabel = new JLabel("Score: 0");
        timerLabel = new JLabel("Time: 300");
        livesLabel = new JLabel("Lives: 3");

        scoreLabel.setForeground(Color.WHITE);
        timerLabel.setForeground(Color.WHITE);
        livesLabel.setForeground(Color.WHITE);

        JPanel top = new JPanel();
        top.setBackground(new Color(18, 24, 38));

        top.add(loadBtn);
        top.add(scoreLabel);
        top.add(timerLabel);
        top.add(livesLabel);

        /*
         * Grid layout:
         * 4x4 → includes headers
         */
        JPanel gridPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        gridPanel.setBackground(new Color(18, 24, 38));

        gridPanel.add(new JLabel(""));

        /*
         * Add column labels
         */
        for (int i = 0; i < 3; i++) {
            colLabels[i] = createLabel();
            gridPanel.add(colLabels[i]);
        }

        /*
         * Build rows + interactive cells
         */
        for (int i = 0; i < 3; i++) {

            rowLabels[i] = createLabel();
            gridPanel.add(rowLabels[i]);

            for (int j = 0; j < 3; j++) {

                grid[i][j] = createCell();

                int r = i, c = j;

                /*
                 * Click behavior
                 */
                grid[i][j].addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {

                        if (data == null) {
                            JOptionPane.showMessageDialog(null, "Load CSV first!");
                            return;
                        }

                        if (grid[r][c].getText().length() > 0) return;

                        String input = showGuessDialog(r, c);

                        if (input != null) checkAnswer(r, c, input);
                    }
                });

                gridPanel.add(grid[i][j]);
            }
        }

        frame.add(top, BorderLayout.NORTH);
        frame.add(gridPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    /*
     * Creates row/column labels.
     */
    static JLabel createLabel() {
        JLabel l = new JLabel("", SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(Color.WHITE);
        l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return l;
    }

    /*
     * Creates individual grid cell.
     */
    static JLabel createCell() {
        JLabel c = new JLabel("", SwingConstants.CENTER);
        c.setOpaque(true);
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return c;
    }

    // ============================================================
    // GUESS DIALOG
    // ============================================================

    /*
     * Custom input window for guesses.
     */
    static String showGuessDialog(int i, int j) {

        JDialog dialog = new JDialog();
        dialog.setTitle("Make your guess");
        dialog.setSize(450, 350);
        dialog.setLayout(new BorderLayout());
        dialog.setModal(true);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 36, 54));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        /*
         * Display requirements clearly
         */
        JLabel rule1 = new JLabel("Row: " + rowCategories[i] + " = " + rowValues[i]);
        JLabel rule2 = new JLabel("Col: " + colCategories[j] + " = " + colValues[j]);

        rule1.setForeground(Color.CYAN);
        rule2.setForeground(Color.CYAN);

        JTextField input = new JTextField();

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        JScrollPane scroll = new JScrollPane(list);

        /*
         * Live suggestion system
         */
        input.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {

                model.clear();

                for (String s : findSimilarNames(input.getText())) {
                    model.addElement(s);
                }
            }
        });

        /*
         * Click suggestion → autofill input
         */
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (list.getSelectedValue() != null)
                    input.setText(list.getSelectedValue());
            }
        });

        JButton submit = new JButton("Submit");

        final String[] result = {null};

        submit.addActionListener(e -> {
            result[0] = input.getText();
            dialog.dispose();
        });

        panel.add(rule1);
        panel.add(rule2);
        panel.add(input);
        panel.add(scroll);
        panel.add(submit);

        dialog.add(panel);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        return result[0];
    }

    // ============================================================
    // WIN / LOSE SYSTEM
    // ============================================================

    /*
     * Checks if ALL 9 cells are filled.
     */
    static void checkWin() {

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (grid[i][j].getText().isEmpty())
                    return;

        showWinScreen();
    }

    /*
     * Displays win message.
     */
    static void showWinScreen() {

        if (timer != null) timer.stop();

        JOptionPane.showMessageDialog(
            null,
            "✅ Congrats!! You've won!!!\n\n" +
            "Score: " + score + "\n" +
            "Time Remaining: " + timeLeft
        );
    }

    /*
     * Displays lose message.
     */
    static void showLoseScreen() {

        if (timer != null) timer.stop();

        JOptionPane.showMessageDialog(
            null,
            "❌ Sorry, you lost.\n\nScore: " + score
        );
    }

    // ============================================================
    // GAME LOGIC (TIMER + CSV)
    // ============================================================

    static void loadCSV(JFrame frame) {

        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(frame);

        if (chooser.getSelectedFile() != null) {

            CsvLoader.load(chooser.getSelectedFile().getAbsolutePath());
            data = CsvLoader.data;

            generatePuzzle();
            startTimer();
        }
    }

    /*
     * Timer decreases each second.
     */
    static void startTimer() {

        if (timer != null) timer.stop();

        timer = new javax.swing.Timer(1000, e -> {

            timeLeft--;
            timerLabel.setText("Time: " + timeLeft);

            if (timeLeft <= 0) {
                showLoseScreen();
            }
        });

        timer.start();
    }

    // ============================================================
    // PUZZLE GENERATION
    // ============================================================

    static void generatePuzzle() {

        while (true) {

            List<String> keys = new ArrayList<>(CsvLoader.headers);

            filterInvalidCategories(keys);
            Collections.shuffle(keys);

            for (int i = 0; i < 3; i++) {
                rowCategories[i] = keys.get(i);
                colCategories[i] = keys.get(i + 3);

                rowValues[i] = getValidValues(rowCategories[i]).get(0);
                colValues[i] = getValidValues(colCategories[i]).get(0);
            }

            boolean valid = true;

            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    if (!hasValidAnswer(i, j)) valid = false;

            if (valid) break;
        }

        updateLabels();
    }

    static List<String> getValidValues(String cat) {

        List<String> list = new ArrayList<>();

        for (Map<String, String> entry : data) {
            String val = entry.get(cat);
            if (isValidValue(val)) list.add(val);
        }

        Collections.shuffle(list);
        return list;
    }

    static void updateLabels() {
        for (int i = 0; i < 3; i++) {
            rowLabels[i].setText(rowCategories[i] + ": " + rowValues[i]);
            colLabels[i].setText(colCategories[i] + ": " + colValues[i]);
        }
    }

    // ============================================================
    // ANSWER CHECKING
    // ============================================================

    static void checkAnswer(int i, int j, String input) {

        Map<String, String> chosen = null;

        /*
         * Find matching character by name
         */
        for (Map<String, String> entry : data) {
            String name = entry.get("Name");

            if (isValidValue(name) && name.equalsIgnoreCase(input)) {
                chosen = entry;
                break;
            }
        }

        if (chosen == null) {
            loseLife(i, j);
            showFeedback(false);
            return;
        }

        /*
         * Get attribute values
         */
        String rVal = chosen.get(rowCategories[i]);
        String cVal = chosen.get(colCategories[j]);

        /*
         * Validate match
         */
        boolean valid =
                matchesValue(rVal, rowValues[i]) &&
                matchesValue(cVal, colValues[j]);

        if (valid && !usedAnswers.contains(chosen.get("Name"))) {

            usedAnswers.add(chosen.get("Name"));
            grid[i][j].setText(chosen.get("Name"));
            grid[i][j].setBackground(new Color(100, 200, 100));

            score++;
            scoreLabel.setText("Score: " + score);

            showFeedback(true);
            checkWin();

        } else {
            loseLife(i, j);
            showFeedback(false);
        }
    }

    // ============================================================
    // LIFE SYSTEM
    // ============================================================

    static void loseLife(int i, int j) {

        lives--;
        livesLabel.setText("Lives: " + lives);

        grid[i][j].setBackground(Color.RED);

        if (lives <= 0) {
            showLoseScreen();
        }
    }

    // ============================================================
    // FEEDBACK DISPLAY
    // ============================================================

    static void showFeedback(boolean correct) {
        JOptionPane.showMessageDialog(null, correct ? "Correct!" : "Incorrect!");
    }
}