package com.steeplesoft.wildfly.modulegraph;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import com.steeplesoft.wildfly.modulegraph.model.ModuleDependency;

public class ModuleDefinitionValueFactory implements Callback<TableColumn.CellDataFeatures<ModuleDependency, String>, ObservableValue<String>> {
    private final String field;

    public ModuleDefinitionValueFactory(String field) {
        this.field = field;
    }

    @Override
    public ObservableValue<String> call(TableColumn.CellDataFeatures<ModuleDependency, String> param) {
        return switch (field) {
            case "name" -> new ReadOnlyStringWrapper(param.getValue().name);
            case "export" -> new ReadOnlyStringWrapper(Boolean.toString(param.getValue().export));
            case "services" -> new ReadOnlyStringWrapper(param.getValue().services);
            case "optional" -> new ReadOnlyStringWrapper(Boolean.toString(param.getValue().optional));
            default -> null;
        };
    }
}
