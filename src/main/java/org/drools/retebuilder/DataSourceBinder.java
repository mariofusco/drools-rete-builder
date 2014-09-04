package org.drools.retebuilder;

import org.drools.datasource.Observable;
import org.drools.retebuilder.nodes.DataStreamNode;
import org.kie.api.runtime.KieSession;

public class DataSourceBinder {

    public static void bindDataSource(KieSession kSession, String dataSourceName, Observable dataSource) {
        CanonicalKieBase kieBase = (CanonicalKieBase)kSession.getKieBase();
        DataStreamNode streamNode = kieBase.getDataStreamNode(dataSourceName);
        streamNode.registerDataStreamObserver(kSession, dataSource);
    }
}
