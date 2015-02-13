package org.drools.retebuilder;

import org.drools.datasource.DataSource;
import org.drools.datasource.DataStore;
import org.drools.datasource.Observable;
import org.drools.retebuilder.nodes.DataStreamNode;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;

public class DataSourceBinder {

    public static void bindDataSource(KieSession kSession, String dataSourceName, DataSource dataSource) {
        CanonicalKieBase kieBase = (CanonicalKieBase)kSession.getKieBase();
        if (dataSource instanceof Observable) {
            DataStreamNode streamNode = kieBase.getDataStreamNode(dataSourceName);
            if (streamNode == null) {
                throw new RuntimeException("Unknown data source: " + dataSourceName);
            }
            streamNode.registerDataStreamObserver(kSession, (Observable)dataSource);
        } else {
            EntryPoint entryPoint = kSession.getEntryPoint(dataSourceName);
            if (entryPoint == null) {
                throw new RuntimeException("Unknown data source: " + dataSourceName);
            }
            for (Object obj : ((DataStore) dataSource).getObjects()) {
                entryPoint.insert(obj);
            }
        }
    }
}
