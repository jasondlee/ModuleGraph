module modulegraph {
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires java.prefs;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.jgrapht.core;

    opens com.steeplesoft.wildfly.modulegraph to javafx.fxml;
    exports com.steeplesoft.wildfly.modulegraph;
    exports com.steeplesoft.wildfly.modulegraph.model;
}
