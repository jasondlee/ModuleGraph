package com.steeplesoft.wildfly.modulegraph;

import com.steeplesoft.wildfly.modulegraph.model.ModuleDefinition;
import javafx.scene.control.TreeItem;

public class ModuleDefinitionTreeItem extends TreeItem<ModuleDefinition> {
    public ModuleDefinitionTreeItem(ModuleDefinition value) {
        super(value);
    }
}
