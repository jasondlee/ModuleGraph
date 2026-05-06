package com.steeplesoft.modulegraph;

import static dev.tamboui.toolkit.Toolkit.column;
import static dev.tamboui.toolkit.Toolkit.list;
import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.row;
import static dev.tamboui.toolkit.Toolkit.text;

import java.util.List;

import dev.tamboui.layout.Constraint;
import dev.tamboui.style.Color;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.elements.ListElement;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.TuiRunner;
import dev.tamboui.tui.event.Event;
import dev.tamboui.tui.event.KeyCode;
import dev.tamboui.tui.event.KeyEvent;

public class ModuleGraph extends ToolkitApp {
    private final ListElement<?> listElement = list();
    private final String moduleDir;

    public static void main(String[] args) throws Exception {
        // TODO: aesh
        new ModuleGraph(args[0]).run();
    }

    public ModuleGraph(String moduleDir) {
        this.moduleDir = moduleDir;
    }

    @Override
    protected void onStart() {
        var parser = new ModulesParser(moduleDir);
        parser.getModules().values().stream().sorted().forEach(module -> {
            listElement.add(module.name());
        });
        listElement.highlightSymbol("> ")
                .highlightColor(Color.YELLOW)
                .autoScroll()
                .scrollbar()
                .scrollbarThumbColor(Color.CYAN);
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected TuiConfig configure() {
        return TuiConfig.builder()
//                .rawMode(true)
//                .alternateScreen(true)
//                .hideCursor(true)
                .mouseCapture(true)
                .build();
    }

    private boolean handleEvent(Event event, TuiRunner runner) {
        if (!(event instanceof KeyEvent)) {
            return true;
        }

        KeyEvent k = (KeyEvent) event;
        if (k.isQuit() || k.code() == KeyCode.ESCAPE) {
            runner.quit();
            return false;
        }


        return true;
    }

    public Element render() {
        return column(
                panel(
                        text(" Rich List Demo - ListElement with StyledElement items ")
                                .bold()
                                .cyan()
                )
                        .rounded()
                        .borderColor(Color.CYAN)
                        .length(3),
                row (
                        panel(listElement)
                                .title("Rich Items (" + 12 + " items)")
                                .rounded()
                                .borderColor(Color.WHITE)
                                .id("list-panel")
                                .focusable()
//                                .onKeyEvent(this::handleKey)
                                .constraint(Constraint.percentage(25))
                ).fill(),
                panel(
                        text(" Up/Down: Navigate | q: Quit ").dim()
                ).rounded().borderColor(Color.DARK_GRAY).length(3)
        );
    }
}
