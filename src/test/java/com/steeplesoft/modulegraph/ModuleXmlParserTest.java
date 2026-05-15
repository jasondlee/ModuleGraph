package com.steeplesoft.modulegraph;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.steeplesoft.modulegraph.model.Artifact;
import com.steeplesoft.modulegraph.model.Filter;
import com.steeplesoft.modulegraph.model.ModuleDefinition;
import com.steeplesoft.modulegraph.model.ModuleDependency;

import static org.junit.jupiter.api.Assertions.*;

class ModuleXmlParserTest {

    private ModuleXmlParser parser;

    @BeforeEach
    void setUp() {
        parser = new ModuleXmlParser();
    }

    private ModuleDefinition parseResource(String resourceName) throws Exception {
        return parser.parse(resourcePath(resourceName));
    }

    private Path resourcePath(String resourceName) {
        var url = getClass().getClassLoader().getResource("modules/" + resourceName);
        assertNotNull(url, "Test resource not found: " + resourceName);
        return Path.of(url.getPath());
    }

    @Nested
    @DisplayName("Basic module parsing")
    class BasicParsing {

        @Test
        @DisplayName("minimal module with only name")
        void minimalModule() throws Exception {
            var mod = parseResource("minimal.xml");
            assertEquals("test.minimal", mod.name());
            assertEquals("", mod.version());
            assertNull(mod.mainClass());
            assertTrue(mod.properties().isEmpty());
            assertTrue(mod.resources().isEmpty());
            assertTrue(mod.dependencies().isEmpty());
            assertTrue(mod.exports().isEmpty());
            assertTrue(mod.permissions().isEmpty());
            assertTrue(mod.provides().isEmpty());
        }

        @Test
        @DisplayName("module with version attribute")
        void moduleWithVersion() throws Exception {
            var mod = parseResource("version-module.xml");
            assertEquals("test.versioned", mod.name());
            assertEquals("2.0.1", mod.version());
        }

        @Test
        @DisplayName("simple module with artifact resource")
        void simpleModuleWithArtifact() throws Exception {
            var mod = parseResource("simple-module.xml");
            assertEquals("jakarta.annotation.api", mod.name());
            assertEquals(1, mod.resources().size());
            assertEquals("jakarta.annotation:jakarta.annotation-api:3.0.0", mod.resources().get(0).name());
            assertNull(mod.resources().get(0).path());
        }

        @Test
        @DisplayName("module with main-class element")
        void moduleWithMainClass() throws Exception {
            var mod = parseResource("main-class-module.xml");
            assertEquals("io.smallrye.jandex", mod.name());
            assertEquals("org.jboss.jandex.Main", mod.mainClass());
        }

        @Test
        @DisplayName("parse via Path delegates to InputStream parse")
        void parseViaPath() throws Exception {
            var path = resourcePath("minimal.xml");
            var mod = parser.parse(path);
            assertEquals("test.minimal", mod.name());
        }
    }

    @Nested
    @DisplayName("Properties parsing")
    class PropertiesParsing {

        @Test
        @DisplayName("module-level properties")
        void moduleProperties() throws Exception {
            var mod = parseResource("main-class-module.xml");
            assertEquals(1, mod.properties().size());
            assertEquals("jboss.api", mod.properties().get(0).name());
            assertEquals("private", mod.properties().get(0).value());
        }

        @Test
        @DisplayName("multiple properties")
        void multipleProperties() throws Exception {
            var mod = parseResource("dependency-properties.xml");
            var dep = mod.dependencies().get(0);
            assertEquals(2, dep.properties().size());
            assertEquals("jboss.api", dep.properties().get(0).name());
            assertEquals("private", dep.properties().get(0).value());
            assertEquals("custom.key", dep.properties().get(1).name());
            assertEquals("custom.value", dep.properties().get(1).value());
        }
    }

    @Nested
    @DisplayName("Resources parsing")
    class ResourcesParsing {

        @Test
        @DisplayName("resource-root elements")
        void resourceRoots() throws Exception {
            var mod = parseResource("resource-root.xml");
            assertEquals(3, mod.resources().size());

            Artifact root1 = mod.resources().get(0);
            assertNull(root1.name());
            assertEquals("lib", root1.path());

            Artifact root2 = mod.resources().get(1);
            assertNull(root2.name());
            assertEquals("service-loader-resources", root2.path());

            Artifact artifact = mod.resources().get(2);
            assertEquals("com.example:test:1.0", artifact.name());
            assertNull(artifact.path());
        }

