import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

/**
 * Word Master
 * Author: Peter Mitchell (2021)
 *
 * GamePanel class:
 * Controls the game state and all the rendering of the interface.
 */
public class GamePanel extends JPanel implements ActionListener {
    /**
     * Different states that the game can be in.
     * Starting: Shows the start message. Swaps to Playing when SPACE is pressed.
     * Playing: Allows word input and has the timer ticking down till the game ends.
     * GameOver: Shows the end message. Swaps back to Playing with a new game when SPACE is pressed.
     */
    public enum GameState { Starting, Playing, GameOver }

    /**
     * Width of the panel.
     */
    private static final int PANEL_WIDTH = 500;
    /**
     * Height of the panel.
     */
    private static final int PANEL_HEIGHT = 500;
    /**
     * Time between updates in ms.
     */
    private static final int TIME_INTERVAL = 20;
    /**
     * Font used for most of the text displayed.
     */
    private static final Font font = new Font("Arial", Font.BOLD, 40);

    /**
     * Database containing a word list that random words can be pulled from.
     */
    private WordDatabase wordDatabase;
    /**
     * The current word from the database that is being entered by the user.
     */
    private String currentWord;
    /**
     * Timer to track how long is remaining for the player to enter words.
     */
    private ActionTimer timeRemaining;
    /**
     * Timer to keep updates ticking regularly for everything that needs to update on a timer.
     */
    private Timer gameTimer;
    /**
     * A list of all the FadingEventTexts that have been triggered from completed words to show changed scores.
     */
    private List<FadingEventText> fadingEventTexts;

    /**
     * The current index in the currentWord that is being entered by the user.
     */
    private int currentLetterIndex;
    /**
     * The number of characters that were entered incorrectly during the typing of currentWord.
     */
    private int totalWrongLettersForWord;
    /**
     * The total number of wrong letters over the entire game session.
     */
    private int wrongLetters;
    /**
     * The total score over the duration of a game session.
     */
    private int totalScore;
    /**
     * Text prefix for the score shown at the bottom.
     */
    private final String scorePrefixString = "Score: ";
    /**
     * Text prefix for the wrong characters entered shown at the bottom.
     */
    private final String wrongPrefixString = "Incorrect: ";
    /**
     * String combining the scorePrefixString and totalScore.
     */
    private String scoreString;
    /**
     * String combining the wrongPrefixString and wrongScore.
     */
    private String wrongString;
    /**
     * A flag to track if the last character typed was wrong. This changes the remaining letters to red till a
     * next correct character is entered.
     */
    private boolean lastCharacterWrong;

    /**
     * The current game state.
     */
    private GameState gameState;

    /**
     * Prepares the game to be played and initialises it by starting in the Starting game state.
     */
    public GamePanel() {
        setBackground(new Color(179, 179, 179));
        setPreferredSize(new Dimension(PANEL_WIDTH,PANEL_HEIGHT));

        // Word data is taken from: https://github.com/Xethron/Hangman/blob/master/words.txt
        wordDatabase = new WordDatabase("Words.txt");
        gameTimer = new Timer(TIME_INTERVAL, this);
        timeRemaining = new ActionTimer(2*60*1000); // 2 minute timer
        nextWord();
        fadingEventTexts = new ArrayList<>();
        totalScore = 0;
        totalWrongLettersForWord = 0;
        wrongLetters = 0;
        scoreString = scorePrefixString + "0";
        wrongString = wrongPrefixString + "0";
        lastCharacterWrong = false;
        gameState = GameState.Starting;
        gameTimer.start();
    }

    /**
     * Handles the word input and transitions between states by pressing space.
     *
     * @param keyCode The key that was pressed.
     */
    public void handleInput(int keyCode) {
        if(keyCode == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        } else if(keyCode == KeyEvent.VK_SPACE && gameState != GameState.Playing) {
            restart();
        } else if(gameState == GameState.Playing) {
            char currentChar = (char)keyCode;
            if(currentChar >= 'A' && currentChar <= 'Z') {
                testCharacterOnWord(currentChar);
            }
        }
        repaint();
    }

    /**
     * Draws all the visual elements of the game into the panel.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    public void paint(Graphics g) {
        super.paint(g);
        drawBackgroundPanels(g);
        g.setFont(font);
        if(gameState == GameState.Playing) {
            drawCurrentWord(g);
        } else if(gameState == GameState.GameOver) {
            drawEndMessage(g);
        } else {
            drawStartMessage(g);
        }
        drawTime(g);
        drawScore(g);
        for(FadingEventText text : fadingEventTexts) {
            text.paint(g);
        }
    }

    /**
     * Restarts the game by resetting the score to 0 and starting
     * with a new word.
     */
    public void restart() {
        totalScore = 0;
        wrongLetters = 0;
        scoreString = scorePrefixString + "0";
        wrongString = wrongPrefixString + "0";
        lastCharacterWrong = false;
        nextWord();
        gameState = GameState.Playing;
        timeRemaining.reset();
    }

    /**
     * Triggered when the timer goes off. Updates elements on a fixed time interval.
     * Specifically by updating the time remaining when the game is playing, and
     * updating the animation of fading text elements.
     *
     * @param e Reference to the event information.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(gameState == GameState.Playing) {
            timeRemaining.update(TIME_INTERVAL);
            if (timeRemaining.isTriggered()) {
                gameState = GameState.GameOver;
            }
        }
        updateFadingText(TIME_INTERVAL);
        repaint();
    }

    /**
     * Gets a new random word from the database and prepares for
     * detecting entry by the player.
     */
    private void nextWord() {
        currentLetterIndex = 0;
        totalWrongLettersForWord = 0;
        currentWord = wordDatabase.getRandomWord().toUpperCase(Locale.ROOT);
    }

