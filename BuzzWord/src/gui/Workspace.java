package gui;

import apptemplate.AppTemplate;
import components.AppWorkspaceComponent;
import controller.BuzzWordController;
import data.GameData;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import propertymanager.PropertyManager;
import ui.AppGUI;
import ui.AppMessageDialogSingleton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import static buzzword.BuzzWordProperties.*;
import static settings.AppPropertyType.*;
import static settings.AppPropertyType.WORK_FILE_EXT;
import static settings.InitializationParameters.APP_WORKDIR_PATH;

/**
 * This class serves as the GUI component for the Hangman game.
 *
 * @author Charles Giovanniello
 */
public class Workspace extends AppWorkspaceComponent {

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
    boolean           loggedIn;


    boolean           head;
    boolean           lEye;
    boolean           rEye;
    boolean           hair;
    boolean           lArm;
    boolean           rArm;
    boolean           body;
    boolean           lLeg;
    boolean           rLeg;
    boolean           mouth;


    /**
     * Constructor for initializing the workspace, note that this constructor
     * will fully setup the workspace user interface for use.
     *
     * @param initApp The application this workspace is part of.
     * @throws IOException Thrown should there be an error loading application
     *                     data for setting up the user interface.
     */
    public Workspace(AppTemplate initApp) throws IOException {
        app = initApp;
        initApp.setWorkspaceUsed("workspace");
        gui = app.getGUI();
        controller = (BuzzWordController) gui.getFileController();    //new HangmanController(app, startGame); <-- THIS WAS A MAJOR BUG!??
        loggedIn = controller.getLoggedIn();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("GUI Tester");
        alert.setHeaderText("Which GUI would you like to test?");
        alert.setContentText("Choose your option.");

        ButtonType buttonTypeOne = new ButtonType("Home Page");

        ButtonType buttonTypeTwoA = new ButtonType("Level Select 1");
        ButtonType buttonTypeTwoB = new ButtonType("Level Select 2");
        ButtonType buttonTypeTwoC = new ButtonType("Level Select 3");
        ButtonType buttonTypeThree = new ButtonType("Game Play");


        /*alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwoA,buttonTypeTwoB,buttonTypeTwoC, buttonTypeThree);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne){
            layoutGUIHome();
        } else if (result.get() == buttonTypeTwoA) {
            layoutGUILevelSel1("Famous people");
        } else if (result.get() == buttonTypeTwoB) {
            layoutGUILevelSel1("State Capitals");
        } else if (result.get() == buttonTypeTwoC) {
            layoutGUILevelSel1("Foods");
        } else if (result.get() == buttonTypeThree) {
            layoutGUIGameplay("Famous people");
        }*/


        //layoutGUILevelSel("Famous people");
        layoutGUIHome();
        //layoutGUIGameplay("Famous people");
        //setupHandlers(); // ... and set up event handling
    }
    private void loginScreen(){
        PropertyManager propertyManager = PropertyManager.getManager();
        Label profIcon = new Label(propertyManager.getPropertyValue(PROFESSAUR_ICON));
        //Creating a GridPane container
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Login to BuzzWord!");
        // Set the icon (must be included in the project).
       /* File file = new File(profIcon.toString());
        String absolutePath = file.getAbsolutePath();
        Image image = new Image(absolutePath);

        // simple displays ImageView the image as is
        ImageView iv1 = new ImageView();
        iv1.setImage(image);
        dialog.setGraphic(iv1);
*/
        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        dialog.getDialogPane().setContent(grid);
        // Do some validation (using the Java 8 lambda syntax).
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });


        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            //System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
            userName = usernamePassword.getKey();
            passWord = usernamePassword.getValue();
        });

        try {
            handleLogin();
        }catch(IOException e){

        }
    }

    private void createProfileScreen(){
        PropertyManager propertyManager = PropertyManager.getManager();
        Label profIcon = new Label(propertyManager.getPropertyValue(PROFESSAUR_ICON));
        //Creating a GridPane container
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Create Profile");
        dialog.setHeaderText("Create your BuzzWord profile!");
        // Set the icon (must be included in the project).
       /* File file = new File(profIcon.toString());
        String absolutePath = file.getAbsolutePath();
        Image image = new Image(absolutePath);

        // simple displays ImageView the image as is
        ImageView iv1 = new ImageView();
        iv1.setImage(image);
        dialog.setGraphic(iv1);
*/
        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Create!", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        dialog.getDialogPane().setContent(grid);
        // Do some validation (using the Java 8 lambda syntax).
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });


        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            //System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
            userName = usernamePassword.getKey();
            passWord = usernamePassword.getValue();
        });

        try {
            handleCreateProfile();
        }catch(IOException e){

        }
    }

    public void layoutGUIHome() {
        setupHandlers();
        PropertyManager propertyManager = PropertyManager.getManager();
        guiHeadingLabel = new Label(propertyManager.getPropertyValue(WORKSPACE_HEADING_LABEL));
        headPane = new VBox();
        headPane.getChildren().add(guiHeadingLabel);
        headPane.setAlignment(Pos.CENTER);

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
        canvas.setWidth(gui.getPrimaryScene().getWidth());
        canvas.setHeight(gui.getPrimaryScene().getHeight() / 1.6);

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
        gui.getProfileButton().setVisible(false);
        gui.getHomeButton().setVisible(false);
        if(!loggedIn)
            gui.getPlayButton().setDisable(true);

        //Create hint button
        hintButton = new Button("Hint");
        footToolbar = new ToolBar(blankBoxLeft, startGame,blankBoxRight, hintButton);
        hintButton.setVisible(false);

        //footToolbar.setVisible(false);

        //gamedata = (GameData) app.getDataComponent();
        //int numBoxes = gamedata.getTargetWord().length();

        //Add everything to workspace ***************
        workspace = new VBox(10);

        //CONSTRUCT GRIDPANE FOR OUR LETTERS
        HBox gridPanes = new HBox();


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

        //ADD ALL WORKSPACE OBJECTS TO GUI
        workspace.getChildren().addAll(headPane, bodyPane,blankBoxLeft, gridpane);//add littleBox
        //CENTER THE GRIDPANE
        gridpane.setAlignment(Pos.CENTER);

        //ADD GAME NAME LETTERS
        //B
        Text letterB = new Text("B");
        letterB.setFont(Font.font ("Courier New", 50));
        circlesArray[0][0].getChildren().add(letterB);
        //U
        Text letterU = new Text("U");
        letterU.setFont(Font.font ("Courier New", 50));
        circlesArray[0][1].getChildren().add(letterU);
        //Z
        Text letterZ = new Text("Z");
        letterZ.setFont(Font.font ("Courier New", 50));
        circlesArray[1][0].getChildren().add(letterZ);
        Text letterZ2 = new Text("Z");
        letterZ2.setFont(Font.font ("Courier New", 50));
        circlesArray[1][1].getChildren().add(letterZ2);

        //W
        Text letterW = new Text("W");
        letterW.setFont(Font.font ("Courier New", 50));
        circlesArray[2][2].getChildren().add(letterW);
        //O
        Text letterO = new Text("O");
        letterO.setFont(Font.font ("Courier New", 50));
        circlesArray[2][3].getChildren().add(letterO);
        //R
        Text letterR = new Text("R");
        letterR.setFont(Font.font ("Courier New", 50));
        circlesArray[3][2].getChildren().add(letterR);
        //D
        Text letterD = new Text("D");
        letterD.setFont(Font.font ("Courier New", 50));
        circlesArray[3][3].getChildren().add(letterD);


    }

    private void layoutGUILevelSel(String gameMode) {
        gui.getComboBox().setVisible(false);
        gui.getPlayButton().setVisible(false);
        gui.getNewProfileButton().setVisible(false);
        gui.getLoginButton().setVisible(false);
        PropertyManager propertyManager = PropertyManager.getManager();
        guiHeadingLabel = new Label(propertyManager.getPropertyValue(WORKSPACE_HEADING_LABEL));
        headPane = new VBox(100);
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
        canvas.setWidth(gui.getPrimaryScene().getWidth());
        canvas.setHeight(gui.getPrimaryScene().getHeight() / 1.6);

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


        //Create hint button
        hintButton = new Button("Hint");
        footToolbar = new ToolBar(blankBoxLeft, startGame,blankBoxRight, hintButton);
        hintButton.setVisible(false);

        //footToolbar.setVisible(false);

        //gamedata = (GameData) app.getDataComponent();
        //int numBoxes = gamedata.getTargetWord().length();

        //Add everything to workspace ***************
        workspace = new VBox(10);

        //CONSTRUCT GRIDPANE FOR OUR LETTERS
        GridPane gridpane = new GridPane();
        gridpane.setPrefSize(gui.getPrimaryScene().getWidth(), gui.getPrimaryScene().getWidth()/1.6);

        //SET GAP BETWEEN GRIDPANE OBJECTS
        gridpane.setHgap(20); //horizontal gap in pixels => that's what you are asking for
        gridpane.setVgap(20); //vertical gap in pixels
        StackPane[][] circlesArray = new StackPane[4][4];

        //CREATE 4X4 MATRIX FOR THE STACK PANES(WHICH INITIALLY CONTAIN CIRCLES)
        // AND ADD TO ARRAY TO ACCESS EACH ELEMENT WHEN NEEDED
        int counter = 1;
        for (int i = 0; i <= 1; i++){ //row
            for(int j = 0; j <= 3; j++){ //coll
                Text levelNumber = new Text(String.valueOf(counter));
                levelNumber.setFont(Font.font ("Courier New", 30));
                Circles addMe = new Circles();
                StackPane stack = new StackPane();
                stack.getChildren().addAll(addMe, levelNumber);
                gridpane.add(stack, j, i);
                circlesArray[j][i] = stack;
                counter++;
            }
        }

        //ADD ALL WORKSPACE OBJECTS TO GUI
        workspace.getChildren().addAll(headPane,selectLevel, bodyPane,blankBoxLeft, gridpane);//add littleBox
        //CENTER THE GRIDPANE
        gridpane.setAlignment(Pos.CENTER);

    }


    private void layoutGUIGameplay(String gameMode) {
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
        canvas.setWidth(gui.getPrimaryScene().getWidth());
        canvas.setHeight(gui.getPrimaryScene().getHeight() / 1.6);

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
        gui.getExitButton().setVisible(false);


        //Create hint button
        hintButton = new Button("Hint");
        footToolbar = new ToolBar(blankBoxLeft, startGame,blankBoxRight, hintButton);
        hintButton.setVisible(false);

        //********** PLAY BUTTON************************************************************************************
        Polygon polygon = new Polygon();
        polygon.getPoints().addAll(new Double[]{
                0.0, 0.0,
                20.0, 20.0,
                0.0, 40.0 });

        HBox emptyBox = new HBox();
        emptyBox.setPadding(new Insets(0,0,0,100));
        emptyBox.setPrefWidth(225);
        HBox playButton = new HBox();
        playButton.getChildren().addAll(emptyBox,polygon);
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
                addMe.addShadow();
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
        StackPane guessingStack = new StackPane();
        WordBox guessingBox = new WordBox();
        guessingBox.setHeight(70);
        gridPane2.setMargin(guessingStack, new Insets(50, 50, 20, 50));
        guessingStack.getChildren().add(guessingBox);
        gridPane2.add(guessingStack, 0,0);

        //add currently g
        Text guessingWord = new Text("BU");
        guessingWord.setFont(Font.font ("Courier New", 30));
        guessingStack.getChildren().add(guessingWord);
        guessingStack.setAlignment(Pos.CENTER_LEFT);


        StackPane correctStack = new StackPane();
        GridPane correctGrid = new GridPane();
        WordBox correctBox = new WordBox();
        correctBox.setHeight(225);
        gridPane2.setMargin(correctStack, new Insets(0, 50, 0, 50));
        correctStack.getChildren().add(correctBox);
        correctStack.getChildren().add(correctGrid);
        gridPane2.add(correctStack, 0,1);

        //**********FAKE GUESSED WORDS************************************************************************************
        //Add words to the stack pane
        Text guessedWord1 = new Text("BULK");
        guessedWord1.setFont(Font.font ("Courier New", 30));
        correctGrid.add(guessedWord1,0,0);

        Text guessedWord1Points = new Text("20");
        guessedWord1Points.setFont(Font.font ("Courier New", 30));
        correctGrid.add(guessedWord1Points,1,0);
        correctGrid.setHgap(70);

        Text guessedWord2 = new Text("BUN");
        guessedWord2.setFont(Font.font ("Courier New", 30));
        correctGrid.add(guessedWord2,0,1);

        Text guessedWord2Points = new Text("10");
        guessedWord2Points.setFont(Font.font ("Courier New", 30));
        correctGrid.add(guessedWord2Points,1,1);
        correctGrid.setHgap(70);

        /*Text guessedWord2 = new Text("BURN");
        guessedWord2.setFont(Font.font ("Courier New", 30));
        correctStack.getChildren().add(guessedWord2);
        correctStack.setAlignment(Pos.TOP_LEFT);*/
        //********** TOTAL POINTS************************************************************************************
        Text totalPointsText = new Text("Total: ");
        totalPointsText.setFont(Font.font ("Courier New", 30));

        Text totalPointsNumber = new Text("        30");
        totalPointsNumber.setFont(Font.font ("Courier New", 30));

        StackPane totalPointsStack = new StackPane();
        WordBox totalPoints = new WordBox();
        totalPoints.setHeight(40);
        gridPane2.setMargin(totalPointsStack, new Insets(0, 50, 0, 50));
        totalPointsStack.getChildren().add(totalPoints);
        gridPane2.add(totalPointsStack, 0,2);
        gridPane2.setAlignment(Pos.CENTER);
        totalPointsStack.setAlignment(Pos.CENTER_LEFT);
        totalPointsStack.getChildren().addAll(totalPointsText, totalPointsNumber);


        Text targetPointsText = new Text("Target: ");
        targetPointsText.setFont(Font.font ("Courier New", 30));

        Text targetPointsNumber = new Text("        75");
        targetPointsNumber.setFont(Font.font ("Courier New", 30));

        // ********** TARGET POINTS************************************************************************************
        StackPane targetWordStack = new StackPane();
        WordBox targetPoints = new WordBox();
        targetPoints.setArcHeight(20);
        targetPoints.setArcWidth(20);
        targetPoints.setHeight(75);
        gridPane2.setMargin(targetWordStack, new Insets(15, 50, 0, 50));
        targetWordStack.getChildren().add(targetPoints);
        gridPane2.add(targetWordStack, 0,3);
        gridPane2.setAlignment(Pos.CENTER);
        targetWordStack.getChildren().addAll(targetPointsText, targetPointsNumber);
        targetWordStack.setAlignment(Pos.CENTER_LEFT);
        //gridPanes.getChildren().addAll(gridpane, gridPane2);

        //*************TIMER*******************************************************************************************
        Text timerText = new Text("Timer: ");
        timerText.setFont(Font.font ("Courier New", 30));

        Text timerNumber = new Text("        75");
        timerNumber.setFont(Font.font ("Courier New", 30));

        StackPane timerStack = new StackPane();
        WordBox timerBox = new WordBox();
        timerBox.setArcHeight(20);
        timerBox.setArcWidth(20);
        timerBox.setHeight(75);
        timerStack.getChildren().add(timerBox);
        bodyPane.setMargin(timerStack, new Insets(0, 0, 0,450));
        bodyPane.getChildren().add(timerStack);
        timerStack.getChildren().addAll(timerText);
        timerStack.setAlignment(Pos.CENTER_LEFT);
        gridPanes.getChildren().addAll(gridpane, gridPane2);

        //**************FAKE LETTER GRID*******************************************************************************
        //ADD GAME NAME LETTERS
        //B
        Text letterB = new Text("B");
        letterB.setFont(Font.font ("Courier New", 50));
        circlesArray[0][0].getChildren().add(letterB);
        //U
        Text letterU = new Text("U");
        letterU.setFont(Font.font ("Courier New", 50));
        circlesArray[0][1].getChildren().add(letterU);
        //Z
        Text letterZ = new Text("Z");
        letterZ.setFont(Font.font ("Courier New", 50));
        circlesArray[1][0].getChildren().add(letterZ);
        Text letterZ2 = new Text("Z");
        letterZ2.setFont(Font.font ("Courier New", 50));
        circlesArray[1][1].getChildren().add(letterZ2);

        //W
        Text letterW = new Text("W");
        letterW.setFont(Font.font ("Courier New", 50));
        circlesArray[2][2].getChildren().add(letterW);
        //O
        Text letterO = new Text("O");
        letterO.setFont(Font.font ("Courier New", 50));
        circlesArray[2][3].getChildren().add(letterO);
        //R
        Text letterR = new Text("R");
        letterR.setFont(Font.font ("Courier New", 50));
        circlesArray[3][2].getChildren().add(letterR);
        //D
        Text letterD = new Text("D");
        letterD.setFont(Font.font ("Courier New", 50));
        circlesArray[3][3].getChildren().add(letterD);




        //ADD ALL WORKSPACE OBJECTS TO GUI
        workspace.getChildren().addAll(headPane,selectLevel, bodyPane,blankBoxLeft, gridPanes, playButton);//add littleBox
        //CENTER THE GRIDPANE
        gridpane.setAlignment(Pos.CENTER);


    }

    public void addLittleBoxes(AppTemplate app){
        littleBoxes = new HBox();
        gamedata = (GameData) app.getDataComponent();
        int numBoxes = gamedata.getTargetWord().length();
        for(int i = 0; i < numBoxes; i++){
            StackPane boxHolder = new StackPane();
            littleBoxes.getChildren().add(boxHolder);
            WordBox box = new WordBox();
            Text letter = new Text();
            letter.setText(Character.toString(gamedata.getTargetWord().charAt(i)));
            boxHolder.getChildren().addAll(box,letter);
        }
        workspace.getChildren().add(1,littleBoxes);//add littleBoxes
    }
    public void addLittleBoxesWrong(AppTemplate app){
        littleBoxes = new HBox();
        gamedata = (GameData) app.getDataComponent();
        int numBoxes = gamedata.getTargetWord().length();

        for(int i = 0; i < numBoxes; i++){
            StackPane boxHolder = new StackPane();
            littleBoxes.getChildren().add(boxHolder);
            WordBox box = new WordBox();
            Text letter = new Text();
            letter.setText(Character.toString(gamedata.getTargetWord().charAt(i)));
            boxHolder.getChildren().addAll(box,letter);
        }
        workspace.getChildren().add(1,littleBoxes);//add littleBoxes
    }
    private void setupHandlers() {
        gui.getLoginButton().setOnMouseClicked(e -> loginScreen());
        gui.getNewProfileButton().setOnMouseClicked(e -> createProfileScreen());
        gui.getLogoutButton().setOnMouseClicked(e -> logout());
        gui.getPlayButton().setOnMouseClicked(e -> selectLevel((String)gui.getComboBox().getValue()) );
    }
    public void selectLevel(String level){
        if (level == null || level.equals("Names")){
            if(level == null)
                level = "Names";
            app.renderLvlSel(gui.getWindow(),app.getDataComponent(),app.getFileComponent(), level);
        }
        if (level.equals("Foods")){
            //potential other things
            app.renderLvlSel(gui.getWindow(),app.getDataComponent(),app.getFileComponent(), level);
        }
        if (level.equals("Animals")){
            //potential other things
            app.renderLvlSel(gui.getWindow(),app.getDataComponent(),app.getFileComponent(), level);
        }
    }
    private void gameModeScreen(String s){
        layoutGUILevelSel(s);
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
    public void initBodyParts(){
        head = false;lEye = false;rEye = false;hair = false;
        lArm = false;rArm = false;body = false;lLeg = false;
        rLeg = false;mouth = false;
    }


    /** This function reloads the entire workspace */
    @Override
    public void reloadWorkspace() {
        reinitialize();
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

    public void reinitialize() {
        guessedLetters = new HBox();
        guessedLetters.setStyle("-fx-background-color: transparent;");
        wronglyGuessedLetters = new HBox();
        wronglyGuessedLetters.setStyle("-fx-background-color: transparent;");
        workspace.getChildren().remove(littleBoxes);
        remainingGuessBox = new HBox();
        gameTextsPane = new VBox();
        gameTextsPane.getChildren().setAll(remainingGuessBox, guessedLetters, wronglyGuessedLetters);
        bodyPane.getChildren().setAll(figurePane, gameTextsPane);
        gc.clearRect(0, 0, gc.getCanvas().getWidth(),gc.getCanvas().getHeight());
        initBodyParts();
    }
    public void handleCreateProfile() throws IOException {
        PropertyManager propertyManager = PropertyManager.getManager();
        if (BuzzWordInfo == null) {
            FileChooser filechooser = new FileChooser();
            java.nio.file.Path appDirPath  = Paths.get(propertyManager.getPropertyValue(APP_TITLE)).toAbsolutePath();
            java.nio.file.Path targetPath  = appDirPath.resolve(APP_WORKDIR_PATH.getParameter());
            filechooser.setInitialDirectory(targetPath.toFile());

            filechooser.setTitle(propertyManager.getPropertyValue(CREATE_PROFILE_TITLE));
            String description = propertyManager.getPropertyValue(WORK_FILE_EXT_DESC);
            String extension   = propertyManager.getPropertyValue(WORK_FILE_EXT);
            /*FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(String.format("%s (*.%s)", description, extension),
                    String.format("*.%s", extension)); // Appends .json to name provided
            filechooser.getExtensionFilters().add(extFilter);*/
            File selectedFile = filechooser.getInitialDirectory();// filechooser.showSaveDialog(app.getGUI().getWindow());
            if (selectedFile != null)
                createProfile(selectedFile.toPath());
        } else
            createProfile(BuzzWordInfo);
    }
    private void createProfile(java.nio.file.Path target) throws IOException {
        app.getFileComponent().createProfile(app.getDataComponent(), target, userName, passWord);
        BuzzWordInfo = target;
        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
        PropertyManager           props  = PropertyManager.getManager();
        dialog.show(props.getPropertyValue(PROFILE_CREATED_TITLE), props.getPropertyValue(PROFILE_CREATED_MESSAGE));
    }
    // LOGIN TO BUZZWORD
    public void handleLogin() throws IOException {
        PropertyManager propertyManager = PropertyManager.getManager();
        if (BuzzWordInfo == null) {
            FileChooser filechooser = new FileChooser();
            java.nio.file.Path appDirPath  = Paths.get(propertyManager.getPropertyValue(APP_TITLE)).toAbsolutePath();
            java.nio.file.Path targetPath  = appDirPath.resolve(APP_WORKDIR_PATH.getParameter());
            filechooser.setInitialDirectory(targetPath.toFile());

            File selectedFile = filechooser.getInitialDirectory();// filechooser.showSaveDialog(app.getGUI().getWindow());
            if (selectedFile != null)
                login(selectedFile.toPath());
        } else
            login(BuzzWordInfo);
    }
    private void login(java.nio.file.Path target) throws IOException {
        boolean loginSuccess = app.getFileComponent().login(app.getDataComponent(), target, userName, passWord);
        BuzzWordInfo = target;
        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
        PropertyManager           props  = PropertyManager.getManager();
        if(loginSuccess) {
            dialog.show(props.getPropertyValue(LOGIN_SUCCESS_TITLE), props.getPropertyValue(LOGIN_SUCCESS_MESSAGE) + userName);
            gui.getLoginButton().setVisible(false);
            gui.getNewProfileButton().setVisible(false);
            gui.getLogoutButton().setVisible(true);
            gui.getPlayButton().setDisable(false);
            loggedIn = true;
            controller.setLoggedIn(true);
            layoutGUIHome();
        }
        else
            dialog.show(props.getPropertyValue(LOGIN_SUCCESS_TITLE), props.getPropertyValue(LOGIN_FAIL_MESSAGE));
    }
    public void logout(){
        gamedata = (GameData) app.getDataComponent();
        gamedata.reset();
        gui.getLogoutButton().setVisible(false);
        gui.getLoginButton().setVisible(true);
        gui.getNewProfileButton().setVisible(true);
        layoutGUIHome();
    }
}
class WordBox extends Rectangle{
    public WordBox(){
        setWidth(200);
        setHeight(100);

        setFill(Color.SLATEGRAY);
        setStroke(Color.WHITE);
    }
}

class Circles extends Circle{
    private DropShadow ds;
    public Circles(){
        setRadius(35);
        this.setFill(Color.STEELBLUE);
        //setFill(Color.WHITE.deriveColor(0, 1.2, 1, 0.6));
        setStroke(Color.WHITE);
        //Drop shadow props
        ds = new DropShadow();
        ds.setRadius(50);
        ds.setOffsetX(6.0);
        ds.setOffsetY(4.0);
        ds.setColor(Color.color(0.3, 0.6, 0.05));
    }

    public void removeShadow() {
        this.setEffect(null);
    }
    public void addShadow(){
        //Set circle props
        setEffect(ds);
    }
}
