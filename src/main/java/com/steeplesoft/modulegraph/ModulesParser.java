package com.steeplesoft.modulegraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.steeplesoft.modulegraph.model.ModuleDefinition;

public class ModulesParser {
    final String moduleDir;
    private final XmlMapper mapper = new XmlMapper();
    private File moduleRoot;
    private Map<String, ModuleDefinition> modules;
    private MutableGraph<ModuleDefinition> graph;
    private final Logger logger = Logger.getLogger(ModulesParser.class.getName());

    public ModulesParser(String moduleDir) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.moduleDir = moduleDir;

        moduleRoot = new File(moduleDir);
        if (!moduleRoot.exists()) {
            throw new IllegalArgumentException("Module directory does not exist: " + moduleDir);
        }
        readModules();
    }

    public Map<String, ModuleDefinition> getModules() {
        return modules;
    }

    private void readModules() {
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

    public MutableGraph<ModuleDefinition> getGraph() {
        return graph;
    }
}
