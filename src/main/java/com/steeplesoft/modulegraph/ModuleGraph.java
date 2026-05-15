package com.steeplesoft.modulegraph;

import static dev.tamboui.toolkit.Toolkit.column;
import static dev.tamboui.toolkit.Toolkit.list;
import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.row;
import static dev.tamboui.toolkit.Toolkit.spacer;
import static dev.tamboui.toolkit.Toolkit.table;
import static dev.tamboui.toolkit.Toolkit.tabs;
import static dev.tamboui.toolkit.Toolkit.text;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.steeplesoft.modulegraph.model.ModuleDefinition;
import dev.tamboui.layout.Constraint;
import dev.tamboui.layout.Rect;
import dev.tamboui.style.Color;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;
import dev.tamboui.toolkit.app.ToolkitRunner;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.element.RenderContext;
import dev.tamboui.toolkit.element.Size;
import dev.tamboui.toolkit.elements.ListElement;
import dev.tamboui.toolkit.elements.Panel;
import dev.tamboui.toolkit.event.EventResult;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.bindings.ActionHandler;
import dev.tamboui.tui.bindings.BindingSets;
import dev.tamboui.tui.bindings.KeyTrigger;
import dev.tamboui.tui.event.KeyCode;
import dev.tamboui.tui.event.KeyEvent;
import dev.tamboui.tui.event.MouseEvent;
import dev.tamboui.widgets.table.Row;
import dev.tamboui.widgets.tabs.TabsState;

public class ModuleGraph implements Element { //extends ToolkitApp {
    private final ListElement<?> listElement = list().highlightSymbol("> ")
            .highlightColor(Color.YELLOW)
            .autoScroll()
            .scrollbar()
            .scrollbarThumbColor(Color.CYAN);
    private final TabsState tabsState = new TabsState(0);
    private final Logger logger;

    private ModulesParser modulesParser;
    private List<ModuleDefinition> modules;

