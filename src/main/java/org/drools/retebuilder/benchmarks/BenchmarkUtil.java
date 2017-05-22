package org.drools.retebuilder.benchmarks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.io.Resource;

public final class BenchmarkUtil {

    public static KJarWithKnowledgeFiles createJarFile(final KieServices kieServices, final ReleaseId releaseId, final int numberOfRules,
            final int numberOfRulesPerFile, final boolean useRuleModelSources) {

        final KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.writeKModuleXML(getDefaultKieModuleModel(kieServices).toXML());
        kieFileSystem.writePomXML(getPom(releaseId));

        writeDomainModelToKJar(kieServices, kieFileSystem);

        final Collection<String> generatedKnowledge;
        if (useRuleModelSources) {
            generatedKnowledge = generateRuleModelSourcesToKJar(kieFileSystem, numberOfRules, numberOfRulesPerFile);
        } else {
            generatedKnowledge = generateDRLsToKJar(kieFileSystem, numberOfRules, numberOfRulesPerFile);
        }

        final KieBuilder kieBuilder = kieServices.newKieBuilder( kieFileSystem );
        testGeneratedKnowledge(kieBuilder);
        final File jarFile = writeKJarToFile(kieBuilder, releaseId);
        return new KJarWithKnowledgeFiles(jarFile, generatedKnowledge);
    }

    private static void writeDomainModelToKJar(final KieServices kieServices, final KieFileSystem kieFileSystem) {
        final String javaSrc = Person.class.getCanonicalName().replace('.', File.separatorChar) + ".java";
        final Resource javaResource = kieServices.getResources().newFileSystemResource("src/main/java/" + javaSrc);
        kieFileSystem.write("src/main/java/" + javaSrc, javaResource);
    }

    private static Collection<String> generateDRLsToKJar(final KieFileSystem kieFileSystem, final int numberOfRules,
            final int numberOfRulesPerFile) {
        final Collection<String> generatedDrls = new ArrayList<>();
        // Rounding down so we generate all files with the exact amount of rules first.
        final BigDecimal numberOfFiles = BigDecimal.valueOf(numberOfRules)
                .divide(BigDecimal.valueOf(numberOfRulesPerFile), 0, BigDecimal.ROUND_DOWN);
        for (int i = 1; i <= numberOfFiles.intValue(); i++) {
            generatedDrls.add(generateDRLtoKJar(kieFileSystem, numberOfRulesPerFile, i));
        }
        // Remaining rules are generated into one last file.
        final int numberOfRulesLeftToGenerate = numberOfRules - (numberOfFiles.intValue() * numberOfRulesPerFile);
        if (numberOfRulesLeftToGenerate > 0) {
            generatedDrls.add(generateDRLtoKJar(kieFileSystem, numberOfRulesLeftToGenerate, numberOfFiles.intValue() + 1));
        }
        return generatedDrls;
    }

    private static String generateDRLtoKJar(final KieFileSystem kieFileSystem, final int numberOfRules, final int fileIndex) {
        final StringBuilder rulesBuilder = new StringBuilder();
        rulesBuilder.append("package org.drools.retebuilder.benchmarks;\n");
        rulesBuilder.append("\n");
        rulesBuilder.append("import " + Person.class.getCanonicalName() + ";\n");
        rulesBuilder.append("\n");
        for (int i = 1; i <= numberOfRules; i++) {
            rulesBuilder.append("rule R_" + fileIndex + "_" + i + " when\n");
            rulesBuilder.append("  $p1 : Person(name == \"Mark_" + fileIndex + "_" + i + "\")\n");
            rulesBuilder.append("  $p2 : Person(name != \"Mark_" + fileIndex + "_" + i + "\", age > $p1.age)\n");
            rulesBuilder.append("then\n");
            rulesBuilder.append("  System.out.println($p2.getName() + \" is older than \" + $p1.getName());\n");
            rulesBuilder.append("end\n");
            rulesBuilder.append("\n");
        }
        final String drlName = "src/main/resources/rules" + fileIndex + ".drl";
        kieFileSystem.write(drlName, rulesBuilder.toString());
        return drlName;
    }

