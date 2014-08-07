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

        KieSession ksession = getKieSession(str);

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
                "  $p2 : Person(name != \"Mark\", age > $p1.age)\n" +
                "then\n" +
                "  System.out.println($p2.getName() + \" is older than \" + $p1.getName());\n" +
                "end";

        KieSession ksession = getKieSession(str);

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

        KieSession ksession = getKieSession(str);

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
    }

    @Test
    public void testAccumulate() {
        String str =
                "import " + Person.class.getCanonicalName() + ";" +
                "rule R when\n" +
                "  accumulate( Person( name.startsWith(\"M\"), $age : age ); $sum : sum( $age ) )\n" +
                "then\n" +
                "  System.out.println( $sum );\n" +
                "end";

        KieSession ksession = getKieSession(str);

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
    }

    private KieSession getKieSession(String str) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem().write( "src/main/resources/r1.drl", str );
        ks.newKieBuilder( kfs ).buildAll();
        return ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();
    }

    @Test
    public void testQuery() {
        String str =
                "import " + Person.class.getCanonicalName() + ";\n" +
                "query isOlder( Person $p1, Person $p2 )\n" +
                "    $p2 := Person( age > $p1.age )\n" +
                "end\n" +
                "rule R when\n" +
                "  $p1 : Person(name == \"Mark\")\n" +
                "  isOlder( $p1, $p2; )\n" +
                "then\n" +
                "  System.out.println($p2.getName() + \" is older than \" + $p1.getName());\n" +
                "end";

        KieSession ksession = getKieSession(str);

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
    }

    @Test
    public void testFunction() {
        String str =
                "import " + Person.class.getCanonicalName() + ";\n" +
                "function int findAge(Person person) {\n" +
                "    return person.getAge();\n" +
                "}\n" +
                "rule R when\n" +
                "  $p1 : Person(name == \"Mark\")\n" +
                "  $age : Integer() from findAge($p1)" +
                "then\n" +
                "  System.out.println($age);\n" +
                "end";

        KieSession ksession = getKieSession(str);

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
    }
}
