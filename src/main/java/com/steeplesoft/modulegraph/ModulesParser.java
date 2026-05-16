package com.steeplesoft.modulegraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.steeplesoft.modulegraph.model.ModuleDefinition;

public class ModulesParser {
    private final Logger logger = Logger.getLogger(ModulesParser.class.getName());
    private final ModuleXmlParser parser = new ModuleXmlParser();
    private final File moduleRoot;

    private Map<String, ModuleDefinition> modules;
    private MutableGraph<ModuleDefinition> graph;

    public ModulesParser(String moduleDir) {
        moduleRoot = new File(moduleDir);
        if (!moduleRoot.exists()) {
            throw new IllegalArgumentException("Module directory does not exist: " + moduleDir);
        }
        readModules();
    }

    public Map<String, ModuleDefinition> getModules() {
        return modules;
    }

    public MutableGraph<ModuleDefinition> getGraph() {
        return graph;
    }

    private void readModules() {
        try (Stream<Path> stream = Files.walk(moduleRoot.toPath(), FileVisitOption.FOLLOW_LINKS)) {
            modules = stream
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
            module.dependencies().forEach(dep -> {
                ModuleDefinition target = modules.get(dep.name());
                // Some dependencies (e.g., JDK modules) are not present in the graph
                if (target != null) {
                    graph.putEdge(module, target);
                }
            });
        });
    }
}
