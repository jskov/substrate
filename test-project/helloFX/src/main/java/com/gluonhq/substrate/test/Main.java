package com.gluonhq.substrate.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.Locale;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        Scene scene = new Scene(new StackPane(l), 640, 480);
        stage.setScene(scene);
        stage.show();

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(f -> System.exit(0));
        pause.play();
    }

    public static void main(String[] args) {
        String osName  = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        System.setProperty("prism.verbose", "true");
        if (osName.contains("mac") || osName.contains("nux")) {
            System.setProperty("prism.order", "sw");
        }
        launch(args);
    }

}