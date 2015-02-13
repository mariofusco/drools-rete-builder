package org.drools.retebuilder.rxjava;

import rx.Observable;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class TempServer {

    public static Observable<TempInfo> getTemp1(String... towns) {
        return Observable.from(Stream.of(towns).map(town -> TempInfo.fetch(town)).collect(toList()));
    }

    public static Observable<TempInfo> getTemp(String town) {
        return Observable.create(subscriber -> subscriber.onNext(TempInfo.fetch(town)));
    }

    public static Observable<TempInfo> getFeed(String town) {
        return Observable.create(subscriber ->
                                         Observable.interval(1, TimeUnit.SECONDS)
                                                   .subscribe(i -> {
                                                       if (i > 5) subscriber.onCompleted();
                                                       try {
                                                           subscriber.onNext(TempInfo.fetch(town));
                                                       } catch (Exception e) {
                                                           subscriber.onError(e);
                                                       }
                                                   }));
    }

    public static Observable<TempInfo> getFeeds(String... towns) {
        return Observable.merge(Arrays.stream(towns)
                                      .map(TempServer::getFeed)
                                      .collect(toList()));
    }

    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch(Exception ex) {}
    }
}