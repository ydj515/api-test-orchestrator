package architecture;

import architecture.policy.LayerPolicy;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayeredArchitectureTest {
    private static final String BASE = System.getProperty("architecture.basePackage");
    private static final JavaClasses CLASSES = importModule();

    private static JavaClasses importModule() {
        if (BASE == null || BASE.isBlank()) {
            throw new IllegalStateException("architecture.basePackage is required; run archUnitTest");
        }
        var importer = new ClassFileImporter();
        if (!BASE.equals("karate")) {
            importer = importer.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS);
        }
        return importer.importPackages(BASE);
    }

    @Test
    void importsTheExpectedModuleInsteadOfPassingAnEmptyScope() {
        assertFalse(CLASSES.isEmpty(), "No module classes imported");
        if (BASE.equals("karate")) {
            assertTrue(CLASSES.contain(BASE + ".KarateRunner"));
            assertTrue(CLASSES.contain(BASE + ".KarateSelection"));
        } else {
            for (String layer : new String[]{"presentation", "application", "domain", "config"}) {
                assertTrue(CLASSES.stream().anyMatch(type -> type.getPackageName().startsWith(BASE + "." + layer)),
                        "Missing required layer: " + layer);
            }
        }
    }

    @Test
    void dependenciesPointInwardWithoutFrameworkLeakage() {
        classes().should(obeyLayerPolicy(BASE)).check(CLASSES);
    }

    @Test
    void topLevelLayersHaveNoCycles() {
        if (!BASE.equals("karate")) {
            slices().matching(BASE + ".(*)..").should().beFreeOfCycles().check(CLASSES);
        }
    }

    @Test
    void rejectsOutwardDependenciesAndCyclesInCompiledFixtures() {
        JavaClasses fixture = new ClassFileImporter().importPackages("architecture.fixture");
        assertTrue(classes().should(obeyLayerPolicy("architecture.fixture")).evaluate(fixture).hasViolation());
        assertTrue(slices().matching("architecture.fixture.(*)..").should().beFreeOfCycles()
                .evaluate(fixture).hasViolation());
    }

    private static ArchCondition<JavaClass> obeyLayerPolicy(String base) {
        return new ArchCondition<>("obey layered-clean dependencies") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                item.getDirectDependenciesFromSelf().forEach(dependency -> {
                    if (LayerPolicy.forbidden(base, item.getName(), dependency.getTargetClass().getName())) {
                        events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
                    }
                });
            }
        };
    }
}
