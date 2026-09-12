package br.edu.ifma.labmanager.metrics;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class ServiceClasspath {

    private ServiceClasspath() {
    }

    static JavaClasses importService(String serviceDir) {
        Path root = locateRepoRoot();
        String[] modules = {"domain", "application", "presentation", "infra"};
        List<Path> paths = new ArrayList<>();
        for (String module : modules) {
            Path classes = root.resolve("services").resolve(serviceDir).resolve(module).resolve("target/classes");
            if (Files.isDirectory(classes)) {
                paths.add(classes);
            }
        }
        if (paths.isEmpty()) {
            throw new IllegalStateException(
                    "Nenhum target/classes para " + serviceDir + ". Execute mvnw verify na raiz. root=" + root
            );
        }
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPaths(paths);
    }

    static Path locateRepoRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        Path[] candidates = {current, current.getParent(), current.resolve("..").normalize()};
        for (Path candidate : candidates) {
            if (candidate != null && Files.isRegularFile(candidate.resolve("pom.xml"))
                    && Files.isDirectory(candidate.resolve("services"))) {
                return candidate;
            }
        }
        return current;
    }
}
