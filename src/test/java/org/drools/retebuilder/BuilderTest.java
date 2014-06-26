package org.drools.retebuilder;

import org.drools.model.DataSource;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.junit.Test;
import org.kie.api.runtime.KieSession;

import java.util.ArrayList;
import java.util.List;

import static org.drools.model.DSL.*;
import static org.drools.model.functions.accumulate.Average.avg;
import static org.drools.model.functions.accumulate.Sum.sum;
import static org.drools.model.impl.DataSourceImpl.sourceOf;
import static org.junit.Assert.assertEquals;

public class BuilderTest {

    @Test
    public void testAlpha() {
        DataSource persons = sourceOf(new Person("Mark", 37),
                                      new Person("Edson", 35),
                                      new Person("Mario", 40));

        Result result = new Result();

        Variable<Person> mark = bind(typeOf(Person.class));

        Rule rule = rule(
                view(p -> p.filter(mark)
                           .with(person -> person.getName().equals("Mark"))
                           .from(persons)),
                then(c -> c.on(mark)
                           .execute(p -> result.value = p.getName()))
        );

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRule(rule);

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
        Variable<Person> mark = bind(typeOf(Person.class));
        Variable<Person> older = bind(typeOf(Person.class));

        Rule rule = rule(
                view(
                        p -> p.filter(mark)
                              .with(person -> person.getName().equals("Mark"))
                              .from(persons),
                        p -> p.filter(older)
                              .with(older, mark, (p1, p2) -> p1.getAge() > p2.getAge())
                              .from(persons)
                    ),
                then(c -> c.on(older, mark)
                           .execute((p1, p2) -> result.value = p1.getName() + " is older than " + p2.getName()))
        );

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRule(rule);

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
        Variable<Person> oldest = bind(typeOf(Person.class));

        Rule rule = rule(
                view(
                        p -> p.filter(oldest)
                              .from(persons),
                        not(p -> p.filter(typeOf(Person.class))
                                  .with(oldest, (p1, p2) -> p1.getAge() > p2.getAge())
                                  .from(persons))
                    ),
                then(c -> c.on(oldest)
                           .execute(p -> result.value = "Oldest person is " + p.getName()))
        );

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRule(rule);

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

        Variable<Integer> resultSum = bind(typeOf(Integer.class));
        Variable<Double> resultAvg = bind(typeOf(Double.class));

        Rule rule = rule(
                view(
                        accumulate(p -> p.filter(typeOf(Person.class))
                                         .with(person -> person.getName().startsWith("M"))
                                         .from(persons),
                                   sum(Person::getAge).as(resultSum),
                                   avg(Person::getAge).as(resultAvg))
                    ),
                then(c -> c.on(resultSum, resultAvg)
                           .execute((sum, avg) -> result.value = "total = " + sum + "; average = " + avg))
        );

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRule(rule);

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