    private static Collection<String> generateRuleModelSourcesToKJar(final KieFileSystem kieFileSystem, final int numberOfRules,
            final int numberOfRulesPerFile) {
        final Collection<String> generatedClasses = new ArrayList<>();
        // Rounding down so we generate all files with the exact amount of rules first.
        final BigDecimal numberOfFiles = BigDecimal.valueOf(numberOfRules)
                .divide(BigDecimal.valueOf(numberOfRulesPerFile), 0, BigDecimal.ROUND_DOWN);
        for (int i = 1; i <= numberOfFiles.intValue(); i++) {
            kieFileSystem.write("src/main/java/org/drools/retebuilder/benchmarks/Rules" + i + ".java",
                    getRuleModelClassSource(i, numberOfRulesPerFile));
            generatedClasses.add("org.drools.retebuilder.benchmarks.Rules" + i);
        }
        // Remaining rules are generated into one last source file.
        final int numberOfRulesLeftToGenerate = numberOfRules - (numberOfFiles.intValue() * numberOfRulesPerFile);
        if (numberOfRulesLeftToGenerate > 0) {
            final int lastClassIndex = numberOfFiles.intValue() + 1;
            kieFileSystem.write("src/main/java/org/drools/retebuilder/benchmarks/Rules" + lastClassIndex + ".java",
                    getRuleModelClassSource(lastClassIndex, numberOfRulesLeftToGenerate));
            generatedClasses.add("org.drools.retebuilder.benchmarks.Rules" + lastClassIndex);
        }
        return generatedClasses;
    }

    private static String getRuleModelClassSource(final int classIndex, final int numberOfRules) {
        return "package org.drools.retebuilder.benchmarks;\n" +
                "" +
                "import java.util.*;\n" +
                "import org.drools.model.*;\n" +
                "import static org.drools.model.DSL.*;\n" +
                "import org.drools.retebuilder.benchmarks.Person;\n" +
                "" +
                "public class Rules" + classIndex + " implements Model {\n" +
                "" +
                "    @Override\n" +
                "    public List<Rule> getRules() {\n" +
                "        final List<Rule> ruleList = new ArrayList<>();\n" +
                "        for (int i = 1; i < " + numberOfRules + "; i++) {\n" +
                "            ruleList.add(getRule(i));\n" +
                "        }\n" +
                "        return ruleList;\n" +
                "    }\n" +
                "\n" +
                "    private Rule getRule(final int ruleIndex) {\n" +
                "        Variable<Person> markV = variableOf( type( Person.class ) );\n" +
                "        Variable<Person> olderV = variableOf( type( Person.class ) );\n" +
                "        Rule rule = rule( \"beta\" )\n" +
                "                .view(\n" +
                "                        expr(markV, p -> p.getName().equals(\"Mark_\" + " + classIndex + " + \"_\" + ruleIndex)),\n" +
                "                        expr(olderV, p -> !p.getName().equals(\"Mark_\" + " + classIndex + " + \"_\" + ruleIndex)),\n" +
                "                        expr(olderV, markV, (p1, p2) -> p1.getAge() > p2.getAge())\n" +
                "                     )\n" +
                "                .then(c -> c.on(olderV, markV)\n" +
                "                            .execute( (p1, p2) -> System.out.println( p1.getName() + \" is older than \" + p2.getName() ) ) );\n" +
                "        return rule;\n" +
                "    }\n" +
                "}\n";
    }

    public static KieModuleModel getDefaultKieModuleModel(final KieServices ks) {
        final KieModuleModel kieModuleModel = ks.newKieModuleModel();
        final KieBaseModel kieBaseModel1 = kieModuleModel.newKieBaseModel( "kbase" ).setDefault( true );
        kieBaseModel1.newKieSessionModel( "ksession" ).setDefault( true );
        return kieModuleModel;
    }

    private static String getPom(final ReleaseId releaseId) {
        final String pom =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                        "  <modelVersion>4.0.0</modelVersion>\n" +
                        "\n" +
                        "  <groupId>" + releaseId.getGroupId() + "</groupId>\n" +
                        "  <artifactId>" + releaseId.getArtifactId() + "</artifactId>\n" +
                        "  <version>" + releaseId.getVersion() + "</version>\n" +
                        "</project>";
        return pom;
    }

    private static void testGeneratedKnowledge(final KieBuilder kieBuilder) {
        if (!kieBuilder.buildAll().getResults().getMessages().isEmpty()) {
            final String messages = kieBuilder.buildAll().getResults().getMessages()
                    .stream().map(Message::getText).collect(Collectors.joining("\n"));
            throw new IllegalStateException("There are build errors in generated knowledge!\n" + messages);
        }
    }

    private static File writeKJarToFile(final KieBuilder kieBuilder, final ReleaseId releaseId) {
        final InternalKieModule kieModule = (InternalKieModule) kieBuilder.getKieModule();
        return bytesToFile( releaseId, kieModule.getBytes(), ".jar" );
    }

    private static File bytesToFile( final ReleaseId releaseId, final byte[] bytes, final String extension ) {
        final File file = new File( System.getProperty( "java.io.tmpdir" ), releaseId.getArtifactId() + "-" + releaseId.getVersion() + extension );
        try {
            final FileOutputStream fos = new FileOutputStream( file );
            fos.write( bytes );
            fos.flush();
            fos.close();
        } catch ( final IOException e ) {
            throw new RuntimeException( e );
        }
        return file;
    }

    private BenchmarkUtil() {
        // No instances, this is just a util class.
    }
}
