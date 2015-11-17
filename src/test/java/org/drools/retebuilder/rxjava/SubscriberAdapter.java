/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.retebuilder.rxjava;

import org.drools.datasource.ReactiveDataSource;
import org.drools.datasource.impl.AbstractObservable;
import org.kie.api.runtime.KieSession;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import static org.drools.retebuilder.DataSourceBinder.bindDataSource;

public class SubscriberAdapter<T> extends AbstractObservable implements ReactiveDataSource<T> {
    public SubscriberAdapter(Publisher<T> publisher) {
        publisher.subscribe(new Subscriber<T>() {
            @Override
            public void onError(Throwable throwable) {
                throw new UnsupportedOperationException("onError");
            }

            @Override
            public void onComplete() {
                throw new UnsupportedOperationException( "onComplete" );
            }

            @Override
            public void onSubscribe( Subscription subscription ) {
                subscription.request( Long.MAX_VALUE );
            }

            @Override
            public void onNext(T o) {
                notifyInsert(o);
            }
        });
    }

    public static <T> SubscriberAdapter<T> subscribe(Publisher<T> publisher) {
        return new SubscriberAdapter<T>(publisher);
    }

    public static <T> void bindPublisher(KieSession kSession, String dataSourceName, Publisher<T> publisher) {
        bindDataSource(kSession, dataSourceName, subscribe( publisher ));
    }
}
