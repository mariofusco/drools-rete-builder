package org.drools.retebuilder.constraints;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.reteoo.LeftTuple;
import org.drools.model.Pattern;
import org.drools.model.SingleConstraint;
import org.drools.model.Variable;
import org.drools.model.functions.MultiValuePredicate;

public class ConstraintEvaluator {

    private final MultiValuePredicate predicate;
    private final int[] argsPos;

    public ConstraintEvaluator(Pattern pattern) {
        SingleConstraint constraint = (SingleConstraint) pattern.getConstraint();
        this.predicate = constraint.getPredicate();
        this.argsPos = findArgsPos(pattern, constraint);
    }

    private int[] findArgsPos(Pattern pattern, SingleConstraint constraint) {
        int[] pos = new int[constraint.getVariables().length];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = findVarPosInPattern(pattern, constraint.getVariables()[i]);
        }
        return pos;
    }

    private int findVarPosInPattern(Pattern pattern, Variable var) {
        if (var.equals(pattern.getPatternVariable())) {
            return -1;
        }
        for (int i = 0; i < pattern.getInputVariables().length; i++) {
            if (var.equals(pattern.getInputVariables()[i])) {
                // TODO FIXME
                // how could I determine the position of this variable inside the left tuple?
                return 0;
            }
        }
        throw new RuntimeException("Unknown Variable: " + var);
    }

    public boolean evaluate(InternalFactHandle handle, LeftTuple leftTuple) {
        Object[] params = new Object[argsPos.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = argsPos[i] >= 0 ? leftTuple.get(argsPos[i]).getObject() : handle.getObject();
        }
        return predicate.test(params);
    }
}
