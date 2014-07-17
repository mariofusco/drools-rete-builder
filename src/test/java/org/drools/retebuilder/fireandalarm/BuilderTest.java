package org.drools.retebuilder.fireandalarm;

import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.retebuilder.CanonicalKieBase;
import org.drools.retebuilder.fireandalarm.model.Alarm;
import org.drools.retebuilder.fireandalarm.model.Fire;
import org.drools.retebuilder.fireandalarm.model.Room;
import org.drools.retebuilder.fireandalarm.model.Sprinkler;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import static org.drools.model.DSL.*;
import static org.drools.model.DSL.not;
import static org.drools.retebuilder.ReteDumper.dumpRete;
import static org.junit.Assert.assertTrue;

public class BuilderTest {

    @Test
    public void testFireAndAlarm() {
        Variable<Room> room = bind(typeOf(Room.class));
        Variable<Fire> fire = bind(typeOf(Fire.class));
        Variable<Sprinkler> sprinkler = bind(typeOf(Sprinkler.class));
        Variable<Alarm> alarm = bind(typeOf(Alarm.class));

        Rule r1 = rule("When there is a fire turn on the sprinkler")
                .when(
                        p -> p.filter(fire),
                        p -> p.filter(sprinkler)
                              .with(s -> !s.isOn())
                              .and(sprinkler, fire, (s, f) -> s.getRoom().equals(f.getRoom()))
                    )
                .then(c -> c.on(sprinkler)
                           .execute(s -> {
                               System.out.println("Turn on the sprinkler for room " + s.getRoom().getName());
                               s.setOn(true);
                           })
                           .update(sprinkler, "on")
                    );

        Rule r2 = rule("When the fire is gone turn off the sprinkler")
                .when(
                        p -> p.filter(sprinkler)
                              .with(s -> s.isOn()),
                        not(p -> p.filter(fire)
                                  .with(sprinkler, (f, s) -> f.getRoom().equals(s.getRoom()))
                           )
                     )
                .then(c -> c.on(sprinkler)
                            .execute(s -> {
                                System.out.println("Turn off the sprinkler for room " + s.getRoom().getName());
                                s.setOn(false);
                            })
                            .update(sprinkler, "on")
                     );

        Rule r3 = rule("Raise the alarm when we have one or more fires")
                .when(
                        exists(p -> p.filter(fire))
                    )
                .then(c -> c.execute(() -> System.out.println("Raise the alarm"))
                           .insert(() -> new Alarm())
                    );

        Rule r4 = rule("Lower the alarm when all the fires have gone")
                .when(
                        not(p -> p.filter(fire)),
                        p -> p.filter(alarm)
                    )
                .then(c -> c.execute(() -> System.out.println("Lower the alarm"))
                           .delete(alarm)
                    );

        Rule r5 = rule("Status output when things are ok")
                .when(
                       not(p -> p.filter(alarm)),
                       not(p -> p.filter(sprinkler).with(Sprinkler::isOn))
                    )
                .then(
                        c -> c.execute(() -> System.out.println("Everything is ok"))
                    );

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(r1, r2, r3, r4, r5);

        //dumpRete(kieBase);

        KieSession ksession = kieBase.newKieSession();

        // phase 1
        Room room1 = new Room("Room 1");
        ksession.insert(room1);
        FactHandle fireFact1 = ksession.insert(new Fire(room1));
        ksession.fireAllRules();

        // phase 2
        Sprinkler sprinkler1 = new Sprinkler(room1);
        ksession.insert(sprinkler1);
        ksession.fireAllRules();

        assertTrue(sprinkler1.isOn());

        // phase 3
        ksession.delete(fireFact1);
        ksession.fireAllRules();
    }
}
