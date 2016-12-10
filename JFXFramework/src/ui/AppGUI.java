package ui;

import apptemplate.AppTemplate;
import components.AppStyleArbiter;
import controller.FileController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;
import propertymanager.PropertyManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import static settings.AppPropertyType.*;
import static settings.InitializationParameters.APP_IMAGEDIR_PATH;

/**
 * This class provides the basic user interface for this application, including all the file controls, but it does not
 * include the workspace, which should be customizable and application dependent.
 *
 * @author Richard McKenna, Ritwik Banerjee, Charles Giovanniello
 */
public class AppGUI implements AppStyleArbiter {

    protected FileController fileController;   // to react to file-related controls
    protected Stage          primaryStage;     // the application window
    protected Scene          primaryScene;     // the scene graph
    protected BorderPane     appPane;          // the root node in the scene graph, to organize the containers
    protected VBox           toolbarPane;      // the top toolbar
    protected Button         newProfileButton;        // button to create a new instance of the application
    protected Button         loginButton;
    protected Button         saveButton;       // button to save progress on application
    protected Button         loadButton;       // button to load a saved game from (json) file
    protected Button         exitButton;       // button to exit application
    protected Button         playButton;
    protected Button         homeButton;
    protected Button         profileButton;
    protected Button         HomeButton;
    protected Button         logoutButton;
    protected String         applicationTitle; // the application title
    protected ComboBox           comboBox;
    protected ObservableList<String> options;

    private int appWindowWidth;  // optional parameter for window width that can be set by the application
    private int appWindowHeight; // optional parameter for window height that can be set by the application
    
    /**
     * This constructor initializes the file toolbar for use.
     *
     * @param initPrimaryStage The window for this application.
     * @param initAppTitle     The title of this application, which
     *                         will appear in the window bar.
     * @param app              The app within this gui is used.
     */
    public AppGUI(Stage initPrimaryStage, String initAppTitle, AppTemplate app) throws IOException, InstantiationException {
        this(initPrimaryStage, initAppTitle, app, -1, -1);
    }

    public AppGUI(Stage primaryStage, String applicationTitle, AppTemplate appTemplate, int appWindowWidth, int appWindowHeight) throws IOException, InstantiationException {
        this.appWindowWidth = appWindowWidth;
        this.appWindowHeight = appWindowHeight;
        this.primaryStage = primaryStage;
        this.applicationTitle = applicationTitle;
        initializeToolbar();                    // initialize the top toolbar
        initializeToolbarHandlers(appTemplate); // set the toolbar button handlers
        initializeWindow();                     // start the app window (without the application-specific workspace)

    }

    public FileController getFileController() {
        return this.fileController;
    }

    public VBox getToolbarPane() { return toolbarPane; }

    public BorderPane getAppPane() { return appPane; }
    
    /**
     * Accessor method for getting this application's primary stage's,
     * scene.
     *
     * @return This application's window's scene.
     */
    public Scene getPrimaryScene() { return primaryScene; }
    
    /**
     * Accessor method for getting this application's window,
     * which is the primary stage within which the full GUI will be placed.
     *
     * @return This application's primary stage (i.e. window).
     */
    public Stage getWindow() { return primaryStage; }
    public ComboBox getComboBox(){return comboBox;}
    public Button getPlayButton(){return playButton;}
    public Button getNewProfileButton() {return newProfileButton;}
    public Button getLoginButton(){return loginButton;}
    public Button getHomeButton() {return homeButton;}
    public Button getProfileButton() {return profileButton;}
    public Button getExitButton() {return exitButton;}
    public Button getLogoutButton() {return logoutButton;}
    public ObservableList<String> getOptions(){return options;}

    /**
     * This function initializes all the buttons in the toolbar at the top of
     * the application window. These are related to file management.
     */

    private void initializeToolbar() throws IOException {
        toolbarPane = new VBox(10);
        newProfileButton = initializeChildButton(toolbarPane, NEW_ICON.toString(), NEW_TOOLTIP.toString(), false);
        loginButton = initializeChildButton(toolbarPane, LOGIN_ICON.toString(), LOGIN_TOOLTIP.toString(), false);
        logoutButton = initializeChildButton(toolbarPane, LOGOUT_ICON.toString(), LOGOUT_TOOLTIP.toString(), false);
        playButton = initializeChildButton(toolbarPane, PLAY_ICON.toString(), PLAY_TOOLTIP.toString(), false);
        homeButton = initializeChildButton(toolbarPane, HOME_ICON.toString(), HOME_TOOLTIP.toString(), false);
        profileButton = initializeChildButton(toolbarPane, PROFILE_ICON.toString(), PROFILE_TOOLTIP.toString(), false);
        logoutButton.setVisible(false);

        options =
                FXCollections.observableArrayList(
                        "Names",
                        "Foods",
                        "Animals"
                );
        comboBox = new ComboBox(options);
        HBox blankBox  = new HBox();
        blankBox.setPrefHeight(75);
        toolbarPane.getChildren().addAll(comboBox,blankBox);
        toolbarPane.setAlignment(Pos.BASELINE_CENTER);
        //comboBox.setPromptText("Select mode");
        comboBox.setPromptText(options.get(0));

        //*UNUSED*
        loadButton = initializeChildButton(toolbarPane, LOAD_ICON.toString(), LOAD_TOOLTIP.toString(), false);
        saveButton = initializeChildButton(toolbarPane, SAVE_ICON.toString(), SAVE_TOOLTIP.toString(), true);
        exitButton = initializeChildButton(toolbarPane, EXIT_ICON.toString(), EXIT_TOOLTIP.toString(), false);

        loadButton.setVisible(false);
        saveButton.setVisible(false);
        //exitButton.setVisible(false);
    }

