package org.drools.retebuilder.constraints;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.ContextEntry;
import org.drools.core.rule.Declaration;
import org.drools.core.rule.MutableTypeConstraint;
import org.drools.core.rule.constraint.MvelConstraint;
import org.drools.core.spi.Tuple;

public class LambdaConstraint extends MutableTypeConstraint {

    private final ConstraintEvaluator evaluator;

    public LambdaConstraint(ConstraintEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public Declaration[] getRequiredDeclarations() {
        throw new UnsupportedOperationException("org.drools.retebuilder.constraints.LambdaConstraint.getRequiredDeclarations -> TODO");
    }

    @Override
    public void replaceDeclaration(Declaration oldDecl, Declaration newDecl) {
        throw new UnsupportedOperationException("org.drools.retebuilder.constraints.LambdaConstraint.replaceDeclaration -> TODO");
    }

    @Override
    public LambdaConstraint clone() {
        return this;
    }

    @Override
    public boolean isTemporal() {
        throw new UnsupportedOperationException("org.drools.retebuilder.constraints.LambdaConstraint.isTemporal -> TODO");
    }

    @Override
    public boolean isAllowed(InternalFactHandle handle, InternalWorkingMemory workingMemory) {
        return evaluator.evaluate(handle);
    }

    @Override
    public boolean isAllowedCachedLeft(ContextEntry context, InternalFactHandle handle) {
        return evaluator.evaluate(handle, ((LambdaContextEntry) context).getTuple());
    }

    @Override
    public boolean isAllowedCachedRight(Tuple tuple, ContextEntry context) {
        throw new UnsupportedOperationException("org.drools.retebuilder.constraints.LambdaConstraint.isAllowedCachedRight -> TODO");
    }

    @Override
    public ContextEntry createContextEntry() {
        return new LambdaContextEntry();
    }

    public static class LambdaContextEntry extends MvelConstraint.MvelContextEntry {
        Tuple getTuple() {
            return tuple;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        return evaluator.equals(((LambdaConstraint) other).evaluator);
    }

    @Override
    public int hashCode() {
        return evaluator.hashCode();
    }
}