    public ModuleGraph() {
        try (InputStream is =
                     getClass().getClassLoader()
                             .getResourceAsStream("logging.properties")) {

            LogManager.getLogManager().readConfiguration(is);
            logger = Logger.getLogger(ModuleGraph.class.getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        // TODO: aesh
        new ModuleGraph().run(args[0]);
    }

    private static Panel footerPanel() {
        var keys = new LinkedHashMap<String, String>();
        keys.put("Up/Down", "Navigate");
        keys.put("F1", "Resources Tab");
        keys.put("F2", "Dependencies Tab");
        keys.put("F3", "Dependents Tab");
        keys.put("q", "Quit");
        return panel(
                () -> row(
                        keys.entrySet().stream()
                                .map(e -> text("[" + e.getKey() + "] " + e.getValue() + " "))
                                .toArray(Element[]::new)
                )
        ).rounded().borderColor(Color.DARK_GRAY).length(3);
    }

    private static Panel generateTitlePanel() {
        return panel(
                text("JBoss Modules Analyzer")
                        .bold()
                        .cyan()
        )
                .rounded()
                .borderColor(Color.CYAN)
                .length(3);
    }

    public void run(String moduleDir) throws Exception {
        modulesParser = new ModulesParser(moduleDir);
        modules = modulesParser.getModules().values().stream().sorted().toList();
        modules.forEach(module -> {
            listElement.add(module.name());
        });

        // Create bindings with F1/F2 for tab switching
        var bindings = BindingSets.standard()
                .toBuilder()
                .bind(KeyTrigger.key(KeyCode.F1), "selectResourcesTab")
                .bind(KeyTrigger.key(KeyCode.F2), "selectDependenciesTab")
                .bind(KeyTrigger.key(KeyCode.F3), "selectDependentsTab")
                .build();

        var config = TuiConfig.builder()
                .mouseCapture(true)
                .noTick()
                .build();

        try (var runner = ToolkitRunner.builder()
                .config(config)
                .bindings(bindings)
                .withAutoBindingRegistration()
                .build()) {

            // Register global handler for tab switching
            var globalHandler = new ActionHandler(bindings)
                    .on("selectResourcesTab", e -> tabsState.select(0))
                    .on("selectDependenciesTab", e -> tabsState.select(1))
                    .on("selectDependentsTab", e -> tabsState.select(2));
            runner.eventRouter().addGlobalHandler(globalHandler);

            runner.run(() -> this);
        }
    }

    @Override
    public void render(Frame frame, Rect area, RenderContext context) {
        column(
                generateTitlePanel(),
                row(
                        navigationPanel(),
                        infoPanel()
                ).fill(),
                footerPanel()
        ).render(frame, area, context);
    }

    @Override
    public Size preferredSize(int availableWidth, int availableHeight, RenderContext context) {
        return Size.UNKNOWN;
    }

    private Panel navigationPanel() {
        return panel(listElement)
                .title("JBoss Modules (" + modules.size() + " items)")
                .rounded()
                .borderColor(Color.WHITE)
                .id("list-panel")
                .focusable()
//                .onKeyEvent(this::handleKey)
                .constraint(Constraint.percentage(25));
    }

    private EventResult handleMouse(MouseEvent mouseEvent) {
        logger.info(mouseEvent.toString());
        return EventResult.UNHANDLED;
    }

    private EventResult handleKey1(KeyEvent event) {
//        if (event.isUp()) {
//            listElement.selectPrevious();
//            return EventResult.HANDLED;
//        }
//        if (event.isDown()) {
//            listElement.selectNext(1);
//            return EventResult.HANDLED;
//        }
        return EventResult.UNHANDLED;
    }

    private Panel infoPanel() {
        ModuleDefinition module = modules.get(listElement.selected());
        var version = module.version().isBlank() ? "-" : module.version();
        var mainClass = (module.mainClass() == null) ? "-" : module.mainClass();

        return panel(
                text("Module Information")
                        .bold()
                        .cyan(),
                text("Module Name:    " + module.name()),
                text("Module Version: " + version),
                text("Main Class:     " + mainClass),
                spacer(1),
                tabs("Resources", "Dependencies", "Dependents")
                        .state(tabsState)
                        .focusable()
                        .id("tabs"),
                renderTabPanel()
        )
                .borderless()
                .id("info-panel")
                .fill();
    }

    private Panel renderTabPanel() {
        ModuleDefinition module = modules.get(listElement.selected());
        var panel = switch (tabsState.selected()) {
            case 1 -> dependenciesPanel(module);
            case 2 -> dependentsPanel(module);
            default -> resourcesPanel(module);
        };

        return panel
                .focusable()
                .fill();
    }

    private Panel dependenciesPanel(ModuleDefinition module) {
        return panel(
                table()
                        .header(Row.from("Module Name", "Export", "Services", "Optional"))
                        .rows(
                                module.dependencies().stream()
                                        // TODO: fix bug in parser. This should never be null
                                        .filter(dependency -> dependency.name() != null)
                                        .map(dependency -> Row.from(
                                                dependency.name(),
                                                "" + dependency.export(),
                                                dependency.services(),
                                                "" + dependency.optional()
                                        )).toList()
                        )
                        .widths(
                                Constraint.fill(3),
                                Constraint.fill(1),
                                Constraint.fill(1),
                                Constraint.fill(1)
                        )
                        .highlightStyle(Style.EMPTY.bg(Color.DARK_GRAY))
                        .title("Module Information")
        );
    }

    private Panel resourcesPanel(ModuleDefinition module) {
        ListElement<?> listElement = list()
                .autoScroll()
                .scrollbar()
                .scrollbarThumbColor(Color.CYAN);
        module.resources()
                .stream().filter(artifact -> artifact.name() != null)
                .forEach(dep -> listElement.add(dep.name()));

        return panel(listElement);
    }

    private Panel dependentsPanel(ModuleDefinition module) {
        ListElement<?> listElement = list()
                .autoScroll()
                .scrollbar()
                .scrollbarThumbColor(Color.CYAN);
        modulesParser.getGraph().incidentEdges(module).stream()
                .map(pair -> pair.source().name())
                .distinct()
                .sorted()
                .toList()
                .forEach(listElement::add);

        return panel(listElement);
    }
}
