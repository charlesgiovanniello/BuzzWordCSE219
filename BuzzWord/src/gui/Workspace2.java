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
public class Workspace2 extends AppWorkspaceComponent {

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
    String            gameMode;
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
    public Workspace2(AppTemplate initApp, String gameMode) throws IOException {
        this.gameMode = gameMode;
        app = initApp;
        gamedata = (GameData)app.getDataComponent();
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

        layoutGUILevelSel(gameMode);

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
                Button btn = new Button(String.valueOf(counter));
                btn.setDisable(true);
                stack.getChildren().addAll(addMe, levelNumber, btn);
                int finalCounter = counter;
                btn.setOnMouseClicked(e -> playGame(finalCounter));
                gridpane.add(stack, j, i);
                circlesArray[j][i] = stack;
                counter++;
            }
        }
        int prog;
        if(gameMode.equals("Names")){
            prog = gamedata.getGameOneProg();
        }
        else if(gameMode.equals("Foods")){
            prog = gamedata.getGameTwoProg();
        }
        else
            prog = gamedata.getGameThreeProg();
        for(int k = 0; k < prog; k+= 0) {
            for (int i = 0; i <= 1; i++) { //row
                for (int j = 0; j <= 3; j++) { //coll
                    if(k < prog) {
                        circlesArray[j][i].getChildren().get(2).setDisable(false);
                        k++;
                    }
                }
            }
        }

        //ADD ALL WORKSPACE OBJECTS TO GUI
        workspace.getChildren().addAll(headPane,selectLevel, bodyPane,blankBoxLeft, gridpane);//add littleBox
        //CENTER THE GRIDPANE
        gridpane.setAlignment(Pos.CENTER);

    }
    private void playGame(int level){
        app.renderGamePlay(gui.getWindow(),app.getDataComponent(),app.getFileComponent(), gameMode, level);
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
