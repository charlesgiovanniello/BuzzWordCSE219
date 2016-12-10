package gui;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Created by CharlesGiovaniello on 12/7/16.
 */
public class FXTimer extends Application {
    // private class constant and some variables
    private static final Integer STARTTIME = 60;
    private Timeline timeline;
    private Label timerLabel = new Label();
    private Integer timeSeconds = STARTTIME;


    @Override
    public void start(Stage primaryStage) throws Exception {
        timerLabel.setText(timeSeconds.toString());
        timerLabel.setTextFill(Color.RED);
        timerLabel.setStyle("-fx-font-size: 4em;");
        //
        if (timeline != null) {
            timeline.stop();
        }
        timeSeconds = STARTTIME;
        //
        timerLabel.setText(timeSeconds.toString());
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1),
                        new EventHandler() {
                            // KeyFrame event handler
                            @Override
                            public void handle(Event event) {
                                timeSeconds--;
                                // update timerLabel
                                System.out.println(timeSeconds.toString());

                                timerLabel.setText(
                                        timeSeconds.toString());
                                if (timeSeconds <= 0) {
                                    timeline.stop();
                                }
                            }
                        }));
        timeline.playFromStart();

    }
}
