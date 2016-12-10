package data;

import apptemplate.AppTemplate;
import components.AppDataComponent;
import controller.GameError;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * @author Ritwik Banerjee
 */
public class GameData implements AppDataComponent {

    //HANGMAN INFOOOOOOOOO
    public static final  int TOTAL_NUMBER_OF_GUESSES_ALLOWED = 10;
    private static final int TOTAL_NUMBER_OF_STORED_WORDS    = 330622;

    private String         targetWord;
    private Set<Character> goodGuesses;
    private Set<Character> badGuesses;
    private int            remainingGuesses;
    public  AppTemplate    appTemplate;
    //BUZZWORD INFOOOOOOOO
    private char[] currentLetters; // letter grid is generated randomly
    private int            targetScore; //should be at most the number of valid word on current grid, also > 2 letters

    private int            gameOneProg = 1;
    private int            gameTwoProg = 1;
    private int            gameThreeProg = 1; //initially all game types will be one
    private String         user;
    private int            numWords;
    private ArrayList<String> wordList;
    private ArrayList<String> possibleWordList = new ArrayList<>();


    public ArrayList<String> getPossibleWordList(){return possibleWordList;}
    public int getNumWords(){return numWords;}
    public int getGameOneProg(){
        return gameOneProg;
    }
    public int getGameTwoProg(){
        return gameTwoProg;
    }
    public int getGameThreeProg(){
        return gameThreeProg;
    }
    public char[] getCurrentLetters(){return currentLetters;}
    public void setUser(String user){
        this.user = user;
    }
    public void setGameOneProg(int gameOneProg){
        this.gameOneProg = gameOneProg;
    }
    public void setGameTwoProg(int gameTwoProg){
        this.gameTwoProg = gameTwoProg;
    }
    public void setGameThreeProg(int gameThreeProg){
        this.gameThreeProg = gameThreeProg;
    }

    public GameData(AppTemplate appTemplate) {
        this(appTemplate, false);
    }

    public GameData(AppTemplate appTemplate, boolean initiateGame) {
        if (initiateGame) {
            this.appTemplate = appTemplate;
            this.goodGuesses = new HashSet<>();
            this.badGuesses = new HashSet<>();
            this.remainingGuesses = TOTAL_NUMBER_OF_GUESSES_ALLOWED;
        } else {
            this.appTemplate = appTemplate;
        }
    }

    public void init() {
        this.targetWord = setTargetWord();
        this.goodGuesses = new HashSet<>();
        this.badGuesses = new HashSet<>();
        this.remainingGuesses = TOTAL_NUMBER_OF_GUESSES_ALLOWED;
    }

    @Override
    public void reset() {
        this.targetWord = null;
        this.goodGuesses = new HashSet<>();
        this.badGuesses = new HashSet<>();
        this.remainingGuesses = TOTAL_NUMBER_OF_GUESSES_ALLOWED;
        appTemplate.getWorkspaceComponent().reloadWorkspace();
    }

    public String getTargetWord() {
        return targetWord;
    }