        @Test
        @DisplayName("artifact with filter children")
        void artifactWithFilter() throws Exception {
            var mod = parseResource("artifact-with-filter.xml");
            assertEquals(1, mod.resources().size());

            Artifact artifact = mod.resources().get(0);
            assertEquals(1, artifact.filter().size());

            Filter filter = artifact.filter().get(0);
            assertEquals(1, filter.include().size());
            assertEquals("com/example/api", filter.include().get(0).path());
            assertEquals(1, filter.exclude().size());
            assertEquals("com/example/internal", filter.exclude().get(0).path());
        }

        @Test
        @DisplayName("artifact with conditions")
        void artifactWithConditions() throws Exception {
            var mod = parseResource("conditions.xml");
            assertEquals(1, mod.resources().size());

            Artifact artifact = mod.resources().get(0);
            assertEquals(2, artifact.conditions().size());

            var propEqual = artifact.conditions().get(0);
            assertEquals("jboss.api", propEqual.propertyEqual());
            assertNull(propEqual.propertyNotEqual());

            var propNotEqual = artifact.conditions().get(1);
            assertNull(propNotEqual.propertyEqual());
            assertEquals("jboss.deprecated", propNotEqual.propertyNotEqual());
        }
    }

    @Nested
    @DisplayName("Dependencies parsing")
    class DependenciesParsing {

        @Test
        @DisplayName("basic dependencies with attributes")
        void basicDependencies() throws Exception {
            var mod = parseResource("dependency-with-exports.xml");
            assertEquals(5, mod.dependencies().size());

            ModuleDependency desktop = mod.dependencies().get(0);
            assertEquals("java.desktop", desktop.name());
            assertFalse(desktop.export());
            assertFalse(desktop.optional());
            assertEquals("none", desktop.services());

            ModuleDependency naming = mod.dependencies().get(1);
            assertEquals("java.naming", naming.name());
            assertFalse(naming.export());

            ModuleDependency txApi = mod.dependencies().get(4);
            assertEquals("jakarta.transaction.api", txApi.name());
            assertTrue(txApi.export());
        }

        @Test
        @DisplayName("dependency with export, optional, and services attributes")
        void dependencyAttributes() throws Exception {
            var mod = parseResource("provides-module.xml");
            ModuleDependency dep = mod.dependencies().get(0);
            assertEquals("org.bouncycastle.bcpkix", dep.name());
            assertTrue(dep.export());
            assertEquals("export", dep.services());
        }

        @Test
        @DisplayName("dependency with optional and services=import")
        void dependencyOptionalImport() throws Exception {
            var mod = parseResource("dependency-with-imports.xml");
            ModuleDependency dep = mod.dependencies().get(0);
            assertEquals("some.module", dep.name());
            assertTrue(dep.optional());
            assertEquals("import", dep.services());
        }

        @Test
        @DisplayName("dependency with properties sub-element")
        void dependencyWithProperties() throws Exception {
            var mod = parseResource("dependency-properties.xml");
            ModuleDependency dep = mod.dependencies().get(0);
            assertEquals(2, dep.properties().size());
        }

        @Test
        @DisplayName("dependency with exports filter")
        void dependencyWithExportsFilter() throws Exception {
            var mod = parseResource("dependency-with-exports.xml");
            ModuleDependency naming = mod.dependencies().get(1);
            assertEquals(1, naming.exports().size());

            Filter filter = naming.exports().get(0);
            assertEquals(1, filter.include().size());
            assertEquals("javax/naming", filter.include().get(0).path());
        }

        @Test
        @DisplayName("dependency with imports filter")
        void dependencyWithImportsFilter() throws Exception {
            var mod = parseResource("dependency-with-imports.xml");
            ModuleDependency dep = mod.dependencies().get(0);
            assertEquals(1, dep.imports().size());

            Filter filter = dep.imports().get(0);
            assertEquals(1, filter.include().size());
            assertEquals("META-INF/services", filter.include().get(0).path());
            assertEquals(1, filter.exclude().size());
            assertEquals("META-INF/internal", filter.exclude().get(0).path());
        }
    }

    @Nested
    @DisplayName("Filter and path set parsing")
    class FilterParsing {

        @Test
        @DisplayName("module-level exports with all filter types")
        void moduleLevelExportsAllFilterTypes() throws Exception {
            var mod = parseResource("module-exports.xml");
            assertEquals(1, mod.exports().size());

            Filter filter = mod.exports().get(0);
            assertEquals(1, filter.include().size());
            assertEquals("org/api", filter.include().get(0).path());
            assertEquals(1, filter.exclude().size());
            assertEquals("org/internal", filter.exclude().get(0).path());

            assertEquals(1, filter.includeSet().size());
            assertEquals(2, filter.includeSet().get(0).path().size());
            assertEquals("org/public/a", filter.includeSet().get(0).path().get(0).path());
            assertEquals("org/public/b", filter.includeSet().get(0).path().get(1).path());

            assertEquals(1, filter.excludeSet().size());
            assertEquals(2, filter.excludeSet().get(0).path().size());
            assertEquals("org/impl/a", filter.excludeSet().get(0).path().get(0).path());
            assertEquals("org/impl/b", filter.excludeSet().get(0).path().get(1).path());
        }

