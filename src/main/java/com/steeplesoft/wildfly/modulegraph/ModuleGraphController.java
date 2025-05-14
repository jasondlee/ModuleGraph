package com.steeplesoft.wildfly.modulegraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.steeplesoft.wildfly.modulegraph.model.ModuleDefinition;
import com.steeplesoft.wildfly.modulegraph.model.ModuleDependency;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;

@SuppressWarnings("UnstableApiUsage")
public class ModuleGraphController {
    private final XmlMapper mapper;
    private final Preferences preferences = Preferences.userRoot().node("com/steeplesoft/wildfly/modulegraph");

    private File moduleRoot;
    private Map<String, ModuleDefinition> modules;
    @SuppressWarnings("UnstableApiUsage")
    private MutableGraph<ModuleDefinition> graph;
    private final ArrayDeque<String> backStack = new ArrayDeque<>();
    private String moduleFilter;

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
    private ListView<String> dependentList;
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
    @FXML
    private TextField moduleFilterTextField;

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

        moduleFilterTextField.textProperty().addListener((obs, oldText, newText) -> {
            moduleFilter = newText;
            populateTree();
        });

        String moduleDir = preferences.get("moduleRoot", null);
        if (moduleDir != null) {
            moduleRoot = new File(moduleDir);
            getModules();
            populateTree();
        }
    }

    @FXML
    private void openModuleRoot(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();

        chooser.setTitle("Open Module Root");
        if (moduleRoot != null) {
            chooser.setInitialDirectory(moduleRoot);
        }

        File selectedDir = chooser.showDialog(moduleTree.getScene().getWindow());
        if (selectedDir != null && selectedDir.exists() && selectedDir.isDirectory()) {
            moduleRoot = selectedDir;
            preferences.put("moduleRoot", moduleRoot.getAbsolutePath());
            getModules();
            populateTree();
        }
    }

    @FXML
    private void onModuleClicked(MouseEvent event) {
        var moduleDef = moduleTree.getSelectionModel().getSelectedItem().getValue();
        pushModuleToBackStack(moduleDef);
        populateMetadata();
    }

    private void pushModuleToBackStack(ModuleDefinition moduleDef) {
        if (!Objects.equals(backStack.peek(), moduleDef.name)) {
            backStack.push(moduleDef.name);
        }
    }

    @FXML
    private void onDependencyClicked(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            pushModuleToBackStack(moduleTree.getSelectionModel().getSelectedItem().getValue());
            navigateToModule(dependencyTable.getSelectionModel().getSelectedItem().name);
        }
    }

    @FXML
    private void onDependentClicked(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            pushModuleToBackStack(moduleTree.getSelectionModel().getSelectedItem().getValue());
            navigateToModule(dependentList.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    private void onBackClicked(MouseEvent event) {
        if (!backStack.isEmpty()) {
            var current = moduleTree.getSelectionModel().getSelectedItem().getValue().name;
            var back = backStack.pop();
            while (!backStack.isEmpty() && Objects.equals(current, back)) {
                back = backStack.pop();
            }
            navigateToModule(back);
        }
    }

    private void populateMetadata() {
        var moduleDef = moduleTree.getSelectionModel().getSelectedItem().getValue();
        moduleName.setText(moduleDef.getName());
        moduleVersion.setText(moduleDef.version);
        mainClass.setText(moduleDef.mainClass);

        populateResourceList(moduleDef);
        populateDependencyTable(moduleDef);
        populateDependentsList(moduleDef);
    }

    private void navigateToModule(String moduleDef) {
        moduleTree.getRoot().getChildren().stream()
            .filter(item -> item.getValue().name.equals(moduleDef))
            .findFirst()
            .ifPresent(item -> moduleTree.getSelectionModel().select(item));
        moduleTree.scrollTo(moduleTree.getSelectionModel().getSelectedIndex());
        populateMetadata();
    }

    private void populateTree() {
        var root = new ModuleDefinitionTreeItem(new ModuleDefinition("root"));

        modules.values().stream()
            .filter(module -> {
                if (moduleFilter != null && !modules.isEmpty()) {
                    return module.name.toLowerCase(Locale.ROOT).contains(moduleFilter.toLowerCase(Locale.ROOT));
                } else {
                    return true;
                }
            })
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

    private void populateDependentsList(ModuleDefinition moduleDef) {
        dependentList.getItems().clear();
        dependentList.getItems().addAll(graph.incidentEdges(moduleDef).stream()
            .map(pair -> pair.source().name)
            .distinct()
            .sorted()
            .toList());
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
            createGraph();
        } catch (IOException e) {
            modules = new LinkedHashMap<>();
        }
    }

    private void createGraph() {
        graph = GraphBuilder.directed().allowsSelfLoops(true).build();
        // Add all modules as vertices first
        modules.values().forEach(module -> graph.addNode(module));

        // Add outgoing edges (module -> dependency)
        modules.values().forEach(module -> {
            module.dependencies.forEach(dep -> {
                ModuleDefinition target = modules.get(dep.name);
                // Some dependencies (e.g., JDK modules) are not present in the graph
                if (target != null) {
                    graph.putEdge(module, target);
                }
            });
        });
    }

/*
    public static class DependencyEdge extends DefaultEdge {
        @Override
        public ModuleDefinition getSource() {
            return (ModuleDefinition) super.getSource();
        }

        @Override
        public ModuleDefinition getTarget() {
            return (ModuleDefinition) super.getTarget();
        }
    }
*/
}
