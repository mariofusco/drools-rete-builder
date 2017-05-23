/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.retebuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieProject;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.drools.compiler.kie.builder.impl.ZipKieModule;
import org.drools.compiler.kproject.models.KieBaseModelImpl;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.model.Model;
import org.junit.Test;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import static org.drools.retebuilder.ModelGenerator.generateModel;
import static org.junit.Assert.assertTrue;

public class KJarTest {

    @Test
    public void test() {
        KieServices ks = KieServices.get();
        ReleaseId releaseId = ks.newReleaseId( "org.kie", "kjar-test", "1.0" );

        File jarFile = createJarFile(ks, releaseId);

        executeSession( ks, releaseId );

        KieRepository repo = ks.getRepository();
        repo.removeKieModule( releaseId );

        KieModule zipKieModule = new CanonicalKieModule( releaseId, getDefaultKieModuleModel( ks ), jarFile );
        repo.addKieModule( zipKieModule );

        executeSession( ks, releaseId );
    }

    private void executeSession( KieServices ks, ReleaseId releaseId ) {
        KieContainer kieContainer = ks.newKieContainer( releaseId );
        KieSession kieSession = kieContainer.newKieSession();

        kieSession.insert(new Person( "Mark", 37) );
        kieSession.insert(new Person("Edson", 35));
        kieSession.insert(new Person("Mario", 40));
        kieSession.fireAllRules();
    }

    private File createJarFile(KieServices ks, ReleaseId releaseId) {

        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.writeKModuleXML(getDefaultKieModuleModel(ks).toXML());
        kfs.writePomXML(getPom(releaseId));

        String javaSrc = Person.class.getCanonicalName().replace( '.', File.separatorChar ) + ".java";
        Resource javaResource = ks.getResources().newFileSystemResource( "src/test/java/" + javaSrc );
        kfs.write( "src/main/java/" + javaSrc, javaResource );

        kfs.write("src/main/resources/rule.drl", getRule());
        kfs.write("src/main/java/mymodel/Rules.java", getRuleModelSource());

        KieBuilder kieBuilder = ks.newKieBuilder( kfs );
        assertTrue(kieBuilder.buildAll().getResults().getMessages().isEmpty());

        generateModel( kieBuilder );

        InternalKieModule kieModule = (InternalKieModule) kieBuilder.getKieModule();
        return bytesToFile( releaseId, kieModule.getBytes(), ".jar" );
    }


    private KieModuleModel getDefaultKieModuleModel(KieServices ks) {
        KieModuleModel kproj = ks.newKieModuleModel();
        KieBaseModel kieBaseModel1 = kproj.newKieBaseModel( "kbase" ).setDefault( true );
        KieSessionModel ksession1 = kieBaseModel1.newKieSessionModel( "ksession" ).setDefault( true );
        return kproj;

    }
    private String getPom(ReleaseId releaseId) {
        String pom =
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

    private String getRule() {
        return "import " + Person.class.getCanonicalName() + ";" +
               "rule beta when\n" +
               "  $p1 : Person(name == \"Mark\")\n" +
               "  $p2 : Person(name != \"Mark\", age > $p1.age)\n" +
               "then\n" +
               "  System.out.println($p2.getName() + \" is older than \" + $p1.getName());\n" +
               "end";
    }

    private File bytesToFile( ReleaseId releaseId, byte[] bytes, String extension ) {
        File file = new File( System.getProperty( "java.io.tmpdir" ), releaseId.getArtifactId() + "-" + releaseId.getVersion() + extension );
        try {
            FileOutputStream fos = new FileOutputStream( file );
            fos.write( bytes );
            fos.flush();
            fos.close();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        return file;
    }

    public static class CanonicalKieModule extends ZipKieModule {
        public CanonicalKieModule( ReleaseId releaseId, KieModuleModel kieProject, File file ) {
            super( releaseId, kieProject, file );
        }

        @Override
        public InternalKnowledgeBase createKieBase( KieBaseModelImpl kBaseModel, KieProject kieProject, ResultsImpl messages, KieBaseConfiguration conf ) {
            CanonicalKieBase kieBase = new CanonicalKieBase();

            KieProjectClassLoader kieProjectCL = new KieProjectClassLoader(kieProject);
            Model model = kieProjectCL.createInstance( "mymodel.Rules" );

            model.getRules().forEach( kieBase::addRule );
            return kieBase;
        }

        class KieProjectClassLoader extends ClassLoader {
            public KieProjectClassLoader(KieProject kieProject) {
                super(kieProject.getClassLoader());
            }

            public Class<?> loadClass(String className) throws ClassNotFoundException {
                try {
                    return super.loadClass( className );
                } catch (ClassNotFoundException cnfe) { }

                String fileName = className.replace( '.', '/' ) + ".class";
                byte[] bytes = getBytes(fileName);

                if (bytes == null) {
                    throw new ClassNotFoundException(className);
                }

                return defineClass(className, bytes, 0, bytes.length);
            }

            public <T> T createInstance(String className) {
                try {
                    return (T) loadClass( className ).newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException( e );
                }
            }
        }
    }

    private String getRuleModelSource() {
        return "package mymodel;\n" +
               "" +
               "import java.util.*;\n" +
               "import org.drools.model.*;\n" +
               "import static org.drools.model.DSL.*;\n" +
               "import org.drools.retebuilder.Person;\n" +
               "" +
               "public class Rules implements Model {\n" +
               "" +
               "    @Override\n" +
               "    public List<Rule> getRules() {\n" +
               "        return Arrays.asList( rule1() );\n" +
               "    }\n" +
               "" +
               "    private Rule rule1() {\n" +
               "        Variable<Person> markV = variableOf( type( Person.class ) );\n" +
               "        Variable<Person> olderV = variableOf( type( Person.class ) );\n" +
               "        Rule rule = rule( \"beta\" )\n" +
               "                .view(\n" +
               "                        expr(markV, p -> p.getName().equals(\"Mark\")),\n" +
               "                        expr(olderV, p -> !p.getName().equals(\"Mark\")),\n" +
               "                        expr(olderV, markV, (p1, p2) -> p1.getAge() > p2.getAge())\n" +
               "                     )\n" +
               "                .then(c -> c.on(olderV, markV)\n" +
               "                            .execute( (p1, p2) -> System.out.println( p1.getName() + \" is older than \" + p2.getName() ) ) );\n" +
               "        return rule;\n" +
               "    }\n" +
               "}\n";
    }
}
