package com.steeplesoft.modulegraph;

import java.io.InputStream;
import java.time.Duration;
import java.util.logging.LogManager;

import com.steeplesoft.modulegraph.view.ModuleGraphView;
import dev.tamboui.toolkit.app.ToolkitRunner;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.bindings.ActionHandler;
import dev.tamboui.tui.bindings.BindingSets;
import dev.tamboui.tui.bindings.KeyTrigger;
import dev.tamboui.tui.event.KeyCode;

public class ModuleGraph {
    public static void main(String[] args) throws Exception {
        try (InputStream is = ModuleGraph.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        }

        var controller = new ModuleGraphController(args[0]);
        var view = new ModuleGraphView(controller);

        // Create bindings with F1/F2 for tab switching
        var bindings = BindingSets.standard()
                .toBuilder()
                .bind(KeyTrigger.key(KeyCode.F1), "selectResourcesTab")
                .bind(KeyTrigger.key(KeyCode.F2), "selectDependenciesTab")
                .bind(KeyTrigger.key(KeyCode.F3), "selectDependentsTab")
                .build();

        try (var runner = ToolkitRunner.builder()
                .config(TuiConfig.builder()
                        .tickRate(Duration.ofMillis(50))
                        .build())
                .bindings(bindings)
                .withAutoBindingRegistration()
                .build()) {

            // Register global handler for tab switching
            var globalHandler = new ActionHandler(bindings)
                    .on("selectResourcesTab", e -> controller.getTabsState().select(0))
                    .on("selectDependenciesTab", e -> controller.getTabsState().select(1))
                    .on("selectDependentsTab", e -> controller.getTabsState().select(2));
            runner.eventRouter().addGlobalHandler(globalHandler);

            runner.run(() -> view);
        }
    }
}
