package org.drools.retebuilder.rxjava;

import rx.Observable;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

public class TempServer {

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
}