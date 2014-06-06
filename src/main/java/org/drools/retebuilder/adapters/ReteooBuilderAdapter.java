package org.drools.retebuilder.adapters;

import org.drools.core.reteoo.ReteooBuilder;
import org.drools.retebuilder.CanonicalReteBuilder;

public class ReteooBuilderAdapter extends ReteooBuilder {
    private final CanonicalReteBuilder reteBuilder;

    public ReteooBuilderAdapter(CanonicalReteBuilder reteBuilder) {
        this.reteBuilder = reteBuilder;
    }

    @Override
    public IdGenerator getIdGenerator() {
        return reteBuilder.getIdGenerator();
    }
}
