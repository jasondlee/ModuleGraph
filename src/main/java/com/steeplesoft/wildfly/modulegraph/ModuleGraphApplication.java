package com.steeplesoft.wildfly.modulegraph;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ModuleGraphApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ModuleGraphApplication.class.getResource("window.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        stage.setTitle("Hello!");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }
}
