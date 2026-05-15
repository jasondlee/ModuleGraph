package com.steeplesoft.modulegraph;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.steeplesoft.modulegraph.model.Artifact;
import com.steeplesoft.modulegraph.model.Condition;
import com.steeplesoft.modulegraph.model.Filter;
import com.steeplesoft.modulegraph.model.ModuleDefinition;
import com.steeplesoft.modulegraph.model.ModuleDependency;
import com.steeplesoft.modulegraph.model.PathSet;
import com.steeplesoft.modulegraph.model.PathSpec;
import com.steeplesoft.modulegraph.model.Property;

public class ModuleXmlParser {
    private static final Logger LOGGER = Logger.getLogger(ModuleXmlParser.class.getName());
    private final XMLInputFactory factory;

    public ModuleXmlParser() {
        factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    }

    public ModuleDefinition parse(Path xmlFile) throws IOException, XMLStreamException {
        try (InputStream is = Files.newInputStream(xmlFile)) {
            return parse(is);
        }
    }

    public ModuleDefinition parse(InputStream is) throws XMLStreamException {
        XMLStreamReader reader = factory.createXMLStreamReader(is);
        try {
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                    if ("module".equals(reader.getLocalName())) {
                        return readModule(reader);
                    }
                    if ("module-alias".equals(reader.getLocalName())) {
                        return readModuleAlias(reader);
                    }
                    LOGGER.warning("Expected root <module> or <module-alias> element but found <" + reader.getLocalName() + ">");
                    skipElement(reader);
                }
            }
            throw new XMLStreamException("No <module> root element found");
        } finally {
            reader.close();
        }
    }

    private ModuleDefinition readModule(XMLStreamReader reader) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        String version = reader.getAttributeValue(null, "version");
        String mainClass = null;
        List<Filter> exports = new ArrayList<>();
        List<ModuleDependency> dependencies = new ArrayList<>();
        List<Artifact> resources = new ArrayList<>();
        List<Property> properties = new ArrayList<>();
        List<Object> permissions = new ArrayList<>();
        List<Object> provides = new ArrayList<>();

        warnUnexpectedAttributes(reader, "module", "name", "version");

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "main-class" -> {
                        mainClass = reader.getAttributeValue(null, "name");
                        skipElement(reader);
                    }
                    case "properties" -> properties.addAll(readProperties(reader));
                    case "resources" -> resources.addAll(readResources(reader));
                    case "dependencies" -> dependencies.addAll(readDependencies(reader));
                    case "exports" -> exports.add(readFilter(reader));
                    case "permissions" -> {
                        LOGGER.fine("Skipping <permissions> element (not fully modeled)");
                        skipElement(reader);
                    }
                    case "provides" -> {
                        LOGGER.fine("Skipping <provides> element (not fully modeled)");
                        skipElement(reader);
                    }
                    default -> {
                        LOGGER.warning("Unexpected element <" + reader.getLocalName() + "> inside <module>, skipping");
                        skipElement(reader);
                    }
                }
            }
        }

        return new ModuleDefinition(name, version, mainClass, null, exports, dependencies,
                resources, properties, permissions, provides);
    }

    private ModuleDefinition readModuleAlias(XMLStreamReader reader) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        String targetName = reader.getAttributeValue(null, "target-name");
        skipElement(reader);
        return new ModuleDefinition(name, "", null, targetName,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private List<Property> readProperties(XMLStreamReader reader) throws XMLStreamException {
        List<Property> properties = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("property".equals(reader.getLocalName())) {
                    String name = reader.getAttributeValue(null, "name");
                    String value = reader.getAttributeValue(null, "value");
                    properties.add(new Property(name, value));
                    skipElement(reader);
                } else {
                    LOGGER.warning("Unexpected element <" + reader.getLocalName() + "> inside <properties>, skipping");
                    skipElement(reader);
                }
            }
        }

        return properties;
    }

    private List<Artifact> readResources(XMLStreamReader reader) throws XMLStreamException {
        List<Artifact> resources = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "artifact" -> {
                        String name = reader.getAttributeValue(null, "name");
                        resources.add(readArtifact(reader, name, null));
                    }
                    case "resource-root" -> {
                        String path = reader.getAttributeValue(null, "path");
                        resources.add(readArtifact(reader, null, path));
                    }
                    default -> {
                        LOGGER.warning("Unexpected element <" + reader.getLocalName() + "> inside <resources>, skipping");
                        skipElement(reader);
                    }
                }
            }
        }

        return resources;
    }

    private Artifact readArtifact(XMLStreamReader reader, String name, String path) throws XMLStreamException {
        List<Filter> filters = new ArrayList<>();
        List<Condition> conditions = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "filter" -> filters.add(readFilter(reader));
                    case "conditions" -> conditions.addAll(readConditions(reader));
                    default -> {
                        LOGGER.warning("Unexpected element <" + reader.getLocalName() + "> inside resource element, skipping");
                        skipElement(reader);
                    }
                }
            }
        }

        return new Artifact(name, path, filters, conditions);
    }

    private List<Condition> readConditions(XMLStreamReader reader) throws XMLStreamException {
        List<Condition> conditions = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "property-equal" -> {
                        String value = reader.getAttributeValue(null, "name");
                        conditions.add(new Condition(value, null));
                        skipElement(reader);
                    }
                    case "property-not-equal" -> {
                        String value = reader.getAttributeValue(null, "name");
                        conditions.add(new Condition(null, value));
                        skipElement(reader);
                    }
                    default -> {
                        LOGGER.warning("Unexpected element <" + reader.getLocalName() + "> inside <conditions>, skipping");
                        skipElement(reader);
                    }
                }
            }
        }

        return conditions;
    }

    private List<ModuleDependency> readDependencies(XMLStreamReader reader) throws XMLStreamException {
        List<ModuleDependency> dependencies = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "module" -> dependencies.add(readModuleDependency(reader));
                    case "system" -> {
                        LOGGER.fine("Skipping <system> dependency element (not supported by model)");
                        skipElement(reader);
                    }
                    default -> {
                        LOGGER.warning("Unexpected element <" + reader.getLocalName() + "> inside <dependencies>, skipping");
                        skipElement(reader);
                    }
                }
            }
        }

        return dependencies;
    }

    private ModuleDependency readModuleDependency(XMLStreamReader reader) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        boolean export = Boolean.parseBoolean(reader.getAttributeValue(null, "export"));
        boolean optional = Boolean.parseBoolean(reader.getAttributeValue(null, "optional"));
        String services = reader.getAttributeValue(null, "services");

        List<Property> properties = new ArrayList<>();
        List<Filter> imports = new ArrayList<>();
        List<Filter> exports = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "properties" -> properties.addAll(readProperties(reader));
                    case "imports" -> imports.add(readFilter(reader));
                    case "exports" -> exports.add(readFilter(reader));
                    default -> {
                        LOGGER.warning("Unexpected element <" + reader.getLocalName() + "> inside dependency <module>, skipping");
                        skipElement(reader);
                    }
                }
            }
        }

        return new ModuleDependency(name, export, optional, services, properties, imports, exports);
    }

    private Filter readFilter(XMLStreamReader reader) throws XMLStreamException {
        List<PathSpec> include = new ArrayList<>();
        List<PathSpec> exclude = new ArrayList<>();
        List<PathSet> includeSet = new ArrayList<>();
        List<PathSet> excludeSet = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "include" -> {
                        String path = reader.getAttributeValue(null, "path");
                        if (path == null) {
                            path = reader.getAttributeValue(null, "name");
                        }
                        include.add(new PathSpec(path));
                        skipElement(reader);
                    }
                    case "exclude" -> {
                        String path = reader.getAttributeValue(null, "path");
                        if (path == null) {
                            path = reader.getAttributeValue(null, "name");
                        }
                        exclude.add(new PathSpec(path));
                        skipElement(reader);
                    }
                    case "include-set" -> includeSet.add(readPathSet(reader));
                    case "exclude-set" -> excludeSet.add(readPathSet(reader));
                    default -> {
                        LOGGER.warning("Unexpected element <" + reader.getLocalName() + "> inside filter/exports/imports, skipping");
                        skipElement(reader);
                    }
                }
            }
        }

        return new Filter(include, exclude, includeSet, excludeSet);
    }

    private PathSet readPathSet(XMLStreamReader reader) throws XMLStreamException {
        List<PathSpec> paths = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("path".equals(reader.getLocalName())) {
                    String name = reader.getAttributeValue(null, "name");
                    paths.add(new PathSpec(name));
                    skipElement(reader);
                } else {
                    LOGGER.warning("Unexpected element <" + reader.getLocalName() + "> inside path set, skipping");
                    skipElement(reader);
                }
            }
        }

        return new PathSet(paths);
    }

    private void skipElement(XMLStreamReader reader) throws XMLStreamException {
        int depth = 1;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
                if (depth == 0) {
                    return;
                }
            }
        }
    }

    private void warnUnexpectedAttributes(XMLStreamReader reader, String elementName, String... expected) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attrName = reader.getAttributeLocalName(i);
            boolean isExpected = false;
            for (String exp : expected) {
                if (exp.equals(attrName)) {
                    isExpected = true;
                    break;
                }
            }
            if (!isExpected) {
                LOGGER.fine("Unexpected attribute '" + attrName + "' on <" + elementName + ">");
            }
        }
    }
}
