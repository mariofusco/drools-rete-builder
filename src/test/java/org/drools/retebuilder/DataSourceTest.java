package org.drools.retebuilder;

import org.drools.model.DataStream;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.junit.Test;
import org.kie.api.runtime.KieSession;

import java.util.concurrent.atomic.AtomicReference;

import static org.drools.model.DSL.*;
import static org.drools.model.flow.FlowDSL.*;
import static org.junit.Assert.assertEquals;

public class DataSourceTest {

    @Test
    public void testDataStreamInsert() {
        DataStream persons = newDataStream();

        AtomicReference<String> result = new AtomicReference<String>();

        Variable<Person> mark = bind(typeOf(Person.class));
        Variable<Integer> age = bind(typeOf(Integer.class));

        Rule rule = rule("R")
                .view(
                        input(mark, () -> persons),
                        expr(mark, person -> person.getName().equals("Mark")),
                        set(age).invoking(mark, Person::getAge)
                     )
                .then(on(mark, age)
                              .execute((m, a) -> result.set( m + " is " + a + " years old" )));

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(rule);

        KieSession ksession = kieBase.newKieSession();

        persons.insert(new Person("Mark", 37));
        persons.insert(new Person("Edson", 35));
        persons.insert(new Person("Mario", 40));

        ksession.fireAllRules();
        assertEquals("Mark is 37 years old", result.get());
    }

    @Test
    public void testDataStreamOf() {
        AtomicReference<String> result = new AtomicReference<String>();

        Variable<Person> edson = bind(typeOf(Person.class));
        Variable<Integer> age = bind(typeOf(Integer.class));

        Rule rule = rule("R")
                .view(
                        input(edson, () -> streamOf(new Person("Mark", 37),
                                                    new Person("Edson", 35),
                                                    new Person("Mario", 40))),
                        expr(edson, person -> person.getName().equals("Edson")),
                        set(age).invoking(edson, Person::getAge)
                     )
                .then(on(edson, age)
                              .execute((e, a) -> result.set( e + " is " + a + " years old" )));

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(rule);

        KieSession ksession = kieBase.newKieSession();

        ksession.fireAllRules();
        assertEquals("Edson is 35 years old", result.get());
    }
}