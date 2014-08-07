package org.drools.retebuilder.nodes;

import org.drools.core.common.EmptyBetaConstraints;
import org.drools.core.reteoo.FromNode;
import org.drools.core.rule.From;
import org.drools.core.spi.AlphaNodeFieldConstraint;
import org.drools.core.spi.DataProvider;
import org.drools.retebuilder.CanonicalBuildContext;

public class SyncInvokerNode extends FromNode {

    public SyncInvokerNode() { }

    public SyncInvokerNode(CanonicalBuildContext context, DataProvider dataProvider, From from) {
        super(context.getNextId(),
              dataProvider,
              context.getTupleSource(),
              new AlphaNodeFieldConstraint[0],
              EmptyBetaConstraints.getInstance(),
              context.isTupleMemoryEnabled(),
              context,
              from);
    }


}
