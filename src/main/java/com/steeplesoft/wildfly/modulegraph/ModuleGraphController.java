package com.steeplesoft.wildfly.modulegraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.steeplesoft.wildfly.modulegraph.model.ModuleDefinition;
import com.steeplesoft.wildfly.modulegraph.model.ModuleDependency;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class ModuleGraphController {
    private final XmlMapper mapper;
    private final String PREFERENCE_NODE_NAME = "com.coderscratchpad.javafx.preferences";
    private final Preferences preferences = Preferences.userRoot().node(PREFERENCE_NODE_NAME);
//    private final Node folderIcon = new ImageView(
//        new Image(getClass().getResourceAsStream("folder_16.png"))
//    );

    private File moduleRoot;
    private Map<String, ModuleDefinition> modules;
    private Graph<ModuleDefinition, DefaultEdge> graph;

    @FXML
    private Label moduleName;
    @FXML
    private Label moduleVersion;
    @FXML
    private Label mainClass;
    @FXML
    private ListView<String> resourceList;
    @FXML
    private TableView<ModuleDependency> dependencyTable;

    @FXML
    private TableColumn<ModuleDependency, String> moduleNameColumn;
    @FXML
    private TableColumn<ModuleDependency, String> moduleExportColumn;
    @FXML
    private TableColumn<ModuleDependency, String> moduleServicesColumn;
    @FXML
    private TableColumn<ModuleDependency, String> moduleOptionalColumn;

    @FXML
    private TreeView<ModuleDefinition> moduleTree;

    public ModuleGraphController() {
        mapper = new XmlMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @FXML
    private void initialize() {
        moduleTree.setShowRoot(false);

        moduleNameColumn.setCellValueFactory(new ModuleDefinitionValueFactory("name"));
        moduleExportColumn.setCellValueFactory(new ModuleDefinitionValueFactory("export"));
        moduleServicesColumn.setCellValueFactory(new ModuleDefinitionValueFactory("services"));
        moduleOptionalColumn.setCellValueFactory(new ModuleDefinitionValueFactory("optional"));

        String moduleDir = preferences.get("moduleRoot", null);
        if (moduleDir == null) {
            openModuleRoot(null);
        } else {
            moduleRoot = new File(moduleDir);
            populateTree();
        }
    }

    @FXML
    private void onModuleClicked(MouseEvent event) {
        var moduleDef = moduleTree.getSelectionModel().getSelectedItem().getValue();
        moduleName.setText(moduleDef.getName());
        moduleVersion.setText(moduleDef.version);
        mainClass.setText(moduleDef.mainClass);

        populateResourceList(moduleDef);
        populateDependencyTable(moduleDef);
    }

    @FXML
    private void onDependencyClicked(MouseEvent event) {
        var moduleDef = dependencyTable.getSelectionModel().getSelectedItem();
        moduleTree.getRoot().getChildren().stream()
            .filter(item -> item.getValue().name.equals(moduleDef.name))
            .findFirst()
            .ifPresent(item -> moduleTree.getSelectionModel().select(item));
        moduleTree.scrollTo(moduleTree.getSelectionModel().getSelectedIndex());
        onModuleClicked(event);
    }

    @FXML
    private void openModuleRoot(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();

        // Set title for the file dialog
        chooser.setTitle("Open Module Root");
        if (moduleRoot != null) {
            chooser.setInitialDirectory(moduleRoot);
        }

        // Show the file dialog and get the selected file
        File selectedDir = chooser.showDialog(moduleName.getScene().getWindow());

        if (selectedDir != null && selectedDir.exists() && selectedDir.isDirectory()) {
            moduleRoot = selectedDir;
            preferences.put("moduleRoot", moduleRoot.getAbsolutePath());
            populateTree();
        }

/*
        var dialog = new TextInputDialog();
        dialog.setTitle("Open Module Root");
        dialog.setHeaderText("Enter some text, or use default value.");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            moduleRoot = result.get();
            preferences.put("moduleRoot", moduleRoot);
            populateTree();
        }
*/
    }

    private void populateTree() {
        getModules();
        graph = createGraph();

        var root = new ModuleDefinitionTreeItem(new ModuleDefinition("root"));

        modules.values().stream()
            .sorted()
            .forEach(module -> root.getChildren().add(new ModuleDefinitionTreeItem(module)));


        moduleTree.setRoot(root);
    }

    private void populateResourceList(ModuleDefinition moduleDef) {
        resourceList.getItems().clear();
        moduleDef.resources.forEach(artifact -> {
            String name = artifact.name;
            if (name == null) {
                name = artifact.path;
            }
            if (name != null) {
                resourceList.getItems().add(name);
            }
        });
    }

    private void populateDependencyTable(ModuleDefinition moduleDef) {
        dependencyTable.getItems().clear();
        moduleDef.dependencies.forEach(dep -> dependencyTable.getItems().add(dep));
    }

    private void getModules() {
        try (Stream<Path> stream = Files.walk(moduleRoot.toPath())) {
            modules = stream
                .filter(Files::isRegularFile)
                .filter(file -> file.getFileName().toString().equals("module.xml"))
                .map(file -> {
                    try {
                        return mapper.readValue(file.toFile(), ModuleDefinition.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toMap(ModuleDefinition::getName, Function.identity()));
        } catch (IOException e) {
            modules =  new LinkedHashMap<>();
        }
    }

    private Graph<ModuleDefinition, DefaultEdge> createGraph() {
        Graph<ModuleDefinition, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        modules.values().forEach(g::addVertex);

        modules.values().forEach(module -> {
            module.dependencies.forEach(dep -> {
                ModuleDefinition target = modules.get(dep.name);
                if (target != null) {
                    g.addEdge(module, target);
                    g.addEdge(target, module);
                }
            });
        });

        return g;
    }

}
