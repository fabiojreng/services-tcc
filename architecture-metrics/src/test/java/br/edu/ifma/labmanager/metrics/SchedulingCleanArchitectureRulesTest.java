package br.edu.ifma.labmanager.metrics;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Regras de fronteira da variante Clean do Agendamento.
 * Violação = falha de teste = evidência de quebra da Regra da Dependência.
 */
class SchedulingCleanArchitectureRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = SchedulingClasspath.importSchedulingClasses();
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
                )
                .because("o domínio deve permanecer livre de frameworks (Clean Architecture)");

        rule.check(classes);
    }

    @Test
    void aplicacaoNaoDependeDeAdaptadoresNemInfra() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..scheduling.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..scheduling.adapter..",
                        "..scheduling.infrastructure.."
                )
                .because("casos de uso dependem de portas, não de adaptadores concretos");

        rule.check(classes);
    }

    @Test
    void dominioNaoDependeDeAplicacaoNemAdaptadores() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..scheduling.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..scheduling.application..",
                        "..scheduling.adapter..",
                        "..scheduling.infrastructure.."
                );

        rule.check(classes);
    }

    @Test
    void camadasRespeitamDirecaoDasDependencias() {
        ArchRule rule = layeredArchitecture()
                .consideringOnlyDependenciesInAnyPackage("br.edu.ifma.labmanager.scheduling..")
                .layer("Domain").definedBy("..scheduling.domain..")
                .layer("Application").definedBy("..scheduling.application..")
                .layer("Adapter").definedBy("..scheduling.adapter..")
                .layer("Infrastructure").definedBy("..scheduling.infrastructure..")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapter", "Infrastructure")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter", "Infrastructure")
                .whereLayer("Adapter").mayOnlyBeAccessedByLayers("Infrastructure")
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();

        rule.check(classes);
    }
}