        @Test
        @DisplayName("include/exclude fall back to name attribute when path is absent")
        void includeExcludeNameFallback() throws Exception {
            var mod = parseResource("include-name-fallback.xml");
            assertEquals(1, mod.exports().size());

            Filter filter = mod.exports().get(0);
            assertEquals(1, filter.include().size());
            assertEquals("org/fallback/include", filter.include().get(0).path());
            assertEquals(1, filter.exclude().size());
            assertEquals("org/fallback/exclude", filter.exclude().get(0).path());
        }
    }

    @Nested
    @DisplayName("Skipped elements")
    class SkippedElements {

        @Test
        @DisplayName("system dependency is skipped, no null-name entries")
        void systemDependencySkipped() throws Exception {
            var mod = parseResource("system-dependency.xml");
            assertEquals(1, mod.dependencies().size());
            assertEquals("some.other.module", mod.dependencies().get(0).name());
            assertTrue(mod.dependencies().stream().noneMatch(d -> d.name() == null));
        }

        @Test
        @DisplayName("provides element is skipped without error")
        void providesSkipped() throws Exception {
            var mod = parseResource("provides-module.xml");
            assertNotNull(mod);
            assertEquals("de.dentrassi.crypto.pem-keystore", mod.name());
            assertTrue(mod.provides().isEmpty());
        }

        @Test
        @DisplayName("permissions element is skipped without error")
        void permissionsSkipped() throws Exception {
            var mod = parseResource("permissions-module.xml");
            assertNotNull(mod);
            assertEquals("test.permissions", mod.name());
            assertTrue(mod.permissions().isEmpty());
        }
    }

    @Nested
    @DisplayName("Negative tests")
    class NegativeTests {

        @Test
        @DisplayName("module-alias produces empty definition with alias set")
        void moduleAlias() throws Exception {
            var mod = parseResource("module-alias.xml");
            assertEquals("org.glassfish.jakarta.el", mod.name());
            assertEquals("org.glassfish.expressly", mod.alias());
            assertEquals("", mod.version());
            assertNull(mod.mainClass());
            assertTrue(mod.dependencies().isEmpty());
            assertTrue(mod.resources().isEmpty());
            assertTrue(mod.exports().isEmpty());
            assertTrue(mod.properties().isEmpty());
            assertTrue(mod.permissions().isEmpty());
            assertTrue(mod.provides().isEmpty());
        }

        @Test
        @DisplayName("empty document throws XMLStreamException")
        void emptyDocumentThrows() {
            assertThrows(XMLStreamException.class, () -> parseResource("empty.xml"));
        }

        @Test
        @DisplayName("nonexistent file throws IOException")
        void nonexistentFileThrows() {
            assertThrows(IOException.class, () -> parser.parse(Path.of("/nonexistent/module.xml")));
        }
    }

    @Nested
    @DisplayName("Unexpected elements produce warnings")
    class UnexpectedElements {

        private List<LogRecord> capturedRecords;
        private TestLogHandler handler;

        @BeforeEach
        void setUpLogCapture() {
            capturedRecords = new java.util.ArrayList<>();
            handler = new TestLogHandler(capturedRecords);
            Logger logger = Logger.getLogger(ModuleXmlParser.class.getName());
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);
        }

        @Test
        @DisplayName("unexpected child elements in all containers are skipped with warnings")
        void unexpectedElementsSkipped() throws Exception {
            var mod = parseResource("unexpected-elements.xml");
            assertNotNull(mod);
            assertEquals("test.unexpected", mod.name());

            assertEquals(1, mod.properties().size());
            assertEquals(1, mod.resources().size());
            assertEquals(1, mod.dependencies().size());
            assertEquals(1, mod.exports().size());

            var warnings = capturedRecords.stream()
                    .filter(r -> r.getLevel() == Level.WARNING)
                    .map(LogRecord::getMessage)
                    .toList();

            assertTrue(warnings.stream().anyMatch(m -> m.contains("unknown-module-child") && m.contains("<module>")));
            assertTrue(warnings.stream().anyMatch(m -> m.contains("unknown-prop-child") && m.contains("<properties>")));
            assertTrue(warnings.stream().anyMatch(m -> m.contains("unknown-resource-child") && m.contains("<resources>")));
            assertTrue(warnings.stream().anyMatch(m -> m.contains("unknown-artifact-child") && m.contains("resource element")));
            assertTrue(warnings.stream().anyMatch(m -> m.contains("unknown-dep-child") && m.contains("<dependencies>")));
            assertTrue(warnings.stream().anyMatch(m -> m.contains("unknown-mod-dep-child") && m.contains("dependency <module>")));
            assertTrue(warnings.stream().anyMatch(m -> m.contains("unknown-filter-child") && m.contains("filter")));
            assertTrue(warnings.stream().anyMatch(m -> m.contains("unknown-pathset-child") && m.contains("path set")));
        }

