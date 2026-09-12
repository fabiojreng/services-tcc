package br.edu.ifma.labmanager.metrics;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Regras mínimas da variante em camadas (controle): controller não acessa repositório diretamente.
 */
class SchedulingLayeredArchitectureRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = ServiceClasspath.importService("scheduling-service-layered");
    }

    @Test
    void controllerNaoAcessaRepositorioDiretamente() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..scheduling.layered.web..")
                .should().dependOnClassesThat().resideInAPackage("..scheduling.layered.repository..");
        rule.check(classes);
    }

    @Test
    void entidadeNaoDependeDeWebNemService() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..scheduling.layered.entity..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..scheduling.layered.web..",
                        "..scheduling.layered.service.."
                );
        rule.check(classes);
    }
}
