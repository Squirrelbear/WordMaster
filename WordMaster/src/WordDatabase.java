import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Word Master
 * Author: Peter Mitchell (2021)
 *
 * WordDatabase class:
 * Defines a database that stores a collection of words that can be randomly selected.
 */
public class WordDatabase {
    /**
     * Collection of word data that can be randomly selected from.
     */
    private List<String> data;
    /**
     * Shared Random reference to use for randomising the word selection.
     */
    private Random rand;

    /**
     * Initialises the object by loading from the specified data file.
     *
     * @param databaseFile File to load the word list from.
     */
    public WordDatabase(String databaseFile) {
        data = new ArrayList<>();
        rand = new Random();
        loadDataFromFile(databaseFile);
    }

    /**
     * Randomly selects a single word from the database and returns it.
     *
     * @return A random word from the database.
     */
    public String getRandomWord() {
        return data.get(rand.nextInt(data.size()));
    }

    /**
     * Reads the specified file into a list of words for the database.
     * If any error occurs it will abort and just put in the word "FILEREADERROR".
     *
     * @param fileName File name to load a word list from.
     */
    private void loadDataFromFile(String fileName) {
        File dataFile;
        Scanner fileScanner;

        // Attempt to open the file ready for reading
        try {
            dataFile = new File(fileName);
            fileScanner = new Scanner(dataFile);
        } catch(Exception e) {
            System.out.println("ERROR");
            data.add("FILEREADERROR");
            return;
        }

        // Read every word from the database
        while(fileScanner.hasNextLine()) {
            String currentWord = fileScanner.nextLine();
            if(currentWord.length() > 0) {
                data.add(currentWord);
            }
        }
    }
}
