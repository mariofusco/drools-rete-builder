package org.drools.retebuilder;

import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.Sink;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.junit.Test;

import static org.drools.model.DSL.*;
import static org.junit.Assert.assertEquals;

public class NodeSharingTest {

    @Test
    public void testNodeSharing() {
        Variable<Person> mark = bind(typeOf(Person.class));
        Variable<Person> older = bind(typeOf(Person.class));

        Rule r1 = rule("alpha")
                .when(
                        p -> p.filter(mark)
                              .with(person -> person.getName().equals("Mark"))
                     )
                .then(
                        on(mark)
                                .execute(p -> System.out.println(p.getName()))
                     );

        Rule r2 = rule("beta")
                .when(
                        p -> p.filter(mark)
                              .with(person -> person.getName().equals("Mark")),
                        p -> p.filter(older)
                              .with(older, mark, (p1, p2) -> p1.getAge() > p2.getAge())
                     )
                .then(
                        on(older, mark)
                            .execute((p1, p2) -> System.out.println(p1.getName() + " is older than " + p2.getName()))
                     );

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(r1, r2);

        EntryPointNode epNode = kieBase.getRete().getEntryPointNodes().values().iterator().next();
        ObjectTypeNode otn = epNode.getObjectTypeNodes().values().iterator().next();
        Sink[] sinks = otn.getSinkPropagator().getSinks();

        int alphaCounter = 0;
        for (Sink sink : sinks) {
            if (sink instanceof AlphaNode) {
                alphaCounter++;
            }
        }

        assertEquals(1, alphaCounter);
    }

    @Test
    public void testFlowNodeSharing() {
        Variable<Person> markV = bind(typeOf(Person.class));
        Variable<Person> olderV = bind(typeOf(Person.class));

        Rule r1 = rule("alpha")
                .view(
                        input(markV),
                        expr(markV, mark -> mark.getName().equals("Mark"))
                     )
                .then(
                        on(markV)
                                .execute(p -> System.out.println(p.getName()))
                     );

        Rule r2 = rule("beta")
                .view(
                        input(markV),
                        input(olderV),
                        expr(markV, mark -> mark.getName().equals("Mark")),
                        expr(olderV, markV, (older, mark) -> older.getAge() > mark.getAge())
                     )
                .then(
                        on(olderV, markV)
                            .execute((p1, p2) -> System.out.println(p1.getName() + " is older than " + p2.getName()))
                     );

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(r1, r2);

        EntryPointNode epNode = kieBase.getRete().getEntryPointNodes().values().iterator().next();
        ObjectTypeNode otn = epNode.getObjectTypeNodes().values().iterator().next();
        Sink[] sinks = otn.getSinkPropagator().getSinks();

        int alphaCounter = 0;
        for (Sink sink : sinks) {
            if (sink instanceof AlphaNode) {
                alphaCounter++;
            }
        }

        assertEquals(1, alphaCounter);
    }
}
