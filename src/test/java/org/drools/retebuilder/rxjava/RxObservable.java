package org.drools.retebuilder.rxjava;

import org.drools.datasource.ReactiveDataSource;
import org.drools.datasource.impl.AbstractObservable;
import org.kie.api.runtime.KieSession;
import rx.Observable;
import rx.Observer;

import static org.drools.retebuilder.DataSourceBinder.bindDataSource;

public class RxObservable<T> extends AbstractObservable implements ReactiveDataSource<T> {

    public RxObservable(Observable<T> observable) {
        observable.subscribe(new Observer<T>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable throwable) {
                throw new UnsupportedOperationException("onError");
            }

            @Override
            public void onNext(T o) {
                System.out.println("insert -> " + o);
                notifyInsert(o);
            }
        });
    }

    public static <T> RxObservable<T> rxObservable(Observable<T> observable) {
        return new RxObservable<T>(observable);
    }

    public static <T> void bindRxObservable(KieSession kSession, String dataSourceName, Observable<T> observable) {
        bindDataSource(kSession, dataSourceName, rxObservable(observable));
    }
}
