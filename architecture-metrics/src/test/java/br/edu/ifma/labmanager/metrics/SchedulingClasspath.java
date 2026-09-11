package br.edu.ifma.labmanager.metrics;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolve os {@code target/classes} dos módulos do Agendamento (Clean),
 * evitando depender do JAR executável do Spring Boot (BOOT-INF).
 */
final class SchedulingClasspath {

    private static final String[] MODULE_RELATIVE_CLASSES = {
            "services/scheduling-service-clean/domain/target/classes",
            "services/scheduling-service-clean/application/target/classes",
            "services/scheduling-service-clean/adapter/target/classes",
            "services/scheduling-service-clean/infrastructure/target/classes"
    };

    private SchedulingClasspath() {
    }

    static JavaClasses importSchedulingClasses() {
        Path root = locateRepoRoot();
        List<Path> paths = new ArrayList<>();
        for (String relative : MODULE_RELATIVE_CLASSES) {
            Path classes = root.resolve(relative).normalize();
            if (Files.isDirectory(classes)) {
                paths.add(classes);
            }
        }
        if (paths.isEmpty()) {
            throw new IllegalStateException(
                    "Nenhum target/classes encontrado. Execute 'mvnw install' ou 'mvnw verify' a partir da raiz do repositório. root=" + root
            );
        }
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPaths(paths);
    }

    static Path locateRepoRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        Path[] candidates = {
                current,
                current.getParent(),
                current.resolve("..").normalize()
        };
        for (Path candidate : candidates) {
            if (candidate != null && Files.isRegularFile(candidate.resolve("pom.xml"))
                    && Files.isDirectory(candidate.resolve("services"))) {
                return candidate;
            }
        }
        return current;
    }
}
