package gui;

import apptemplate.AppTemplate;
import components.AppWorkspaceComponent;
import controller.BuzzWordController;
import data.GameData;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.Pair;
import propertymanager.PropertyManager;
import ui.AppGUI;
import ui.AppMessageDialogSingleton;
import ui.OkayButtonDialog;
import ui.YesNoCancelDialogSingleton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static buzzword.BuzzWordProperties.*;
import static settings.AppPropertyType.*;
import static settings.AppPropertyType.WORK_FILE_EXT;
import static settings.InitializationParameters.APP_WORKDIR_PATH;

/**
 * This class serves as the GUI component for the Hangman game.
 *
 * @author Charles Giovanniello
 */
public class Workspace3 extends AppWorkspaceComponent {
    private static final Integer STARTTIME = 60;
    AppTemplate app; // the actual application
    AppGUI      gui; // the GUI inside which the application sits

    Label             guiHeadingLabel;   // workspace (GUI) heading label
    VBox              headPane;          // conatainer to display the heading
    HBox              bodyPane;          // container for the main game displays
    ToolBar           footToolbar;       // toolbar for game buttons
    BorderPane        figurePane;        // container to display the namesake graphic of the (potentially) hanging person
    VBox              gameTextsPane;     // container to display the text-related parts of the game
    HBox              guessedLetters;    // text area displaying all the letters guessed so far
    HBox              wronglyGuessedLetters;
    HBox              remainingGuessBox; // container to display the number of remaining guesses
    HBox              littleBoxes;
    Button            startGame;         // the button to start playing a game of Hangman
    Button            hintButton;         // the button to start playing a game of Hangman
    BuzzWordController controller;
    Canvas            canvas;
    GraphicsContext   gc;
    GameData          gamedata;
    String            userName;
    String            passWord;
    java.nio.file.Path  BuzzWordInfo;
    String            levelSel;
    Polygon           polygon;
    Rectangle         box1;
    Rectangle         box2;
    boolean           loggedIn;
    boolean           paused;
    ReentrantLock lock;
    StackPane guessingStack;
    String guessingWord = "";
    Text guessingNode;
    ArrayList<String> possibleWordList;
    StackPane correctStack;
    int wordBoxCounter;
    int difficulty;
    int totalPoints;
    int targetScore;
    Text totalPointsNumber;
    StackPane totalPointsStack;
    FXTimer timer;
    Text timerNumber;
    StackPane timerStack;
    Timeline timeline;
    String gameMode;



    /**
     * Constructor for initializing the workspace, note that this constructor
     * will fully setup the workspace user interface for use.
     *
     * @param initApp The application this workspace is part of.
     * @throws IOException Thrown should there be an error loading application
     *                     data for setting up the user interface.
     */
    public Workspace3(AppTemplate initApp, String gameMode, int difficulty) throws IOException {
        this.gameMode = gameMode;
        app = initApp;
        this.difficulty = difficulty;
        gamedata = (GameData)app.getDataComponent();
        totalPoints = 0;
        initApp.setWorkspaceUsed("workspace");
        gui = app.getGUI();
        controller = (BuzzWordController) gui.getFileController();    //new HangmanController(app, startGame); <-- THIS WAS A MAJOR BUG!??
        loggedIn = controller.getLoggedIn();
        lock = new ReentrantLock();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("GUI Tester");
        alert.setHeaderText("Which GUI would you like to test?");
        alert.setContentText("Choose your option.");

        layoutGUIGameplay(gameMode, difficulty);
    }

