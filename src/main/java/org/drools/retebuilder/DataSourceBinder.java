package org.drools.retebuilder;

import org.drools.datasource.DataSource;
import org.drools.datasource.Observable;
import org.drools.retebuilder.nodes.DataStreamNode;
import org.kie.api.runtime.KieSession;

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
            throw new RuntimeException("binding of passive DataSource still unsopported");
        }
    }
}
