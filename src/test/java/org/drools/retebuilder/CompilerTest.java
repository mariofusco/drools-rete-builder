package org.drools.retebuilder;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieSession;

public class CompilerTest {

    @Test
    public void testAlpha() {
        String str =
                "import " + Person.class.getCanonicalName() + ";" +
                "rule R when\n" +
                "  $p : Person(name == \"Mark\")\n" +
                "then\n" +
                "  System.out.println($p);\n" +
                "end";

        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem().write( "src/main/resources/r1.drl", str );
        ks.newKieBuilder( kfs ).buildAll();
        KieSession ksession = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
    }

    @Test
    public void testBeta() {
        String str =
                "import " + Person.class.getCanonicalName() + ";" +
                "rule R when\n" +
                "  $p1 : Person(name == \"Mark\")\n" +
                "  $p2 : Person(age > $p1.age)\n" +
                "then\n" +
                "  System.out.println($p2.getName() + \" is older than \" + $p1.getName());\n" +
                "end";

        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem().write( "src/main/resources/r1.drl", str );
        ks.newKieBuilder( kfs ).buildAll();
        KieSession ksession = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
    }

    @Test
    public void testNot() {
        String str =
                "import " + Person.class.getCanonicalName() + ";" +
                "rule R when\n" +
                "  $p1 : Person()\n" +
                "  not Person(age > $p1.age)\n" +
                "then\n" +
                "  System.out.println(\"Oldest person is \" + $p1.getName());\n" +
                "end";

        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem().write( "src/main/resources/r1.drl", str );
        ks.newKieBuilder( kfs ).buildAll();
        KieSession ksession = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
    }
}