    private void initializeToolbarHandlers(AppTemplate app) throws InstantiationException {
        try {
            Method         getFileControllerClassMethod = app.getClass().getMethod("getFileControllerClass");
            String         fileControllerClassName      = (String) getFileControllerClassMethod.invoke(app);
            Class<?>       klass                        = Class.forName("controller." + fileControllerClassName);
            Constructor<?> constructor                  = klass.getConstructor(AppTemplate.class);
            fileController = (FileController) constructor.newInstance(app);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        newProfileButton.setOnAction(e -> fileController.handleNewRequest());
        saveButton.setOnAction(e -> {
            try {
                fileController.handleSaveRequest();
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(1);
            }
        });
        loadButton.setOnAction(e -> {
            try {
                fileController.handleLoadRequest();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        exitButton.setOnAction(e -> fileController.handleExitRequest());
    }

    public void updateWorkspaceToolbar(boolean savable) {
        saveButton.setDisable(!savable);
        newProfileButton.setDisable(false);
        exitButton.setDisable(false);
    }

    private void initializeWindow() throws IOException {
        PropertyManager propertyManager = PropertyManager.getManager();

        // SET THE WINDOW TITLE
        primaryStage.setTitle(applicationTitle);

        // add the toolbar to the constructed workspace
        appPane = new BorderPane();

        appPane.setLeft(toolbarPane);
        primaryScene = appWindowWidth < 1 || appWindowHeight < 1 ? new Scene(appPane)
                                                                 : new Scene(appPane,
                                                                             appWindowWidth,
                                                                             appWindowHeight);

        URL imgDirURL = AppTemplate.class.getClassLoader().getResource(APP_IMAGEDIR_PATH.getParameter());
        if (imgDirURL == null)
            throw new FileNotFoundException("Image resrouces folder does not exist.");
        try (InputStream appLogoStream = Files.newInputStream(Paths.get(imgDirURL.toURI()).resolve(propertyManager.getPropertyValue(APP_LOGO)))) {
            primaryStage.getIcons().add(new Image(appLogoStream));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //primaryScene.addEventFilter(MouseEvent.ANY, e -> System.out.println( e)); //HELPER
        primaryScene.addEventFilter(MouseEvent.DRAG_DETECTED , new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                primaryScene.startFullDrag();
            }
        });

        primaryStage.setScene(primaryScene);
        primaryStage.show();
        //fileController.handleNewRequest();
    }
    
    /**
     * This is a public helper method for initializing a simple button with
     * an icon and tooltip and placing it into a toolbar.
     *
     * @param toolbarPane Toolbar pane into which to place this button.
     * @param icon        Icon image file name for the button.
     * @param tooltip     Tooltip to appear when the user mouses over the button.
     * @param disabled    true if the button is to start off disabled, false otherwise.
     * @return A constructed, fully initialized button placed into its appropriate
     * pane container.
     */
    public Button initializeChildButton(Pane toolbarPane, String icon, String tooltip, boolean disabled) throws IOException {
        PropertyManager propertyManager = PropertyManager.getManager();

        URL imgDirURL = AppTemplate.class.getClassLoader().getResource(APP_IMAGEDIR_PATH.getParameter());
        if (imgDirURL == null)
            throw new FileNotFoundException("Image resources folder does not exist.");

        Button button = new Button();
        try (InputStream imgInputStream = Files.newInputStream(Paths.get(imgDirURL.toURI()).resolve(propertyManager.getPropertyValue(icon)))) {
            Image buttonImage = new Image(imgInputStream);
            button.setDisable(disabled);
            button.setGraphic(new ImageView(buttonImage));
            Tooltip buttonTooltip = new Tooltip(propertyManager.getPropertyValue(tooltip));
            button.setTooltip(buttonTooltip);
            toolbarPane.getChildren().add(button);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return button;
    }
    
    /**
     * This function specifies the CSS style classes for the controls managed
     * by this framework.
     */
    @Override
    public void initStyle() {
        // currently, we do not provide any stylization at the framework-level
    }
}
