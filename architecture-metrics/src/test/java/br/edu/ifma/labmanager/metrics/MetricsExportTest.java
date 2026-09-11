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
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exporta métricas de Martin e Lakos para CSV (linha de base / experimentos).
 */
class MetricsExportTest {

    private static final String BASE_PACKAGE = "br.edu.ifma.labmanager.scheduling";

    @Test
    void exportMartinAndLakosMetrics() throws IOException {
        var classes = SchedulingClasspath.importSchedulingClasses();

        Set<JavaPackage> packages = classes.stream()
                .map(JavaClass::getPackage)
                .filter(p -> p.getName().startsWith(BASE_PACKAGE))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        MetricsComponents<JavaClass> components = MetricsComponents.fromPackages(packages);

        ComponentDependencyMetrics martin = ArchitectureMetrics.componentDependencyMetrics(components);
        LakosMetrics lakos = ArchitectureMetrics.lakosMetrics(components);

        Path repoRoot = SchedulingClasspath.locateRepoRoot();
        Path outDir = repoRoot.resolve("architecture-metrics").resolve("target").resolve("metrics");
        Files.createDirectories(outDir);
        Path csv = outDir.resolve("baseline-fase-0.csv");

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(csv, StandardCharsets.UTF_8))) {
            writer.println("timestamp,component,Ca,Ce,I,A,D,CCD,ACD,RACD,NCCD");
            String ts = Instant.now().toString();

            for (JavaPackage pkg : packages) {
                String id = pkg.getName();
                try {
                    int ca = martin.getAfferentCoupling(id);
                    int ce = martin.getEfferentCoupling(id);
                    double instability = martin.getInstability(id);
                    double abstractness = martin.getAbstractness(id);
                    double distance = martin.getNormalizedDistanceFromMainSequence(id);

                    writer.printf(
                            Locale.US,
                            "%s,%s,%d,%d,%.4f,%.4f,%.4f,%d,%.4f,%.4f,%.4f%n",
                            ts,
                            id,
                            ca,
                            ce,
                            instability,
                            abstractness,
                            distance,
                            lakos.getCumulativeComponentDependency(),
                            lakos.getAverageComponentDependency(),
                            lakos.getRelativeAverageComponentDependency(),
                            lakos.getNormalizedCumulativeComponentDependency()
                    );
                } catch (IllegalArgumentException ignored) {
                    // pacote sem classes próprias como componente — ignora
                }
            }
        }

        Path published = repoRoot.resolve("experiments").resolve("baseline-fase-0-metrics.csv");
        Files.createDirectories(published.getParent());
        Files.copy(csv, published, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Métricas exportadas para: " + csv.toAbsolutePath());
        System.out.println("Cópia publicada em: " + published.toAbsolutePath());
        System.out.println("Lakos CCD=" + lakos.getCumulativeComponentDependency()
                + " ACD=" + lakos.getAverageComponentDependency()
                + " NCCD=" + lakos.getNormalizedCumulativeComponentDependency());
    }
}
