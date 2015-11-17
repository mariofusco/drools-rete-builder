package org.drools.retebuilder.nodes;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.UpdateContext;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.reteoo.LeftTupleSink;
import org.drools.core.reteoo.LeftTupleSinkNode;
import org.drools.core.reteoo.LeftTupleSource;
import org.drools.core.reteoo.ModifyPreviousTuples;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.ReteooBuilder;
import org.drools.core.reteoo.RightTuple;
import org.drools.core.reteoo.RuleRemovalContext;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.spi.PropagationContext;

public class AsyncInvokerNode extends LeftTupleSource implements LeftTupleSinkNode {

    public AsyncInvokerNode() { }

    public AsyncInvokerNode(int id,
                            LeftTupleSource tupleSource,
                            BuildContext context) {
        super(id, context);
        setLeftTupleSource(tupleSource);

        initMasks(context, tupleSource);
    }

    @Override
    public void attach(BuildContext context) {
        this.leftInput.addTupleSink(this, context);
    }

    @Override
    public void networkUpdated(UpdateContext updateContext) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.networkUpdated -> TODO");
    }

    @Override
    protected boolean doRemove(RuleRemovalContext context, ReteooBuilder builder, InternalWorkingMemory[] workingMemories) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.doRemove -> TODO");
    }

    @Override
    public short getType() {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.getType -> TODO");
    }

    @Override
    public void assertLeftTuple(LeftTuple leftTuple, PropagationContext context, InternalWorkingMemory workingMemory) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.assertLeftTuple -> TODO");
    }

    @Override
    public void retractLeftTuple(LeftTuple leftTuple, PropagationContext context, InternalWorkingMemory workingMemory) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.retractLeftTuple -> TODO");
    }

    @Override
    public LeftTuple createPeer(LeftTuple original) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.createPeer -> TODO");
    }

    @Override
    public LeftTuple createLeftTuple(InternalFactHandle factHandle, LeftTupleSink sink, boolean leftTupleMemoryEnabled) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.createLeftTuple -> TODO");
    }

    @Override
    public LeftTuple createLeftTuple(InternalFactHandle factHandle, LeftTuple leftTuple, LeftTupleSink sink) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.createLeftTuple -> TODO");
    }

    @Override
    public LeftTuple createLeftTuple(LeftTuple leftTuple, LeftTupleSink sink, PropagationContext pctx, boolean leftTupleMemoryEnabled) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.createLeftTuple -> TODO");
    }

    @Override
    public LeftTuple createLeftTuple(LeftTuple leftTuple, RightTuple rightTuple, LeftTupleSink sink) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.createLeftTuple -> TODO");
    }

    @Override
    public LeftTuple createLeftTuple(LeftTuple leftTuple, RightTuple rightTuple, LeftTuple currentLeftChild, LeftTuple currentRightChild, LeftTupleSink sink, boolean leftTupleMemoryEnabled) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.createLeftTuple -> TODO");
    }

    @Override
    public void updateSink(LeftTupleSink sink, PropagationContext context, InternalWorkingMemory workingMemory) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.updateSink -> TODO");
    }

    @Override
    protected ObjectTypeNode getObjectTypeNode() {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.getObjectTypeNode -> TODO");
    }

    @Override
    public boolean isLeftTupleMemoryEnabled() {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.isLeftTupleMemoryEnabled -> TODO");
    }

    @Override
    public void setLeftTupleMemoryEnabled(boolean tupleMemoryEnabled) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.setLeftTupleMemoryEnabled -> TODO");
    }

    @Override
    public void modifyLeftTuple(InternalFactHandle factHandle, ModifyPreviousTuples modifyPreviousTuples, PropagationContext context, InternalWorkingMemory workingMemory) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.modifyLeftTuple -> TODO");
    }

    @Override
    public void modifyLeftTuple(LeftTuple leftTuple, PropagationContext context, InternalWorkingMemory workingMemory) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.modifyLeftTuple -> TODO");
    }

    @Override
    public LeftTupleSinkNode getNextLeftTupleSinkNode() {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.getNextLeftTupleSinkNode -> TODO");
    }

    @Override
    public void setNextLeftTupleSinkNode(LeftTupleSinkNode next) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.setNextLeftTupleSinkNode -> TODO");
    }

    @Override
    public LeftTupleSinkNode getPreviousLeftTupleSinkNode() {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.getPreviousLeftTupleSinkNode -> TODO");
    }

    @Override
    public void setPreviousLeftTupleSinkNode(LeftTupleSinkNode previous) {
        throw new UnsupportedOperationException("org.drools.retebuilder.nodes.AsyncInvokerNode.setPreviousLeftTupleSinkNode -> TODO");
    }
}
