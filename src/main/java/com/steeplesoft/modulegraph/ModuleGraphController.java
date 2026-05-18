package com.steeplesoft.modulegraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.steeplesoft.modulegraph.model.ModuleDefinition;
import dev.tamboui.widgets.tabs.TabsState;

public class ModuleGraphController {
    private final Logger logger = Logger.getLogger(ModuleGraphController.class.getName());
    private final ModuleXmlParser parser = new ModuleXmlParser();
    private final File moduleRoot;

    private final Map<String, ModuleDefinition> modules;
    private final MutableGraph<ModuleDefinition> graph;
    private final List<ModuleDefinition> moduleList;
    private final TabsState tabsState = new TabsState(0);

    public ModuleGraphController(String moduleDir) {
        moduleRoot = new File(moduleDir);
        if (!moduleRoot.exists()) {
            throw new IllegalArgumentException("Module directory does not exist: " + moduleDir);
        }
        modules = readModules();
        graph = createGraph();
        moduleList = modules.values().stream().sorted().toList();
    }

    public List<ModuleDefinition> getModuleList() {
        return moduleList;
    }

    public MutableGraph<ModuleDefinition> getGraph() {
        return graph;
    }

    public TabsState getTabsState() {
        return tabsState;
    }

    private Map<String, ModuleDefinition> readModules() {
        try (Stream<Path> stream = Files.walk(moduleRoot.toPath(), FileVisitOption.FOLLOW_LINKS)) {
            var modules = stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().equals("module.xml"))
                    .map(file -> {
                        try {
                            return parser.parse(file);
                        } catch (IOException | XMLStreamException e) {
                            logger.severe("Error parsing module: " + file.toAbsolutePath());
                            logger.severe(e.getMessage());
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toMap(ModuleDefinition::name, Function.identity()));
            return modules;
        } catch (IOException e) {
            return new LinkedHashMap<>();
        }
    }

    private MutableGraph<ModuleDefinition> createGraph() {
        MutableGraph<ModuleDefinition> graph = GraphBuilder.directed().allowsSelfLoops(true).build();

        modules.values().forEach(module -> {
            graph.addNode(module);
            module.dependencies().forEach(dep -> {
                ModuleDefinition target = modules.get(dep.name());
                // Some dependencies (e.g., JDK modules) are not present in the graph
                if (target != null) {
                    graph.putEdge(module, target);
                }
            });
        });

        return graph;
    }
}
