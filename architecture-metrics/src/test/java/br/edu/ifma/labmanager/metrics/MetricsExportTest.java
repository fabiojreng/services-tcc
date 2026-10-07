package br.edu.ifma.labmanager.metrics;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.library.metrics.ArchitectureMetrics;
import com.tngtech.archunit.library.metrics.ComponentDependencyMetrics;
import com.tngtech.archunit.library.metrics.LakosMetrics;
import com.tngtech.archunit.library.metrics.MetricsComponents;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

class MetricsExportTest {

    @Test
    void exportMartinAndLakosMetricsForAllServices() throws IOException {
        Path repoRoot = ServiceClasspath.locateRepoRoot();
        Path outDir = repoRoot.resolve("architecture-metrics").resolve("target").resolve("metrics");
        Files.createDirectories(outDir);
        Path csv = outDir.resolve("metrics.csv");

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(csv, StandardCharsets.UTF_8))) {
            writer.println("timestamp,service,component,Ca,Ce,I,A,D,CCD,ACD,RACD,NCCD");
            String ts = Instant.now().toString();

            exportService(writer, ts, "scheduling-service-clean", "br.edu.ifma.labmanager.scheduling");
            exportService(writer, ts, "scheduling-service-layered", "br.edu.ifma.labmanager.scheduling.layered");
            exportService(writer, ts, "identity-service", "br.edu.ifma.labmanager.identity");
            exportService(writer, ts, "catalog-service", "br.edu.ifma.labmanager.catalog");
            exportService(writer, ts, "inventory-service", "br.edu.ifma.labmanager.inventory");
        }

        // Sem rótulo, nada é publicado em experiments/ (evita sobrescrever a baseline em execuções comuns).
        String label = System.getProperty("metrics.label", "").trim();
        if (label.isEmpty()) {
            System.out.println("Métricas geradas em " + csv.toAbsolutePath() + " (use -Dmetrics.label=<rotulo> para publicar)");
            return;
        }
        Path experiments = repoRoot.resolve("experiments");
        if (label.equals("baseline")) {
            Files.copy(csv, experiments.resolve("baseline-fase-3-metrics.csv"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(csv, experiments.resolve("baseline-metrics.csv"), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Métricas baseline publicadas em " + experiments.toAbsolutePath());
            return;
        }
        Path target = experiments.resolve(label).resolve("metricas.csv");
        Files.createDirectories(target.getParent());
        Files.copy(csv, target, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Métricas '" + label + "' publicadas em " + target.toAbsolutePath());
    }

    private void exportService(PrintWriter writer, String ts, String serviceDir, String basePackage) {
        var classes = ServiceClasspath.importService(serviceDir);
        Set<JavaPackage> packages = classes.stream()
                .map(JavaClass::getPackage)
                .filter(p -> p.getName().startsWith(basePackage))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        MetricsComponents<JavaClass> components = MetricsComponents.fromPackages(packages);
        ComponentDependencyMetrics martin = ArchitectureMetrics.componentDependencyMetrics(components);
        LakosMetrics lakos = ArchitectureMetrics.lakosMetrics(components);

        for (JavaPackage pkg : packages) {
            String id = pkg.getName();
            try {
                writer.printf(
                        Locale.US,
                        "%s,%s,%s,%d,%d,%.4f,%.4f,%.4f,%d,%.4f,%.4f,%.4f%n",
                        ts,
                        serviceDir,
                        id,
                        martin.getAfferentCoupling(id),
                        martin.getEfferentCoupling(id),
                        martin.getInstability(id),
                        martin.getAbstractness(id),
                        martin.getNormalizedDistanceFromMainSequence(id),
                        lakos.getCumulativeComponentDependency(),
                        lakos.getAverageComponentDependency(),
                        lakos.getRelativeAverageComponentDependency(),
                        lakos.getNormalizedCumulativeComponentDependency()
                );
            } catch (IllegalArgumentException ignored) {
                // pacote sem classes próprias
            }
        }
    }
}