        @Test
        @DisplayName("unexpected child in conditions is skipped with warning")
        void unexpectedConditionChild() throws Exception {
            var mod = parseResource("unexpected-conditions-child.xml");
            assertNotNull(mod);

            Artifact artifact = mod.resources().get(0);
            assertEquals(1, artifact.conditions().size());
            assertEquals("test.prop", artifact.conditions().get(0).propertyEqual());

            var warnings = capturedRecords.stream()
                    .filter(r -> r.getLevel() == Level.WARNING)
                    .map(LogRecord::getMessage)
                    .toList();
            assertTrue(warnings.stream().anyMatch(m -> m.contains("unknown-condition-child") && m.contains("<conditions>")));
        }

        @Test
        @DisplayName("unexpected attributes on root module element produce FINE log")
        void unexpectedRootAttributes() throws Exception {
            var mod = parseResource("unexpected-attrs.xml");
            assertEquals("test.unexpected.attrs", mod.name());
            assertEquals("1.0", mod.version());

            var fineMessages = capturedRecords.stream()
                    .filter(r -> r.getLevel() == Level.FINE)
                    .map(LogRecord::getMessage)
                    .toList();
            assertTrue(fineMessages.stream().anyMatch(m -> m.contains("slot") && m.contains("<module>")));
        }

        @Test
        @DisplayName("system dependency produces FINE log, not WARNING")
        void systemDependencyFineLog() throws Exception {
            parseResource("system-dependency.xml");

            var fineMessages = capturedRecords.stream()
                    .filter(r -> r.getLevel() == Level.FINE)
                    .map(LogRecord::getMessage)
                    .toList();
            assertTrue(fineMessages.stream().anyMatch(m -> m.contains("<system>")));

            var warnings = capturedRecords.stream()
                    .filter(r -> r.getLevel() == Level.WARNING)
                    .toList();
            assertTrue(warnings.isEmpty());
        }

        @Test
        @DisplayName("regular module has null alias")
        void regularModuleHasNullAlias() throws Exception {
            var mod = parseResource("minimal.xml");
            assertNull(mod.alias());
        }

        private static class TestLogHandler extends Handler {
            private final List<LogRecord> records;

            TestLogHandler(List<LogRecord> records) {
                this.records = records;
                setLevel(Level.ALL);
            }

            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {}

            @Override
            public void close() throws SecurityException {}
        }
    }

    @Nested
    @DisplayName("Real WildFly module files")
    class RealWorldFiles {

        @Test
        @DisplayName("opentelemetry api module parses fully")
        void opentelemetryApi() throws Exception {
            var mod = parseResource("io.opentelemetry.api.xml");
                    //parser.parse(Path.of("/Users/jdlee/src/ibm/wildfly/wildfly-full/build/target/wildfly-40.0.0.Final-SNAPSHOT/modules/system/layers/base/io/opentelemetry/api/main/module.xml"));
            assertEquals("io.opentelemetry.api", mod.name());
            assertEquals(1, mod.properties().size());
            assertEquals(2, mod.resources().size());
            assertEquals(9, mod.dependencies().size());
            assertTrue(mod.dependencies().stream().noneMatch(d -> d.name() == null));
        }

        @Test
        @DisplayName("wildfly transaction client module with exports and provides")
        void wildflyTransactionClient() throws Exception {
            var mod = parseResource("org.wildfly.transaction.client.xml");
                    //parser.parse(Path.of("/Users/jdlee/src/ibm/wildfly/wildfly-full/build/target/wildfly-40.0.0.Final-SNAPSHOT/modules/system/layers/base/org/wildfly/transaction/client/main/module.xml"));
            assertEquals("org.wildfly.transaction.client", mod.name());
            assertEquals(1, mod.exports().size());
            assertEquals(1, mod.exports().get(0).exclude().size());
            assertEquals("org/wildfly/transaction/client/_private", mod.exports().get(0).exclude().get(0).path());
            assertEquals(14, mod.dependencies().size());

            ModuleDependency ejbClient = mod.dependencies().stream()
                    .filter(d -> "org.jboss.ejb-client".equals(d.name()))
                    .findFirst().orElseThrow();
            assertTrue(ejbClient.optional());
            assertEquals("import", ejbClient.services());
        }
    }
}
