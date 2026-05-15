module modulegraph {
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires java.prefs;
    requires com.google.common;
    requires dev.tamboui.toolkit;
    requires java.logging;

    exports com.steeplesoft.modulegraph;
    exports com.steeplesoft.modulegraph.model;
}
