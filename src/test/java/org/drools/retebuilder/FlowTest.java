package org.drools.retebuilder;

import org.drools.model.DataSource;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.junit.Test;
import org.kie.api.runtime.KieSession;

import java.util.ArrayList;
import java.util.List;

import static org.drools.model.DSL.bind;
import static org.drools.model.DSL.typeOf;
import static org.drools.model.DSL.rule;
import static org.drools.model.flow.FlowDSL.*;
import static org.drools.model.functions.accumulate.Average.avg;
import static org.drools.model.functions.accumulate.Sum.sum;
import static org.drools.model.impl.DataSourceImpl.sourceOf;
import static org.junit.Assert.assertEquals;

public class FlowTest {

    @Test
    public void testAlpha() {
        DataSource persons = sourceOf(new Person("Mark", 37),
                                      new Person("Edson", 35),
                                      new Person("Mario", 40));

        Result result = new Result();

        Variable<Person> markV = bind(typeOf(Person.class));

        Rule rule = rule("alpha")
                .view(
                        input(markV, () -> persons),
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
        DataSource persons = sourceOf(new Person("Mark", 37),
                                      new Person("Edson", 35),
                                      new Person("Mario", 40));

        Result result = new Result();

        List<String> list = new ArrayList<>();
        Variable<Person> markV = bind(typeOf(Person.class));
        Variable<Person> olderV = bind(typeOf(Person.class));

        Rule rule = rule("beta")
                .view(
                        input(markV, () -> persons),
                        input(olderV, () -> persons),
                        expr(markV, mark -> mark.getName().equals("Mark")),
                        expr(olderV, older -> !older.getName().equals("Mark")),
                        expr(olderV, markV, (older, mark) -> older.getAge() > mark.getAge())
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
        DataSource persons = sourceOf(new Person("Mark", 37),
                                      new Person("Edson", 35),
                                      new Person("Mario", 40));

        Result result = new Result();

        List<String> list = new ArrayList<>();
        Variable<Person> oldestV = bind(typeOf(Person.class));
        Variable<Person> otherV = bind(typeOf(Person.class));

        Rule rule = rule("not")
                .view(
                        input(oldestV, () -> persons),
                        input(otherV, () -> persons),
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
        DataSource persons = sourceOf(new Person("Mark", 37),
                                      new Person("Edson", 35),
                                      new Person("Mario", 40));

        Result result = new Result();

        Variable<Person> person = bind(typeOf(Person.class));
        Variable<Integer> resultSum = bind(typeOf(Integer.class));
        Variable<Double> resultAvg = bind(typeOf(Double.class));

        Rule rule = rule("accumulate")
                .view(
                        input(person, () -> persons),
                        accumulate(expr(person, p -> p.getName().startsWith("M")),
                                   sum(Person::getAge).as(resultSum),
                                   avg(Person::getAge).as(resultAvg))
                     )
                .then(c -> c.on(resultSum, resultAvg)
                            .execute((sum, avg) -> result.value = "total = " + sum + "; average = " + avg));

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(rule);

        KieSession ksession = kieBase.newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));

        ksession.fireAllRules();
        assertEquals("total = 77; average = 38.5", result.value);
    }

    private static class Result {
        Object value;
    }
}
