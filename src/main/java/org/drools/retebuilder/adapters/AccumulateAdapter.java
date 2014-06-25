package org.drools.retebuilder.adapters;

import org.drools.core.WorkingMemory;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.rule.Accumulate;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Accumulator;
import org.drools.core.spi.Tuple;

public class AccumulateAdapter extends Accumulate {

    public AccumulateAdapter(Accumulator[] accumulators) {

        super(null, new Declaration[0], accumulators, true);
    }

    @Override
    public void accumulate(final Object[] workingMemoryContext,
                           final Object[] context,
                           final Tuple leftTuple,
                           final InternalFactHandle handle,
                           final WorkingMemory workingMemory) {
        try {
            for ( int i = 0; i < getAccumulators().length; i++ ) {
                getAccumulators()[i].accumulate( workingMemoryContext[i],
                                                 context[i],
                                                 leftTuple,
                                                 handle,
                                                 null, // this.requiredDeclarations
                                                 null, // getInnerDeclarationCache(),
                                                 workingMemory );
            }
        } catch ( final Exception e ) {
            throw new RuntimeException( e );
        }
    }
}
