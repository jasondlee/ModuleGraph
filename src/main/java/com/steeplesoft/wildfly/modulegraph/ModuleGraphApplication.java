package com.steeplesoft.wildfly.modulegraph;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ModuleGraphApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // https://www.flaticon.com/free-icons/graph
        Image image = new Image("icon.png");
        stage.getIcons().add(image);

        FXMLLoader fxmlLoader = new FXMLLoader(ModuleGraphApplication.class.getResource("/window.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        stage.setTitle("WildFly Module Graph Analyzer");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }
}
