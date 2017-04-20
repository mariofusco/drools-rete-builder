package org.drools.retebuilder.rxjava;

import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.datasources.DataStore;
import org.drools.retebuilder.CanonicalKieBase;
import org.drools.retebuilder.Person;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import rx.Observable;
import rx.Observer;

import static org.drools.model.DSL.*;
import static org.drools.retebuilder.DataSourceBinder.bindDataSource;
import static org.drools.retebuilder.rxjava.RxObservable.bindRxObservable;

public class RxObservableTest {
    @Test
    public void testDataStream() {
        Variable<TempInfo> temp = any( TempInfo.class );
        Variable<Person> person = any( Person.class );

        Rule r1 = rule("low temp")
                .view(
                        subscribe(temp, "tempFeed"),
                        expr(temp, t -> t.getTemp() < 0),
                        input(person, "persons"),
                        expr(person, temp, (p, t) -> p.getTown().equals(t.getTown()))
                     )
                .then(on(person, temp)
                              .execute((p, t) -> System.out.println(p.getName() + " is freezing in " + p.getTown() + " - temp is " + t.getTemp())));

        Rule r2 = rule("high temp")
                .view(
                        subscribe(temp, "tempFeed"),
                        expr(temp, t -> t.getTemp() > 30),
                        input(person, "persons"),
                        expr(person, temp, (p, t) -> p.getTown().equals(t.getTown()))
                     )
                .then(on(person, temp)
                              .execute((p, t) -> System.out.println(p.getName() + " is sweating in " + p.getTown() + " - temp is " + t.getTemp())));

        CanonicalKieBase kieBase = new CanonicalKieBase();
        kieBase.addRules(r1, r2);

        final KieSession ksession = kieBase.newKieSession();

        DataStore persons = storeOf( new Person( "Mark", 37, "London"),
                                     new Person("Edson", 35, "Toronto"),
                                     new Person("Mario", 40, "Milano") );
        bindDataSource(ksession, "persons", persons);

        Observable<TempInfo> tempFeed = TempServer.getFeeds( "Milano", "London", "Toronto" );
        bindRxObservable( ksession, "tempFeed", tempFeed );

        tempFeed.subscribe(new Observer<TempInfo>() {
            @Override
            public void onCompleted() {
                ksession.halt();
            }

            @Override
            public void onError(Throwable throwable) { }

            @Override
            public void onNext(TempInfo tempInfo) {
            }
        });

        ksession.fireUntilHalt();
    }
}
