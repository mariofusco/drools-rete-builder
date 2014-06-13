package org.drools.retebuilder;

import org.drools.model.DataSource;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.junit.Test;
import org.kie.api.runtime.KieSession;

import java.util.ArrayList;
import java.util.List;

import static org.drools.model.DSL.*;
import static org.drools.model.impl.DataSourceImpl.sourceOf;

public class BuilderTest {

    @Test
    public void testAlpha() {
        DataSource persons = sourceOf(new Person("Mark", 37),
                                      new Person("Edson", 35),
                                      new Person("Mario", 40));

        Variable<Person> mark = bind(typeOf(Person.class));

        Rule rule = rule(
                view(p -> p.filter(mark)
                           .with(person -> person.getName().equals("Mark"))
                           .from(persons)),
                then(c -> c.on(mark)
                           .execute(System.out::println))
        );

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRule(rule);

        KieSession ksession = kieBase.newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));

        ksession.fireAllRules();
    }

    @Test
    public void testBeta() {
        DataSource persons = sourceOf(new Person("Mark", 37),
                                      new Person("Edson", 35),
                                      new Person("Mario", 40));

        List<String> list = new ArrayList<>();
        Variable<Person> mark = bind(typeOf(Person.class));
        Variable<Person> older = bind(typeOf(Person.class));

        Rule rule = rule(
                view(
                        p -> p.filter(mark)
                              .with(person -> person.getName().equals("Mark"))
                              .from(persons),
                        p -> p.using(mark).filter(older)
                              .with(older, mark, (p1, p2) -> p1.getAge() > p2.getAge())
                              .from(persons)
                    ),
                then(c -> c.on(older, mark)
                           .execute((p1, p2) -> System.out.println(p1.getName() + " is older than " + p2.getName())))
        );

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRule(rule);

        KieSession ksession = kieBase.newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));

        ksession.fireAllRules();
    }

    @Test
    public void testNot() {
        DataSource persons = sourceOf(new Person("Mark", 37),
                                      new Person("Edson", 35),
                                      new Person("Mario", 40));

        List<String> list = new ArrayList<>();
        Variable<Person> oldest = bind(typeOf(Person.class));

        Rule rule = rule(
                view(
                        p -> p.filter(oldest)
                              .from(persons),
                        not(p -> p.using(oldest).filter(typeOf(Person.class))
                                  .with(oldest, (p1, p2) -> p1.getAge() > p2.getAge())
                                  .from(persons))
                    ),
                then(c -> c.on(oldest)
                           .execute(p -> System.out.println("Oldest person is " + p.getName())))
        );

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRule(rule);

        KieSession ksession = kieBase.newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));

        ksession.fireAllRules();
    }
}

