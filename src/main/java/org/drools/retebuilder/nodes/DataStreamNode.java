package org.drools.retebuilder.nodes;

import java.util.UUID;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.ObjectTypeConfigurationRegistry;
import org.drools.core.common.PropagationContextFactory;
import org.drools.core.reteoo.EmptyObjectSinkAdapter;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.ObjectTypeConf;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.rule.EntryPointId;
import org.drools.core.spi.FactHandleFactory;
import org.drools.core.spi.ObjectType;
import org.drools.core.spi.PropagationContext;
import org.drools.core.util.Bag;
import org.drools.datasource.DataSourceObserver;
import org.drools.datasource.Observable;
import org.drools.model.DataSourceDefinition;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieSession;

public class DataStreamNode extends ObjectTypeNode {

    private final EntryPointId entryPointId;
    private final DataSourceDefinition dataSourceDef;

    public DataStreamNode(final ObjectType objectType,
                          final BuildContext context,
                          final DataSourceDefinition dataSourceDef) {
        this.id = context.getNextId();
        this.partitionId = context.getPartitionId();
        this.partitionsEnabled = context.getKnowledgeBase().getConfiguration().isMultithreadEvaluation();
        this.associations = new Bag<Rule>();

        this.sink = EmptyObjectSinkAdapter.getInstance();
        this.objectType = objectType;
        setObjectMemoryEnabled( context.isObjectTypeNodeMemoryEnabled() );

        this.dataSourceDef = dataSourceDef;
        this.entryPointId = new EntryPointId( UUID.randomUUID().toString() );
        new DataStreamEntryPointNode(context, entryPointId);
    }

    @Override
    public boolean equals(final Object object) {
        if ( this == object ) {
            return true;
        }

        if ( object == null || !(object instanceof DataStreamNode) ) {
            return false;
        }

        final DataStreamNode other = (DataStreamNode) object;
        return this.objectType.equals(other.objectType);
    }

    public void registerDataStreamObserver(KieSession kieSession, Observable observable) {
        new DataStreamObserver((InternalWorkingMemory)kieSession, this, observable);
    }

    public static class DataStreamEntryPointNode extends EntryPointNode {

        DataStreamEntryPointNode(BuildContext context, EntryPointId entryPointId) {
            super(context.getNextId(),
                  context.getPartitionId(),
                  context.getKnowledgeBase().getConfiguration().isMultithreadEvaluation(),
                  context.getKnowledgeBase().getRete(),
                  entryPointId);
            context.getKnowledgeBase().getRete().addObjectSink(this);
        }

    }

    public static class DataStreamObserver implements DataSourceObserver {

        private final InternalWorkingMemory workingMemory;
        private final DataStreamNode dataStreamNode;
        private final ObjectTypeConfigurationRegistry typeConfReg;
        private final FactHandleFactory handleFactory;
        private final PropagationContextFactory pctxFactory;

        public DataStreamObserver(InternalWorkingMemory workingMemory, DataStreamNode dataStreamNode, Observable observable) {
            this.workingMemory = workingMemory;
            this.dataStreamNode = dataStreamNode;

            this.typeConfReg = new ObjectTypeConfigurationRegistry(workingMemory.getKnowledgeBase());
            this.handleFactory = workingMemory.getFactHandleFactory();
            this.pctxFactory = workingMemory.getKnowledgeBase().getConfiguration().getComponentFactory().getPropagationContextFactory();

            observable.addObserver(this);
        }

        @Override
        public boolean objectInserted(Object object) {
            ObjectTypeConf typeConf = typeConfReg.getObjectTypeConf( dataStreamNode.entryPointId,
                                                                     object );

            InternalFactHandle factHandle = handleFactory.newFactHandle( object,
                                                                         typeConf,
                                                                         workingMemory,
                                                                         null );


            final PropagationContext pctx = this.pctxFactory.createPropagationContext(workingMemory.getNextPropagationIdCounter(),
                                                                                      PropagationContext.Type.INSERTION,
                                                                                      null, // rule,
                                                                                      null, // activation.getTuple(),
                                                                                      factHandle,
                                                                                      dataStreamNode.entryPointId);

            dataStreamNode.sink.propagateAssertObject( factHandle,
                                                       pctx,
                                                       workingMemory );
            workingMemory.notifyWaitOnRest();
            return true;
        }

        @Override
        public boolean objectUpdated(Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean objectDeleted(Object obj) {
            throw new UnsupportedOperationException();
        }
    }
}
