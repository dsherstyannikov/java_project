package com.example;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * @author Deepak Mishra
 */
public class Main extends Application {

    private Stage primaryStage;

    /**
     * @return Object of type <code>Stage</code>
     */
    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    /**
     * @param primaryStage
     *            Sets primaryStage as Stage in Application
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Scene defaultScene = getDefaultScene("Click here to open");
        primaryStage.setTitle("To Do List");
        primaryStage.setScene(defaultScene);
        defaultScene.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                Scene toDoScene = changeScene("Hello JavaFX");
                Stage rootStage = getPrimaryStage();
                rootStage.setScene(toDoScene);
                rootStage.show();

            }
        });
        primaryStage.centerOnScreen();
        setPrimaryStage(primaryStage);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * @param defaultText
     *  sets it as welcome String during start of Application
     * @return <code>Scene</code> object with <code>defaultText</code> displayed at its center.
     */
    public Scene getDefaultScene(String defaultText) {
        final Group rootGroup = new Group();
        final Scene scene = new Scene(rootGroup, 600, 400, Color.WHITE);

        // A temp variable is used just to get the bounds of defaultText after
        // setting its font and font size so that it can be positioned perfectly
        // on the screen at its center.
        Text temp = new Text(defaultText);
        temp.setFill(Color.BLACK);
        temp.setFont(Font.font(java.awt.Font.SANS_SERIF, 20));
        Text txtDefaultText = new Text(scene.getWidth() / 2 - ((temp.getBoundsInLocal().getMaxX() - temp.getBoundsInLocal().getMinX()) / 2),
                scene.getHeight() / 2, defaultText);
        txtDefaultText.setFill(Color.BLACK);
        txtDefaultText.setFont(Font.font(java.awt.Font.SANS_SERIF, 25));

        rootGroup.getChildren().add(txtDefaultText);

        return scene;
    }

    public Scene changeScene(String text) {
        BorderPane borderPane = new BorderPane();

        Text hello = new Text(text);
        borderPane.setCenter(hello);
        Scene scene = new Scene(borderPane, 600, 400, Color.YELLOW);
        return scene;
    }

}
