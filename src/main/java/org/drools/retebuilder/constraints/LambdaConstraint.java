package org.drools.retebuilder.constraints;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.ContextEntry;
import org.drools.core.rule.Declaration;
import org.drools.core.rule.IndexableConstraint;
import org.drools.core.rule.MutableTypeConstraint;
import org.drools.core.rule.constraint.MvelConstraint;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;
import org.drools.core.spi.Tuple;
import org.drools.core.util.AbstractHashTable.FieldIndex;
import org.drools.core.util.index.IndexUtil;

public class LambdaConstraint extends MutableTypeConstraint implements IndexableConstraint {

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

    @Override
    public boolean isUnification() {
        throw new UnsupportedOperationException( "org.drools.retebuilder.constraints.LambdaConstraint.isUnification -> TODO" );

    }

    @Override
    public boolean isIndexable( short nodeType ) {
        return false;
//        throw new UnsupportedOperationException( "org.drools.retebuilder.constraints.LambdaConstraint.isIndexable -> TODO" );
    }

    @Override
    public IndexUtil.ConstraintType getConstraintType() {
        return IndexUtil.ConstraintType.UNKNOWN;
//        throw new UnsupportedOperationException( "org.drools.retebuilder.constraints.LambdaConstraint.getConstraintType -> TODO" );
    }

    @Override
    public FieldValue getField() {
        throw new UnsupportedOperationException( "org.drools.retebuilder.constraints.LambdaConstraint.getField -> TODO" );

    }

    @Override
    public FieldIndex getFieldIndex() {
        throw new UnsupportedOperationException( "org.drools.retebuilder.constraints.LambdaConstraint.getFieldIndex -> TODO" );

    }

    @Override
    public InternalReadAccessor getFieldExtractor() {
        throw new UnsupportedOperationException( "org.drools.retebuilder.constraints.LambdaConstraint.getFieldExtractor -> TODO" );

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
