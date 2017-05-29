package org.drools.retebuilder.constraints;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.spi.Tuple;
import org.drools.model.Index;
import org.drools.model.Pattern;
import org.drools.model.SingleConstraint;
import org.drools.model.functions.PredicateN;

import static org.drools.retebuilder.constraints.EvaluationUtil.findArgsPos;
import static org.drools.retebuilder.constraints.EvaluationUtil.getInvocationArgs;

public class ConstraintEvaluator {

    private final String id;
    private final PredicateN predicate;
    private final Index index;
    private final String[] reactiveProps;
    private final int[] argsPos;

    public ConstraintEvaluator(Pattern pattern, SingleConstraint constraint) {
        this.id = constraint.getExprId();
        this.predicate = constraint.getPredicate();
        this.index = constraint.getIndex();
        this.reactiveProps = constraint.getReactiveProps();
        this.argsPos = findArgsPos(pattern, constraint.getVariables());
    }

    public boolean evaluate(InternalFactHandle handle) {
        return predicate.test(handle.getObject());
    }

    public boolean evaluate(InternalFactHandle handle, Tuple tuple) {
        return predicate.test(getInvocationArgs(argsPos, handle, tuple));
    }

    public Index getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        return id.equals(((ConstraintEvaluator) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
