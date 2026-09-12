package br.edu.ifma.labmanager.metrics;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class SchedulingCleanArchitectureRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = ServiceClasspath.importService("scheduling-service-clean");
    }

    @Test
    void dominioNaoDependeDeFrameworks() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..scheduling.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "jakarta.servlet..",
                        "org.hibernate.."
                );
        rule.check(classes);
    }

    @Test
    void aplicacaoNaoDependeDePresentationNemInfra() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..scheduling.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..scheduling.presentation..",
                        "..scheduling.infra.."
                );
        rule.check(classes);
    }

    @Test
    void dominioNaoDependeDeCamadasExternas() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..scheduling.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..scheduling.application..",
                        "..scheduling.presentation..",
                        "..scheduling.infra.."
                );
        rule.check(classes);
    }

    @Test
    void camadasRespeitamDirecaoDasDependencias() {
        ArchRule rule = layeredArchitecture()
                .consideringOnlyDependenciesInAnyPackage("br.edu.ifma.labmanager.scheduling..")
                .layer("Domain").definedBy("..scheduling.domain..")
                .layer("Application").definedBy("..scheduling.application..")
                .layer("Presentation").definedBy("..scheduling.presentation..")
                .layer("Infra").definedBy("..scheduling.infra..")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Presentation", "Infra")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation", "Infra")
                .whereLayer("Presentation").mayOnlyBeAccessedByLayers("Infra")
                .whereLayer("Infra").mayNotBeAccessedByAnyLayer();
        rule.check(classes);
    }
}
