package org.drools.retebuilder.constraints;

import org.drools.core.base.field.ObjectFieldImpl;
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
import org.drools.model.AlphaIndex;
import org.drools.model.Index;

public class LambdaConstraint extends MutableTypeConstraint implements IndexableConstraint {

    private final ConstraintEvaluator evaluator;
    private final Declaration[] requiredDeclarations;

    private FieldValue field;
    private InternalReadAccessor readAccessor;

    public LambdaConstraint(ConstraintEvaluator evaluator) {
        this(evaluator, new Declaration[0]);
    }

    public LambdaConstraint(ConstraintEvaluator evaluator, Declaration[] requiredDeclarations) {
        this.evaluator = evaluator;
        this.requiredDeclarations = requiredDeclarations;
        initIndexes();
    }

    private void initIndexes() {
        Index index = evaluator.getIndex();
        if (index instanceof AlphaIndex ) {
            field = new ObjectFieldImpl( ( (AlphaIndex) index ).getRightValue() );
            readAccessor = new LambdaReadAccessor( ( (AlphaIndex) index ).getLeftOperandExtractor() );
        }
    }

    @Override
    public Declaration[] getRequiredDeclarations() {
        return requiredDeclarations;
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
        return getConstraintType().isIndexableForNode(nodeType);
    }

    @Override
    public IndexUtil.ConstraintType getConstraintType() {
        Index index = evaluator.getIndex();
        if (index != null) {
            switch (index.getConstraintType()) {
                case EQUAL:
                    return IndexUtil.ConstraintType.EQUAL;
                case NOT_EQUAL:
                    return IndexUtil.ConstraintType.NOT_EQUAL;
                case GREATER_THAN:
                    return IndexUtil.ConstraintType.GREATER_THAN;
                case GREATER_OR_EQUAL:
                    return IndexUtil.ConstraintType.GREATER_OR_EQUAL;
                case LESS_THAN:
                    return IndexUtil.ConstraintType.LESS_THAN;
                case LESS_OR_EQUAL:
                    return IndexUtil.ConstraintType.LESS_OR_EQUAL;
                case RANGE:
                    return IndexUtil.ConstraintType.RANGE;
            }
        }
        return IndexUtil.ConstraintType.UNKNOWN;
    }

    @Override
    public FieldValue getField() {
        return field;
    }

    @Override
    public FieldIndex getFieldIndex() {
        throw new UnsupportedOperationException( "org.drools.retebuilder.constraints.LambdaConstraint.getFieldIndex -> TODO" );

    }

    @Override
    public InternalReadAccessor getFieldExtractor() {
        return readAccessor;
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
