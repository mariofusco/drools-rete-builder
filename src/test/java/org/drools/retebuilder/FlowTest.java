package org.drools.retebuilder;

import java.util.ArrayList;
import java.util.List;

import org.drools.model.Index.ConstraintType;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.junit.Test;
import org.kie.api.runtime.KieSession;

import static java.util.Arrays.asList;
import static org.drools.model.DSL.*;
import static org.drools.model.functions.accumulate.Average.avg;
import static org.drools.model.functions.accumulate.Sum.sum;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FlowTest {

    @Test
    public void testAlpha() {
        Result result = new Result();

        Variable<Person> markV = variableOf( type( Person.class ) );

        Rule rule = rule("alpha")
                .view(
                        expr(markV, mark -> mark.getName().equals("Mark"))
                    )
                .then(c -> c.on(markV)
                           .execute(p -> result.value = p.getName()));

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(rule);

        KieSession ksession = kieBase.newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));

        ksession.fireAllRules();
        assertEquals("Mark", result.value);
    }

    @Test
    public void testBeta() {
        Result result = new Result();

        List<String> list = new ArrayList<>();
        Variable<Person> markV = variableOf( type( Person.class ) );
        Variable<Person> olderV = variableOf( type( Person.class ) );

        Rule rule = rule("beta")
                .view(
                        expr(markV, p -> p.getName().equals("Mark")).indexedBy( String.class, ConstraintType.EQUAL, Person::getName, "Mark" ),
                        expr(olderV, p -> !p.getName().equals("Mark")),
                        expr(olderV, markV, (p1, p2) -> p1.getAge() > p2.getAge())
                    )
                .then(c -> c.on(olderV, markV)
                           .execute((p1, p2) -> result.value = p1.getName() + " is older than " + p2.getName()));

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(rule);

        KieSession ksession = kieBase.newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));

        ksession.fireAllRules();
        assertEquals("Mario is older than Mark", result.value);
    }

    @Test
    public void testNot() {
        Result result = new Result();

        List<String> list = new ArrayList<>();
        Variable<Person> oldestV = variableOf( type( Person.class ) );
        Variable<Person> otherV = variableOf( type( Person.class ) );

        Rule rule = rule("not")
                .view(
                        not(otherV, oldestV, (p1, p2) -> p1.getAge() > p2.getAge())
                    )
                .then(c -> c.on(oldestV)
                           .execute(p -> result.value = "Oldest person is " + p.getName()));

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(rule);

        KieSession ksession = kieBase.newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));

        ksession.fireAllRules();
        assertEquals("Oldest person is Mario", result.value);
    }

    @Test
    public void testAccumulate() {
        Result result = new Result();

        Variable<Person> person = variableOf( type( Person.class ) );
        Variable<Integer> resultSum = variableOf( type( Integer.class ) );
        Variable<Double> resultAvg = variableOf( type( Double.class ) );

        Rule rule = rule("accumulate")
                .view(
                        accumulate(expr(person, p -> p.getName().startsWith("M")),
                                   sum(Person::getAge).as(resultSum),
                                   avg(Person::getAge).as(resultAvg))
                     )
                .then(
                        on(resultSum, resultAvg)
                                .execute((sum, avg) -> result.value = "total = " + sum + "; average = " + avg)
                     );

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(rule);

        KieSession ksession = kieBase.newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));

        ksession.fireAllRules();
        assertEquals("total = 77; average = 38.5", result.value);
    }

    public static int findAge(Person person) {
        return person.getAge();
    }

    @Test
    public void testInlineInvocation() {
        Result result = new Result();

        Variable<Person> mark = variableOf( type( Person.class ) );
        Variable<Integer> age = variableOf( type( Integer.class ) );

        Rule rule = rule("SyncInvocation")
                .view(
                        input(mark),
                        expr(mark, person -> person.getName().equals("Mark")),
                        set(age).invoking(mark, FlowTest::findAge)
                     )
                .then(on(mark, age)
                              .execute((m, a) -> result.value = m + " is " + a + " years old"));

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(rule);

        KieSession ksession = kieBase.newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));

        ksession.fireAllRules();
        assertEquals("Mark is 37 years old", result.value);
    }

    @Test
    public void testInlineInvocationIterable() {
        List<String> result = new ArrayList<String>();

        Variable<Person> mario = variableOf( type( Person.class ) );
        Variable<Person> parent = variableOf( type( Person.class ) );

        Rule rule = rule("SyncInvocation")
                .view(
                        input(mario),
                        expr(mario, person -> person.getName().equals("Mario")),
                        set(parent).in(mario, Person::getParents)
                     )
                .then(on(mario, parent)
                              .execute((m, p) -> result.add(p + " is parent of " + m)));

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(rule);

        KieSession ksession = kieBase.newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40)
                                .setParents(asList(new Person("Mimmo", 75),
                                                   new Person("Tina", 65))));

        ksession.fireAllRules();
        assertEquals(2, result.size());
        assertTrue(result.contains("Mimmo is parent of Mario"));
        assertTrue(result.contains("Tina is parent of Mario"));
    }

    private static class Result {
        Object value;
    }
}
