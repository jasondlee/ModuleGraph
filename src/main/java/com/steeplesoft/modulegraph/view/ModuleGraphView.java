package com.steeplesoft.modulegraph.view;

import static dev.tamboui.toolkit.Toolkit.column;
import static dev.tamboui.toolkit.Toolkit.list;
import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.row;
import static dev.tamboui.toolkit.Toolkit.spacer;
import static dev.tamboui.toolkit.Toolkit.table;
import static dev.tamboui.toolkit.Toolkit.tabs;
import static dev.tamboui.toolkit.Toolkit.text;

import java.util.LinkedHashMap;

import com.steeplesoft.modulegraph.ModuleGraphController;
import com.steeplesoft.modulegraph.model.ModuleDefinition;
import dev.tamboui.layout.Constraint;
import dev.tamboui.layout.Rect;
import dev.tamboui.style.Color;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.element.RenderContext;
import dev.tamboui.toolkit.element.Size;
import dev.tamboui.toolkit.elements.ListElement;
import dev.tamboui.toolkit.elements.Panel;
import dev.tamboui.widgets.table.Row;

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

    private Panel infoPanel() {
        ModuleDefinition module = controller.getModuleList().get(listElement.selected());

        return panel(
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
        )
                .id("info-panel")
                .focusable()
                .fill();
    }

    private Panel renderTabPanel(ModuleDefinition module) {
        var child = switch (controller.getTabsState().selected()) {
            case 0 -> resourcesPanel(module);
            case 1 -> dependenciesPanel(module);
            case 2 -> dependentsPanel(module);
            default -> throw new IllegalArgumentException("Invalid tab index: " + controller.getTabsState().selected());
        };

        return panel(child)
                .id("active-tab")
                .focusable()
                .fill();
    }

    private Element resourcesPanel(ModuleDefinition module) {
        ListElement<?> listElement = list().highlightSymbol("> ")
                .highlightColor(Color.YELLOW)
                .autoScroll()
                .scrollbar()
                .scrollbarThumbColor(Color.CYAN);
        module.resources().stream()
                .filter(artifact -> artifact.name() != null)
                .forEach(dep -> listElement.add(dep.name()));

        return listElement;
    }

    private Element dependenciesPanel(ModuleDefinition module) {
        return table()
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
                        .borderColor(Color.GRAY);
    }

    private Element dependentsPanel(ModuleDefinition module) {
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

        return listElement;
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