    private void layoutGUIGameplay(String gameMode, int difficulty) {
        wordBoxCounter = 0;
        gui.getExitButton().setVisible(true);
        PropertyManager propertyManager = PropertyManager.getManager();
        guiHeadingLabel = new Label(propertyManager.getPropertyValue(WORKSPACE_HEADING_LABEL));
        headPane = new VBox();
        headPane.getChildren().add(guiHeadingLabel);
        headPane.setAlignment(Pos.CENTER);

        Text gameModeStyled = new Text(gameMode);
        gameModeStyled.setFont(Font.font ("Courier New", 30));
        VBox selectLevel = new VBox();
        selectLevel.getChildren().add(gameModeStyled);
        selectLevel.setAlignment(Pos.CENTER);

        figurePane = new BorderPane();
        guessedLetters = new HBox();
        guessedLetters.setStyle("-fx-background-color: transparent;");

        wronglyGuessedLetters = new HBox();
        wronglyGuessedLetters.setStyle("-fx-background-color: transparent;");

        remainingGuessBox = new HBox();

        gameTextsPane = new VBox();
        //canvas
        canvas = new Canvas();
        canvas.setStyle("-fx-background-color: cyan");
        gc = canvas.getGraphicsContext2D();


        Group root = new Group();
        root.getChildren().add(canvas);
        canvas.setWidth(400);
        canvas.setHeight(400);

        //TEXT PANES
        gameTextsPane.getChildren().setAll(remainingGuessBox, guessedLetters, wronglyGuessedLetters);
        bodyPane = new HBox();
        bodyPane.getChildren().addAll(figurePane, gameTextsPane);
        //BUTTONS
        startGame = new Button("Start Playing");
        HBox blankBoxLeft  = new HBox();
        HBox blankBoxRight = new HBox();
        HBox.setHgrow(blankBoxLeft, Priority.ALWAYS);
        HBox.setHgrow(blankBoxRight, Priority.ALWAYS);
        startGame.setVisible(false);
        gui.getProfileButton().setVisible(true);
        gui.getHomeButton().setVisible(true);
        gui.getPlayButton().setVisible(false);
        gui.getLoginButton().setVisible(false);
        gui.getNewProfileButton().setVisible(false);
        gui.getComboBox().setVisible(false);
        gui.getExitButton().setOnAction(e ->gamePlayExit());

        //Create hint button
        hintButton = new Button("Hint");
        footToolbar = new ToolBar(blankBoxLeft, startGame,blankBoxRight, hintButton);
        hintButton.setVisible(false);

        //********** PLAY BUTTON************************************************************************************
        polygon = new Polygon();
        polygon.getPoints().addAll(new Double[]{
                0.0, 0.0,
                20.0, 20.0,
                0.0, 40.0 });
        box1 = new Rectangle();
        box1.setHeight(45);
        box1.setWidth(15);
        box2 = new Rectangle();
        box2.setHeight(45);
        box2.setWidth(15);
        HBox pauseButton = new HBox(10);
        pauseButton.getChildren().addAll(box1, box2);



        polygon.setOnMouseClicked(e -> pausePlayGame());
        box1.setOnMouseClicked(e -> pausePlayGame());
        box2.setOnMouseClicked(e -> pausePlayGame());
        polygon.setVisible(false);


        HBox emptyBox = new HBox();
        emptyBox.setPadding(new Insets(0,0,0,100));
        emptyBox.setPrefWidth(225);
        HBox playButton = new HBox();
        playButton.getChildren().addAll(emptyBox,polygon,pauseButton);
        playButton.setPadding(new Insets(0, 50, 100, 100));
        //Add everything to workspace ***************
        workspace = new VBox(10);


        //**********************CONSTRUCT GRIDPANE FOR OUR LETTERS(***************************************************
        GridPane gridpane = new GridPane();
        gridpane.setPrefSize(gui.getPrimaryScene().getWidth(), gui.getPrimaryScene().getWidth()/1.6);

        //SET GAP BETWEEN GRIDPANE OBJECTS
        gridpane.setHgap(20); //horizontal gap in pixels => that's what you are asking for
        gridpane.setVgap(20); //vertical gap in pixels
        StackPane[][] circlesArray = new StackPane[4][4];

        //CREATE 4X4 MATRIX FOR THE STACK PANES(WHICH INITIALLY CONTAIN CIRCLES)
        // AND ADD TO ARRAY TO ACCESS EACH ELEMENT WHEN NEEDED
        for (int i = 0; i <= 3; i++){ //row
            for(int j = 0; j <= 3; j++){ //coll
                Circles addMe = new Circles();
                StackPane stack = new StackPane();
                stack.getChildren().addAll(addMe);
                gridpane.add(stack, i, j);
                circlesArray[j][i] = stack;
            }
        }


        HBox gridPanes = new HBox(); //Holds game board and game info
        GridPane gridPane2 = new GridPane();

        //********** GUESSING WORD************************************************************************************
        //The game info (Points,words,targe points etc.
        //Make a stack pane for each element and add those stack panes to the gridp
        guessingStack = new StackPane();
        WordBox guessingBox = new WordBox();
        guessingBox.setHeight(70);
        gridPane2.setMargin(guessingStack, new Insets(50, 50, 20, 50));
        guessingStack.getChildren().add(guessingBox);
        gridPane2.add(guessingStack, 0,0);

        //add currently g
        //guessingStack.getChildren().add(guessingWord);
        guessingStack.setAlignment(Pos.CENTER_LEFT);


        correctStack = new StackPane();
        GridPane correctGrid = new GridPane();
        WordBox correctBox = new WordBox();
        correctBox.setHeight(225);
        gridPane2.setMargin(correctStack, new Insets(0, 50, 0, 50));
        correctStack.getChildren().add(correctBox);
        correctStack.getChildren().add(correctGrid);
        gridPane2.add(correctStack, 0,1);

        //**********FAKE GUESSED WORDS************************************************************************************
        //Add words to the stack pane


        /*Text guessedWord2 = new Text("BURN");
        guessedWord2.setFont(Font.font ("Courier New", 30));
        correctStack.getChildren().add(guessedWord2);
        correctStack.setAlignment(Pos.TOP_LEFT);*/

        //********** TOTAL POINTS************************************************************************************
        Text totalPointsText = new Text("Total: ");
        totalPointsText.setFont(Font.font ("Courier New", 30));

        totalPointsNumber = new Text("        " + totalPoints);
        totalPointsNumber.setFont(Font.font ("Courier New", 30));

        totalPointsStack = new StackPane();
        WordBox totalPoints = new WordBox();
        totalPoints.setHeight(40);
        gridPane2.setMargin(totalPointsStack, new Insets(0, 50, 0, 50));
        totalPointsStack.getChildren().add(totalPoints);
        gridPane2.add(totalPointsStack, 0,2);
        gridPane2.setAlignment(Pos.CENTER);
        totalPointsStack.setAlignment(Pos.CENTER_LEFT);
        totalPointsStack.getChildren().addAll(totalPointsText, totalPointsNumber);

        //*************TIMER*******************************************************************************************
        Text timerText = new Text("Timer: ");
        Text timerNumber = new Text("        0");
        timerNumber.setFont(Font.font ("Courier New", 30));
        createTimer();

        timerStack = new StackPane();
        WordBox timerBox = new WordBox();
        timerBox.setArcHeight(20);
        timerBox.setArcWidth(20);
        timerBox.setHeight(75);
        timerStack.getChildren().add(timerBox);
        bodyPane.setMargin(timerStack, new Insets(0, 0, 0,450));
        bodyPane.getChildren().add(timerStack);
        timerStack.getChildren().addAll(timerText);
        timerStack.setAlignment(Pos.CENTER_LEFT);
        StackPane letterGrid = new StackPane();
        canvas.setDisable(true);
        letterGrid.getChildren().addAll(gridpane,canvas);
        gridPanes.getChildren().addAll(letterGrid, gridPane2);

        //**************LETTER GRID*******************************************************************************
        targetScore = 0;

        do {
            gamedata.setGrid(gameMode);
            char[] letters = gamedata.getCurrentLetters();
            ArrayList<Character> letterList = new ArrayList<Character>();
            for (int i = 0; i < letters.length; i++) {
                letterList.add(letters[i]);
            }
            int k = 0;
            char[][] ch = new char[4][4];
            for (int i = 0; i <= 3; i++) { //row
                for (int j = 0; j <= 3; j++) { //coll
                    ch[j][i] = Character.valueOf(letters[k]);
                    k++;
                }
            }
        /*char ch = circlesArray[0][0].getChildren().get(1).toString().charAt(0);
        for (int i = 0; i < gamedata.getNumWords(); i++){
            String checkInGrid = gamedata.getWord(gameMode, i); // will initially get first word in text file
            for (int j = 0; j < checkInGrid.length(); j++){
                char c = checkInGrid.charAt(j);
                if( letterList.contains(c) ){

                }
            }
        }*/


            //The goal is to have gamedata.getPossibleWordList() give te possible words on the board
            gamedata.findWordsTop(ch);
            possibleWordList = gamedata.getPossibleWordList();
            Set<String> hs = new HashSet<>();
            hs.addAll(possibleWordList);
            possibleWordList.clear();
            possibleWordList.addAll(hs);

            for (int i = 0; i < possibleWordList.size(); i++) {
                if (difficulty == 1)
                    targetScore = 20;
                if (difficulty == 2)
                    targetScore = 40;
                if (difficulty == 3)
                    targetScore = 40;
                if (difficulty == 4)
                    targetScore = 80;
                if (difficulty == 5)
                    targetScore = 60;
                if (difficulty == 6)
                    targetScore += 120;
                if (difficulty == 7)
                    targetScore = 80;
                if (difficulty == 8)
                    targetScore += 160;
            }
            Text targetPointsText = new Text("Target: ");
            targetPointsText.setFont(Font.font("Courier New", 30));

            Text targetPointsNumber = new Text("        " + (targetScore));
            targetPointsNumber.setFont(Font.font("Courier New", 30));

            // ********** TARGET POINTS************************************************************************************
            StackPane targetWordStack = new StackPane();
            WordBox targetPoints = new WordBox();
            targetPoints.setArcHeight(20);
            targetPoints.setArcWidth(20);
            targetPoints.setHeight(75);
            gridPane2.setMargin(targetWordStack, new Insets(15, 50, 0, 50));
            targetWordStack.getChildren().add(targetPoints);
            gridPane2.add(targetWordStack, 0, 3);
            gridPane2.setAlignment(Pos.CENTER);
            targetWordStack.getChildren().addAll(targetPointsText, targetPointsNumber);
            targetWordStack.setAlignment(Pos.CENTER_LEFT);
            //gridPanes.getChildren().addAll(gridpane, gridPane2);
        }while(targetScore < 10);
        int k = 0;
        char[][] ch = new char[4][4];
        for (int i = 0; i <= 3; i++) { //row
            for (int j = 0; j <= 3; j++) { //coll
                Text letter = new Text(String.valueOf(gamedata.getCurrentLetters()[k]));
                letter.setFont(Font.font("Courier New", 50));
                StackPane currentPane = circlesArray[j][i];
                currentPane.getChildren().add(letter);
                currentPane.setCache(true);
                currentPane.setCacheHint(CacheHint.QUALITY);
                currentPane.setCacheHint(CacheHint.SPEED);
                //currentPane.setOnMousePressed(e -> addShadow(currentPane));
                currentPane.setOnMouseDragEntered(e -> addShadow(currentPane));
                //currentPane.setOnMouseDragReleased(e -> removeShadow(currentPane));
                currentPane.setOnMouseDragExited(e -> removeShadow(currentPane));
                currentPane.setOnMouseReleased(e -> removeShadowReturn());

                try {
                    lock.lock();
                    new Timeline(
                            new KeyFrame(Duration.seconds(1.0),
                                    new KeyValue(circlesArray[j][i].scaleXProperty(), 1.1),
                                    new KeyValue(circlesArray[j][i].scaleYProperty(), 1.1),
                                    new KeyValue(circlesArray[j][i].rotateProperty(), 360),
                                    new KeyValue(circlesArray[j][i].cacheHintProperty(), CacheHint.QUALITY)
                            )
                    ).play();
                }finally {
                    lock.unlock();
                }
                ch[j][i] = Character.valueOf(gamedata.getCurrentLetters()[k]);
                k++;
            }
        }
        for (int i = 0; i < possibleWordList.size(); i++)
            System.out.println(possibleWordList.get(i).toString());
        //ADD ALL WORKSPACE OBJECTS TO GUI

        workspace.getChildren().addAll(headPane,selectLevel, bodyPane,blankBoxLeft, gridPanes, playButton);//add littleBox
        //CENTER THE GRIDPANE
        gridpane.setAlignment(Pos.CENTER);
    }
    public void addShadow(StackPane pane){
        ((Circles)pane.getChildren().get(0)).addShadow();
        guessingWord += ((Text)pane.getChildren().get(1)).getText();
        guessingNode = new Text();
        guessingNode.setFont(Font.font("Courier New", 30));
        guessingNode.setText(guessingWord);
        guessingStack.getChildren().add(guessingNode);
    }
    public void removeShadow(StackPane pane){
        ((Circles)pane.getChildren().get(0)).removeShadow();
    }
    public void removeShadowReturn(){
        compareGuessingWord();
    }
    public boolean compareGuessingWord(){
        for(int i =0; i <possibleWordList.size(); i++) {
            if (possibleWordList.get(i).equals(guessingWord) && validateForGameMode(guessingWord)) {
                //calculate total points
                totalPoints += awardedPoints(guessingWord);
                Text totalPointsNumber = new Text("        " + totalPoints);
                totalPointsNumber.setFont(Font.font ("Courier New", 30));
                totalPointsStack.getChildren().remove(2);
                totalPointsStack.getChildren().add(totalPointsNumber);
                //add to the list of guessing words
                String temp = guessingWord;
                guessingWord = "";
                for(int j = 0; j<wordBoxCounter; j++){
                    guessingWord += "\n";
                }
                wordBoxCounter++;
                guessingWord+=temp;
                Text guessedWord = new Text(guessingWord);
                guessedWord.setFont(Font.font ("Courier New", 30));

                correctStack.getChildren().add(guessedWord);
                correctStack.setAlignment(Pos.TOP_LEFT);
                guessingWord = "";
                guessingNode.setText("");
                guessingStack.getChildren().remove(1, guessingStack.getChildren().size()-1);
                //Check it game won
                if(levelCleared())
                    showLevelCleared();
                return true;
            }
        }
        System.out.println("Not a word");
        guessingWord = "";
        guessingNode.setText("");
        guessingStack.getChildren().remove(1, guessingStack.getChildren().size()-1);
        return false;
    }
    public boolean levelCleared(){
        if (totalPoints >= targetScore)
            return true;
        return false;
    }
    public void showLevelCleared(){
        PropertyManager           props  = PropertyManager.getManager();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        ButtonType buttonTypeOne = new ButtonType("Continue");
        alert.getButtonTypes().setAll(buttonTypeOne);
        alert.setTitle(props.getPropertyValue(LEVEL_CLEARED_TITLE));
        alert.setHeaderText(null);
        alert.setContentText(props.getPropertyValue(LEVEL_CLEARED_MESSAGE));
        alert.showAndWait();
        try {
            launchNextLevel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static int awardedPoints(String word){
        switch (word.length()) {
            case 3:
                return 10;
            case 4:
                return 20;
            case 5:
                return 30;
            case 6:
                return 40;
            case 7:
                return 50;
            default:
                return 100;
        }
    }
    public boolean validateForGameMode(String word){
        switch (difficulty) {
            case 8:
            case 7:
                if(word.length() >= 6)
                    return true;
                else return false;
            case 6:
            case 5:
                if(word.length() >= 5)
                    return true;
                else return false;

            case 4:
            case 3:
                if(word.length() >= 4)
                    return true;
                else return false;
            case 2:
            case 1:
                if (word.length() >= 3)
                    return true;
                else return false;
            default: return false;
        }
    }
    public void pausePlayGame(){
        if(paused){
            gc.clearRect(0,0,400,400);
            paused = false;
            polygon.setVisible(false);
            box1.setVisible(true);
            box2.setVisible(true);
        }
        else {
            gc.setFill(Paint.valueOf("blue"));
            gc.fillRect(0, 0, 400, 400);
            gc.beginPath();
            gc.stroke();
            paused = true;
            gc.setFill(Paint.valueOf("white"));
            gc.setFont(Font.font ("Courier New", 50));
            gc.fillText("Paused", 110,200);
            gc.beginPath();
            gc.stroke();
            polygon.setVisible(true);
            box1.setVisible(false);
            box2.setVisible(false);
        }
    }
    public void updateTimer(int i){
        timerStack.getChildren().remove(1,timerStack.getChildren().size() );
        Text timerText = new Text("Time:    ");
        timerText.setFont(Font.font ("Courier New", 30));

        Text timerNumber2 = new Text("       " + i);
        timerNumber2.setFill(Color.RED);
        timerNumber2.setFont(Font.font ("Courier New", 30));
        timerStack.getChildren().addAll(timerText, timerNumber2);
    }
    public void createTimer(){
        Label timerLabel = new Label();
        final Integer[] timeSeconds = {STARTTIME};

        timerLabel.setText(timeSeconds[0].toString());
        timerLabel.setTextFill(Color.RED);
        timerLabel.setStyle("-fx-font-size: 4em;");
        //
        if (timeline != null) {
            timeline.stop();
        }
        timeSeconds[0] = STARTTIME;
        //
        timerLabel.setText(timeSeconds[0].toString());
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1),
                        new EventHandler() {
                            // KeyFrame event handler
                            @Override
                            public void handle(Event event) {
                                timeSeconds[0]--;
                                // update timerLabel
                                updateTimer(timeSeconds[0]);

                                timerLabel.setText(
                                        timeSeconds[0].toString());
                                if (timeSeconds[0] <= 0) {
                                    timeline.stop();
                                }
                            }
                        }));
        timeline.playFromStart();
    }
    public void gamePlayExit() {
        pausePlayGame();
        PropertyManager propertyManager = PropertyManager.getManager();
        YesNoCancelDialogSingleton yesNoCancelDialog = YesNoCancelDialogSingleton.getSingleton();

        yesNoCancelDialog.show("Exit?","Are you sure you want to exit?");
        if (yesNoCancelDialog.getSelection().equals(YesNoCancelDialogSingleton.YES))
            gui.getWindow().close();
    }
    public void launchNextLevel() throws IOException{
        PropertyManager propertyManager = PropertyManager.getManager();
        FileChooser filechooser = new FileChooser();
        java.nio.file.Path appDirPath  = Paths.get(propertyManager.getPropertyValue(APP_TITLE)).toAbsolutePath();
        java.nio.file.Path targetPath  = appDirPath.resolve(APP_WORKDIR_PATH.getParameter());
        filechooser.setInitialDirectory(targetPath.toFile());
        File selectedFile = filechooser.getInitialDirectory();
        //app.getFileComponent().updateUserProg(app.getDataComponent(), selectedFile.toPath(), gameMode, userName, difficulty+1);
        app.renderGamePlay(gui.getWindow(),app.getDataComponent(),app.getFileComponent(), gameMode, difficulty+1);
    }

    /**
     * This function specifies the CSS for all the UI components known at the time the workspace is initially
     * constructed. Components added and/or removed dynamically as the application runs need to be set up separately.
     */
    @Override
    public void initStyle() {
        PropertyManager propertyManager = PropertyManager.getManager();
        gui.getAppPane().setId(propertyManager.getPropertyValue(ROOT_BORDERPANE_ID));
        gui.getToolbarPane().getStyleClass().setAll(propertyManager.getPropertyValue(SEGMENTED_BUTTON_BAR));
        gui.getToolbarPane().setId(propertyManager.getPropertyValue(TOP_TOOLBAR_ID));

        ObservableList<Node> toolbarChildren = gui.getToolbarPane().getChildren();
        toolbarChildren.get(0).getStyleClass().add(propertyManager.getPropertyValue(FIRST_TOOLBAR_BUTTON));
        toolbarChildren.get(toolbarChildren.size() - 1).getStyleClass().add(propertyManager.getPropertyValue(LAST_TOOLBAR_BUTTON));

        workspace.getStyleClass().add(CLASS_BORDERED_PANE);
        guiHeadingLabel.getStyleClass().setAll(propertyManager.getPropertyValue(HEADING_LABEL));


    }
    /** This function reloads the entire workspace */
    @Override
    public void reloadWorkspace() {

    }


    public VBox getGameTextsPane() {
        return gameTextsPane;
    }

    public HBox getLittleBoxes() {return littleBoxes;}

    public HBox getRemainingGuessBox() {
        return remainingGuessBox;
    }

    public Button getStartGame() {
        return startGame;
    }

    public Button getHintButton() {
        return hintButton;
    }

}