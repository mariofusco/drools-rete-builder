package org.drools.retebuilder.constraints;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.spi.Tuple;
import org.drools.model.Pattern;
import org.drools.model.Variable;

public final class EvaluationUtil {

    private EvaluationUtil() { }

    public static int[] findArgsPos(Pattern pattern, Variable[] variables) {
        int[] pos = new int[variables.length];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = findVarPosInPattern(pattern, variables[i]);
        }
        return pos;
    }

    public static int findVarPosInPattern(Pattern pattern, Variable var) {
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

    public static Object[] getInvocationArgs(int[] argsPos, InternalFactHandle handle, Tuple tuple) {
        Object[] params = new Object[argsPos.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = argsPos[i] >= 0 ? tuple.get(argsPos[i]).getObject() : handle.getObject();
        }
        return params;
    }
}
