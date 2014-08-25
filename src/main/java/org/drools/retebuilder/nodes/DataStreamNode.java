package org.drools.retebuilder.nodes;

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
import org.drools.core.spi.RuleComponent;
import org.drools.model.DataStream;
import org.drools.model.functions.Function0;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieSession;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

public class DataStreamNode extends ObjectTypeNode {

    private final EntryPointId entryPointId;
    private final Function0<DataStream> dataStreamSupplier;

    public DataStreamNode(final ObjectType objectType,
                          final BuildContext context,
                          final Function0<DataStream> dataStreamSupplier) {
        this.id = context.getNextId();
        this.partitionId = context.getPartitionId();
        this.partitionsEnabled = context.getKnowledgeBase().getConfiguration().isMultithreadEvaluation();
        this.associations = new HashMap<Rule, RuleComponent>();

        this.sink = EmptyObjectSinkAdapter.getInstance();
        this.objectType = objectType;
        setObjectMemoryEnabled( context.isObjectTypeNodeMemoryEnabled() );

        this.dataStreamSupplier = dataStreamSupplier;
        this.entryPointId = new EntryPointId( UUID.randomUUID().toString() );
        new DataStreamEntryPointNode(context, entryPointId);
    }

    @Override
    public int hashCode() {
        return this.objectType.hashCode();
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

    public void registerDataStreamObserver(KieSession kieSession) {
        new DataStreamObserver((InternalWorkingMemory)kieSession, this);
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

    public static class DataStreamObserver implements Observer {

        private final InternalWorkingMemory workingMemory;
        private final DataStreamNode dataStreamNode;
        private final ObjectTypeConfigurationRegistry typeConfReg;
        private final FactHandleFactory handleFactory;
        private final PropagationContextFactory pctxFactory;

        public DataStreamObserver(InternalWorkingMemory workingMemory, DataStreamNode dataStreamNode) {
            this.workingMemory = workingMemory;
            this.dataStreamNode = dataStreamNode;

            this.typeConfReg = new ObjectTypeConfigurationRegistry(workingMemory.getKnowledgeBase());
            this.handleFactory = workingMemory.getFactHandleFactory();
            this.pctxFactory = workingMemory.getKnowledgeBase().getConfiguration().getComponentFactory().getPropagationContextFactory();

            dataStreamNode.dataStreamSupplier.apply().addObserver(this);
        }

        @Override
        public void update(Observable observable, Object object) {
            ObjectTypeConf typeConf = typeConfReg.getObjectTypeConf( dataStreamNode.entryPointId,
                                                                     object );

            InternalFactHandle factHandle = handleFactory.newFactHandle( object,
                                                                         typeConf,
                                                                         workingMemory,
                                                                         null );


            final PropagationContext pctx = this.pctxFactory.createPropagationContext(workingMemory.getNextPropagationIdCounter(),
                                                                                      PropagationContext.INSERTION,
                                                                                      null, // rule,
                                                                                      null, // activation.getTuple(),
                                                                                      factHandle,
                                                                                      dataStreamNode.entryPointId);

            dataStreamNode.sink.propagateAssertObject( factHandle,
                                                       pctx,
                                                       workingMemory );
        }
    }
}
