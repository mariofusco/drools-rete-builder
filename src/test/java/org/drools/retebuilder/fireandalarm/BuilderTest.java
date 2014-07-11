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

        Rule when_there_is_a_fire_turn_on_the_sprinkler = rule(
                view(
                        p -> p.filter(fire),
                        p -> p.filter(sprinkler)
                              .with(s -> !s.isOn())
                              .and(sprinkler, fire, (s, f) -> s.getRoom().equals(f.getRoom()))
                    ),
                then(c -> c.on(sprinkler)
                           .execute(s -> {
                               System.out.println("Turn on the sprinkler for room " + s.getRoom().getName());
                               s.setOn(true);
                           })
                           .update(sprinkler, "on")
                    )
        );

        Rule raise_the_alarm_when_we_have_one_or_more_fires = rule(
                view(
                        exists(p -> p.filter(fire))
                    ),
                then(c -> c.execute(() -> System.out.println("Raise the alarm"))
                           .insert(() -> new Alarm())
                    )
        );

        Rule when_the_fire_is_gone_turn_off_the_sprinkler = rule(
                view(
                        p -> p.filter(sprinkler)
                              .with(s -> s.isOn()),
                        not(p -> p.filter(fire)
                                  .with(sprinkler, (f, s) -> f.getRoom().equals(s.getRoom()))
                           )
                    ),
                then(c -> c.on(sprinkler)
                           .execute(s -> {
                               System.out.println("Turn off the sprinkler for room " + s.getRoom().getName());
                               s.setOn(false);
                           })
                           .update(sprinkler, "on")
                    )
        );

        Rule lower_the_alarm_when_all_the_fires_have_gone = rule(
                view(
                       p -> p.filter(alarm),
                       not(p -> p.filter(fire))
                    ),
                then(c -> c.execute(() -> System.out.println("Lower the alarm"))
                           .delete(alarm)
                    )
        );

        Rule status_output_when_things_are_ok = rule(
                view(
                       not(p -> p.filter(alarm)),
                       not(p -> p.filter(sprinkler).with(Sprinkler::isOn))
                    ),
                then(
                        c -> c.execute(() -> System.out.println("Everything is ok"))
                    )
        );

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRule(when_there_is_a_fire_turn_on_the_sprinkler);
        kieBase.addRule(raise_the_alarm_when_we_have_one_or_more_fires);
        kieBase.addRule(when_the_fire_is_gone_turn_off_the_sprinkler);
        kieBase.addRule(lower_the_alarm_when_all_the_fires_have_gone);
        kieBase.addRule(status_output_when_things_are_ok);

        dumpRete(kieBase);

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
