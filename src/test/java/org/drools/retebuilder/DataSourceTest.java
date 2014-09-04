package org.drools.retebuilder;

import org.drools.datasource.DataStream;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.junit.Test;
import org.kie.api.runtime.KieSession;

import java.util.concurrent.atomic.AtomicReference;

import static org.drools.model.DSL.*;
import static org.drools.retebuilder.DataSourceBinder.bindDataSource;
import static org.junit.Assert.assertEquals;

public class DataSourceTest {

    @Test
    public void testDataStream() {
        AtomicReference<String> result = new AtomicReference<String>();

        Variable<Person> mark = bind(typeOf(Person.class));
        Variable<Integer> age = bind(typeOf(Integer.class));

        Rule rule = rule("R")
                .view(
                        subscribe(mark, "persons"),
                        expr(mark, person -> person.getName().equals("Mark")),
                        set(age).invoking(mark, Person::getAge)
                     )
                .then(on(mark, age)
                              .execute((m, a) -> result.set( m + " is " + a + " years old" )));

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(rule);

        KieSession ksession = kieBase.newKieSession();

        DataStream persons = newDataStream();
        bindDataSource(ksession, "persons", persons);

        persons.send(new Person("Mark", 37));
        persons.send(new Person("Edson", 35));
        persons.send(new Person("Mario", 40));

        ksession.fireAllRules();
        assertEquals("Mark is 37 years old", result.get());
    }

}