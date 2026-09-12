package br.edu.ifma.labmanager.metrics;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class CatalogArchitectureRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = ServiceClasspath.importService("catalog-service");
    }

    @Test
    void dominioNaoDependeDeFrameworks() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..catalog.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate.."
                );
        rule.check(classes);
    }

    @Test
    void camadasRespeitamDirecaoDasDependencias() {
        ArchRule rule = layeredArchitecture()
                .consideringOnlyDependenciesInAnyPackage("br.edu.ifma.labmanager.catalog..")
                .layer("Domain").definedBy("..catalog.domain..")
                .layer("Application").definedBy("..catalog.application..")
                .layer("Presentation").definedBy("..catalog.presentation..")
                .layer("Infra").definedBy("..catalog.infra..")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Presentation", "Infra")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation", "Infra")
                .whereLayer("Presentation").mayOnlyBeAccessedByLayers("Infra")
                .whereLayer("Infra").mayNotBeAccessedByAnyLayer();
        rule.check(classes);
    }
}
