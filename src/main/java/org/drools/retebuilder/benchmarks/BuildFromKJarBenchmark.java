package org.drools.retebuilder.benchmarks;

import java.util.concurrent.TimeUnit;
import org.drools.compiler.kie.builder.impl.ZipKieModule;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 15, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 15, time = 5, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BuildFromKJarBenchmark {

    @Param({"10000"})
    private int numberOfRules;

    @Param("100")
    private int numberOfRulesPerFile;

    @Param({"true"})
    private boolean useRuleModel;

    private KieServices kieServices;
    private KieRepository kieRepository;
    private ReleaseId releaseId;
    private KJarWithKnowledgeFiles jarWithKnowledgeFiles;

    @Setup
    public void setUpKJar() {
        kieServices = KieServices.get();
        kieRepository = kieServices.getRepository();
        releaseId = kieServices.newReleaseId("org.kie", "kjar-test", "1.0");
        jarWithKnowledgeFiles = BenchmarkUtil.createJarFile(kieServices, releaseId, numberOfRules, numberOfRulesPerFile, useRuleModel);
    }

    @Benchmark
    public KieBase buildKnowledge(final Blackhole eater) {
        return createKieBaseFromKJar(eater);
    }

    private KieBase createKieBaseFromKJar(final Blackhole eater) {
        kieRepository.removeKieModule(releaseId);
        final KieModule zipKieModule;
        if (useRuleModel) {
            zipKieModule = new CanonicalKieModule(releaseId, BenchmarkUtil.getDefaultKieModuleModel(kieServices),
                    jarWithKnowledgeFiles.getJarFile(), jarWithKnowledgeFiles.getKnowledgeFiles());
        } else {
            zipKieModule = new ZipKieModule(releaseId, BenchmarkUtil.getDefaultKieModuleModel(kieServices),
                    jarWithKnowledgeFiles.getJarFile());
        }
        kieRepository.addKieModule(zipKieModule);
        eater.consume(zipKieModule);
        return kieServices.newKieContainer(releaseId).newKieBase(kieServices.newKieBaseConfiguration());
    }
}
