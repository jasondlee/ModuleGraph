/// usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+
//DEPS org.openjfx:javafx-controls:23.0.1
//DEPS org.openjfx:javafx-fxml:23.0.1
//DEPS com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.18.3
//DEPS com.google.guava:guava:33.4.8-jre
//FILES resources/icon.png resources/window.fxml
//SOURCES ModuleDefinitionTreeItem.java ModuleDefinitionValueFactory.java ModuleGraphController.java
//SOURCES model/Artifact.java model/Condition.java model/Dependency.java  model/Filter.java model/ModuleDefinition.java model/ModuleDependency.java model/PathSet.java model/PathSpec.java model/Property.java

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
