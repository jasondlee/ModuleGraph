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
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.bindings.ActionHandler;
import dev.tamboui.tui.bindings.BindingSets;
import dev.tamboui.tui.bindings.KeyTrigger;
import dev.tamboui.tui.event.KeyCode;
import dev.tamboui.widgets.table.Row;
import dev.tamboui.widgets.tabs.TabsState;

public class ModuleGraphView implements Element { //extends ToolkitApp {
    private final ListElement<?> listElement = list().highlightSymbol("> ")
            .highlightColor(Color.YELLOW)
            .autoScroll()
            .scrollbar()
            .scrollbarThumbColor(Color.CYAN);
    private final ModuleGraphController controller;

    public ModuleGraphView(ModuleGraphController controller) {
        this.controller = controller;

        controller.getModuleList().forEach(module -> {
            listElement.add(module.name());
        });
    }

    @Override
    public void render(Frame frame, Rect area, RenderContext context) {
        column(
                titlePanel(),
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

    private Panel titlePanel() {
        return panel(
                text("JBoss Modules Analyzer")
                        .bold()
                        .cyan()
        )
                .rounded()
                .borderColor(Color.CYAN)
                .length(3);
    }

    private Panel navigationPanel() {
        return panel(listElement)
                .title("JBoss Modules (" + controller.getModuleList().size() + " items)")
                .rounded()
                .borderColor(Color.WHITE)
                .id("nav-panel")
                .focusable()
                .constraint(Constraint.percentage(25));
    }

    private Element infoPanel() {
        ModuleDefinition module = controller.getModuleList().get(listElement.selected());

        return column(
                text("Module Information")
                        .bold()
                        .cyan(),
                text("Module Name:    " + module.name()),
                text("Module Version: " + (module.version().isBlank() ? "-" : module.version())),
                text("Main Class:     " + ((module.mainClass() == null) ? "-" : module.mainClass())),
                text("Module Alias:   " + ((module.alias() == null) ? "-" : module.alias())),
                spacer(1),
                tabs("Resources", "Dependencies", "Dependents")
                        .id("tabs")
                        .focusable()
                        .state(controller.getTabsState()),
                renderTabPanel(module).fill()
        ).fill();
    }

    private Panel renderTabPanel(ModuleDefinition module) {
        var panel = switch (controller.getTabsState().selected()) {
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
                        .id("table")
                        .focusable()
                        .borderColor(Color.GRAY)
        )
                .id("dependencies-panel");
    }

    private Panel resourcesPanel(ModuleDefinition module) {
        ListElement<?> listElement = list()
                .autoScroll()
                .scrollbar()
                .scrollbarThumbColor(Color.CYAN);
        module.resources()
                .stream().filter(artifact -> artifact.name() != null)
                .forEach(dep ->
                        listElement.add(dep.name()));

        return panel(listElement)
                .id("resources-panel");
    }

    private Panel dependentsPanel(ModuleDefinition module) {
        ListElement<?> listElement = list()
                .autoScroll()
                .scrollbar()
                .scrollbarThumbColor(Color.CYAN);
        controller.getGraph().incidentEdges(module).stream()
                .map(pair -> pair.source().name())
                .distinct()
                .sorted()
                .toList()
                .forEach(listElement::add);

        return panel(listElement)
                .id("dependents-panel");
    }

    private Panel footerPanel() {
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
}