    /**
     * Tests for the specified character if it is correct based on the next
     * expected input. If it was correct the character will be iterated, or
     * the word will be complete with score awarded appropriately.
     * Otherwise it will increase the number of wrong characters detected.
     *
     * @param currentCharacter Character that was pressed.
     */
    private void testCharacterOnWord(char currentCharacter) {
        if (currentCharacter == currentWord.charAt(currentLetterIndex)) {
            currentLetterIndex++;
            lastCharacterWrong = false;
            if (currentLetterIndex == currentWord.length()) {
                // Word ended, apply score and get a new word
                int scoreAdded = Math.max(currentWord.length() - totalWrongLettersForWord, 1);
                totalScore += scoreAdded;
                scoreString = scorePrefixString + totalScore;
                String resultText = "+" + scoreAdded;
                if(totalWrongLettersForWord > 0) {
                    resultText += " (" + totalWrongLettersForWord + " wrong)";
                }
                addFadingText(resultText,Color.BLACK);
                nextWord();
            }
        } else {
            wrongLetters++;
            totalWrongLettersForWord++;
            wrongString = wrongPrefixString + wrongLetters;
            lastCharacterWrong = true;
        }
    }

    /**
     * Updates any FadingEventTexts by forcing them to update. Then checks
     * each element after updating to determine if they are no longer visible.
     * Any that are not visible are removed.
     */
    private void updateFadingText(int deltaTime) {
        for(int i = 0; i < fadingEventTexts.size(); i++) {
            fadingEventTexts.get(i).update(deltaTime);
            if(fadingEventTexts.get(i).isExpired()) {
                fadingEventTexts.remove(i);
                i--;
            }
        }
    }

    /**
     * Creates a FadingEventText to display the specified text.
     *
     * @param message Text to show on the FadingEventText.
     * @param colour The Colour to display the text with.
     */
    private void addFadingText(String message, Color colour) {
        int x = (int)(Math.random()*300-150);
        int y = (int)(Math.random()*300-150);
        fadingEventTexts.add(new FadingEventText(message, new Position(PANEL_WIDTH/2+x,PANEL_HEIGHT/2+y), colour));
    }

    /**
     * Draws the current word split based on number of characters entered correctly
     * so far. Those characters are shown in green. The remainder are shown as black
     * if the last character was valid, or red if the last character was invalid.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    private void drawCurrentWord(Graphics g) {
        g.setFont(font);
        g.setColor(new Color(25, 106, 25));
        String completed = currentWord.substring(0,currentLetterIndex);
        String inComplete = currentWord.substring(currentLetterIndex);
        int completedWordWidth = g.getFontMetrics().stringWidth(completed);
        int inCompleteWordWidth = g.getFontMetrics().stringWidth(inComplete);
        int totalWidth = completedWordWidth + inCompleteWordWidth;
        g.drawString(completed, PANEL_WIDTH/2 - totalWidth/2, PANEL_HEIGHT/2);
        g.setColor(lastCharacterWrong ? Color.RED : Color.BLACK);
        g.drawString(inComplete, PANEL_WIDTH/2 - totalWidth/2 + completedWordWidth, PANEL_HEIGHT/2);
    }

    /**
     * Draws the time remaining centred at the middle of the panel.
     *
     * @param g Reference to Graphics object for rendering.
     */
    private void drawTime(Graphics g) {
        g.setFont(font);
        String currentTimeRemaining = timeRemaining.toString();
        int timeWidth = g.getFontMetrics().stringWidth(currentTimeRemaining);
        g.setColor(timeRemaining.getTimeRemaining() > 5000 ? Color.BLACK : Color.RED);
        g.drawString(currentTimeRemaining, PANEL_WIDTH/2 - timeWidth/2, 40);
    }

    /**
     * Draws the score with score shown at bottom left, and
     * number of total wrong inputs on the bottom right.
     *
     * @param g Reference to Graphics object for rendering.
     */
    private void drawScore(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.setColor(Color.BLACK);
        g.drawString(scoreString, 40, PANEL_HEIGHT-60);
        int wrongWidth = g.getFontMetrics().stringWidth(wrongString);
        g.drawString(wrongString, PANEL_WIDTH-wrongWidth-40, PANEL_HEIGHT-60);
    }

    /**
     * Draws the message during the Starting game state centred.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    private void drawStartMessage(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(font);
        int strWidth = g.getFontMetrics().stringWidth("Press SPACE to Start!");
        g.drawString("Press SPACE to Start!", PANEL_WIDTH/2 - strWidth/2, PANEL_HEIGHT/2);
    }

    /**
     * Draws the message during the GameOver game state centred.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    private void drawEndMessage(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(font);
        int strWidth = g.getFontMetrics().stringWidth("Game Over!");
        g.drawString("Game Over!", PANEL_WIDTH/2 - strWidth/2, PANEL_HEIGHT/2);
        strWidth = g.getFontMetrics().stringWidth("Press SPACE to Restart!");
        g.drawString("Press SPACE to Restart!", PANEL_WIDTH/2 - strWidth/2, PANEL_HEIGHT/2+40);
    }

    /**
     * Draws panels to provide backgrounds to all the text elements.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    private void drawBackgroundPanels(Graphics g) {
        g.setColor(new Color(128, 102, 62));
        g.fillRect(0,0, PANEL_WIDTH, 60);
        g.fillRect(0,PANEL_HEIGHT-150,PANEL_WIDTH,150);

        g.setColor(new Color(62, 47, 28));
        g.fillRect(0,50, PANEL_WIDTH, 10);
        g.fillRect(0,PANEL_HEIGHT-150,PANEL_WIDTH,10);
    }
}