    private String setTargetWord() {

        URL wordsResource = getClass().getClassLoader().getResource("words/words.txt");
        assert wordsResource != null;
        boolean validWord = false;
        String returnMe = "";

        while(validWord == false) {
            int toSkip = new Random().nextInt(TOTAL_NUMBER_OF_STORED_WORDS);
            try (Stream<String> lines = Files.lines(Paths.get(wordsResource.toURI()))) {

                returnMe = lines.skip(toSkip).findFirst().get();
                if (returnMe.contains("'")|| returnMe.contains("*")){
                    validWord = false;
                }
                else
                    validWord = true;
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return returnMe;
    }

    public char[] setGrid(String gameMode) {
        URL wordsResource = getClass().getClassLoader().getResource("words/" + gameMode + ".txt");
        assert wordsResource != null;
        int toSkip;
        char[] returnMe = new char[16];
        String singleWord = "";

        //These get one random word from appropriate list
        if (gameMode.equals("Names")) {
            toSkip = new Random().nextInt(5163);
            numWords = 5163;
        }
        else if (gameMode.equals("Foods")) {
            toSkip = new Random().nextInt(129);
            numWords = 129;
        }
        else {
            toSkip = new Random().nextInt(52);
            numWords = 52;
        }

        try (Stream<String> lines = Files.lines(Paths.get(wordsResource.toURI()))) {
            singleWord = lines.skip(toSkip).findFirst().get();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }
        int lastIndex = 0;
        //ensure a min of 3 vowels on board
        for(int i = 0; i< 3; i++) {
            int randomLetter = new Random().nextInt(4);
            switch (randomLetter) {
                case 0:
                    returnMe[i] = 'a';
                    break;
                case 1:
                    returnMe[i] = 'e';
                    break;
                case 2:
                    returnMe[i] = 'i';
                    break;
                case 3: returnMe[i] = 'o';
                    break;
                case 4: returnMe[i] = 'u';
                    break;
            }
        }

        for(int i = 3; i < 16 ; i++){

            int randomLetter = new Random().nextInt(25) + 97;
            char letter = (char)randomLetter;
            returnMe[i] = letter;
            lastIndex++;
        }
        int j = 0;
        //not used
        for(int i = lastIndex; i < 16; i++){
            returnMe[i] = singleWord.charAt(j);
            j++;
        }
        // **
        wordList = new ArrayList<>();
        for(int i = 0; i < numWords; i++) {
            try (Stream<String> lines = Files.lines(Paths.get(wordsResource.toURI()))) {

                wordList.add(lines.skip(i).findFirst().get());

            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        shuffleArray(returnMe);
        currentLetters = returnMe;
        return returnMe;
    }

    public String getWord(String gameMode, int i){
        URL wordsResource = getClass().getClassLoader().getResource("words/" + gameMode + ".txt");
        assert wordsResource != null;
        String returnMe ="";
        try (Stream<String> lines = Files.lines(Paths.get(wordsResource.toURI()))) {
            returnMe += lines.skip(i).findFirst().get();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return  returnMe;
    }
    static void shuffleArray(char[] ar)
    {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            char a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public GameData setTargetWord(String targetWord) {
        this.targetWord = targetWord;
        return this;
    }

    public Set<Character> getGoodGuesses() {
        return goodGuesses;
    }

    public GameData setGoodGuesses(Set<Character> goodGuesses) {
        this.goodGuesses = goodGuesses;
        return this;
    }

    public Set<Character> getBadGuesses() {
        return badGuesses;
    }

    public GameData setBadGuesses(Set<Character> badGuesses) {
        this.badGuesses = badGuesses;
        return this;
    }

    public int getRemainingGuesses() {
        return remainingGuesses;
    }

    public void addGoodGuess(char c) {
        goodGuesses.add(c);
    }

    public void addBadGuess(char c) {
        if (!badGuesses.contains(c)) {
            badGuesses.add(c);
            remainingGuesses--;
        }
    }

    //finds all present words on the board
    public void findWords(char[][] boggle, boolean[][] visited, int i, int j, String s){
        visited[i][j] = true;
        s += boggle[i][j];
        int M = 4;
        int N = 4;

        //if s is present add it to possibleWordList
        if(isWord(s))
            possibleWordList.add(s);
        if(s.length() > 10)
            return;
        for (int row=i-1; row<=i+1 && row<M; row++)
            for (int col=j-1; col<=j+1 && col<N; col++)
                if (row>=0 && col>=0)
                    if(!visited[row][col])
                        findWords(boggle,visited, row, col, s);
        s = s.substring(0,s.length()-1);
        visited[i][j] = false;
    }
    private boolean isWord(String s){
        for (int i = 0; i < wordList.size(); i++){
            if (s.toString().compareTo(wordList.get(i)) == 0)
                return true;
        }
        return false;
    }
    public void findWordsTop(char[][] c){
        int M = 4;
        int N = 4;
        String str = "";
        boolean[][] visited = new boolean[4][4];

        for (int i=0; i<M; i++)
            for (int j=0; j<N; j++)
                findWords(c, visited, i, j, str);
    }


}
