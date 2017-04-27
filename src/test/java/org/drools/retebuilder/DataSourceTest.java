package org.drools.retebuilder;

import java.util.concurrent.atomic.AtomicReference;

import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.datasources.DataStore;
import org.drools.model.datasources.DataStream;
import org.junit.Test;
import org.kie.api.runtime.KieSession;

import static org.drools.model.DSL.*;
import static org.drools.retebuilder.DataSourceBinder.bindDataSource;
import static org.junit.Assert.assertEquals;

public class DataSourceTest {

    @Test
    public void testDataStream() {
        AtomicReference<String> result = new AtomicReference<String>();

        Variable<Person> mark = variableOf( type( Person.class ) );
        Variable<Integer> age = variableOf( type( Integer.class ) );

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

    @Test
    public void testDataStore() {
        AtomicReference<String> result = new AtomicReference<String>();

        Variable<Person> mark = variableOf( type( Person.class ) );
        Variable<Integer> age = variableOf( type( Integer.class ) );

        Rule rule = rule("R")
                .view(
                        input(mark, "persons"),
                        expr(mark, person -> person.getName().equals("Mark")),
                        set(age).invoking(mark, Person::getAge)
                     )
                .then(on(mark, age)
                              .execute((m, a) -> result.set( m + " is " + a + " years old" )));

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(rule);

        KieSession ksession = kieBase.newKieSession();

        DataStore persons = storeOf( new Person( "Mark", 37),
                                     new Person("Edson", 35),
                                     new Person("Mario", 40) );
        bindDataSource(ksession, "persons", persons);

        ksession.fireAllRules();
        assertEquals("Mark is 37 years old", result.get());
    }

}