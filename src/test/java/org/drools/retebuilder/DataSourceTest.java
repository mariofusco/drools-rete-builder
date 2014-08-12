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
    public void testDataSourceInsert() {
        DataStream persons = newDataStream();

        AtomicReference<String> result = new AtomicReference<String>();

        Variable<Person> mark = bind(typeOf(Person.class));
        Variable<Integer> age = bind(typeOf(Integer.class));

        Rule rule = rule("SyncInvocation")
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
}