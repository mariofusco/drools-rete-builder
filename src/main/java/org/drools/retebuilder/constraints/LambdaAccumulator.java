package org.drools.retebuilder.constraints;

import org.drools.core.WorkingMemory;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Accumulator;
import org.drools.core.spi.Tuple;
import org.drools.model.AccumulateFunction;

import java.io.Serializable;

public class LambdaAccumulator implements Accumulator {

    private final AccumulateFunction accumulateFunction;

    public LambdaAccumulator(AccumulateFunction accumulateFunction) {
        this.accumulateFunction = accumulateFunction;
    }

    @Override
    public Object createWorkingMemoryContext() {
        // no working memory context needed
        return null;
    }

    @Override
    public Serializable createContext() {
        throw new UnsupportedOperationException("org.drools.retebuilder.constraints.LambdaAccumulator.createContext -> TODO");

    }

    @Override
    public void init(Object workingMemoryContext, Object context, Tuple leftTuple, Declaration[] declarations, WorkingMemory workingMemory) throws Exception {
        throw new UnsupportedOperationException("org.drools.retebuilder.constraints.LambdaAccumulator.init -> TODO");

    }

    @Override
    public void accumulate(Object workingMemoryContext, Object context, Tuple leftTuple, InternalFactHandle handle, Declaration[] declarations, Declaration[] innerDeclarations, WorkingMemory workingMemory) throws Exception {
        throw new UnsupportedOperationException("org.drools.retebuilder.constraints.LambdaAccumulator.accumulate -> TODO");

    }

    @Override
    public boolean supportsReverse() {
        throw new UnsupportedOperationException("org.drools.retebuilder.constraints.LambdaAccumulator.supportsReverse -> TODO");

    }

    @Override
    public void reverse(Object workingMemoryContext, Object context, Tuple leftTuple, InternalFactHandle handle, Declaration[] declarations, Declaration[] innerDeclarations, WorkingMemory workingMemory) throws Exception {
        throw new UnsupportedOperationException("org.drools.retebuilder.constraints.LambdaAccumulator.reverse -> TODO");

    }

    @Override
    public Object getResult(Object workingMemoryContext, Object context, Tuple leftTuple, Declaration[] declarations, WorkingMemory workingMemory) throws Exception {
        throw new UnsupportedOperationException("org.drools.retebuilder.constraints.LambdaAccumulator.getResult -> TODO");

    }
}
