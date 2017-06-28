package org.drools.retebuilder;

import java.util.HashMap;
import java.util.Map;

import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.model.AccumulatePattern;
import org.drools.model.Condition;
import org.drools.model.Condition.Type;
import org.drools.model.Pattern;
import org.drools.model.Variable;

public class CanonicalBuildContext extends BuildContext {

    private int patternCounter = 0;
    private final Map<Variable, ArgumentMapper> boundVariables = new HashMap<Variable, ArgumentMapper>();

    public CanonicalBuildContext(InternalKnowledgeBase kBase) {
        super(kBase);
    }

    public void addBoundVariables( Condition.Type type, Pattern pattern ) {
        if (patternCounter == 0 && requiresInitialfact(type, pattern)) {
            patternCounter++;
        }
        Variable[] vars = pattern.getBoundVariables();
        for (int i = 0; i < vars.length; i++) {
            boundVariables.put(vars[i], new ArgumentExtractor(patternCounter, i));
        }
        if (vars.length > 0) {
            patternCounter++;
        }
    }

    public ArgumentMapper getVariableMapper(Variable variable) {
        return boundVariables.get(variable);
    }

    private boolean requiresInitialfact(Condition.Type type, Pattern pattern) {
        return pattern instanceof AccumulatePattern || type == Type.EXISTS || type == Type.NOT;
    }

    private static class ArgumentExtractor implements ArgumentMapper {
        private final int factHandlePos;
        private final int declarationPos;

        private ArgumentExtractor(int factHandlePos, int declarationPos) {
            this.factHandlePos = factHandlePos;
            this.declarationPos = declarationPos;
        }

        public Object getFact(Object[] objs) {
            Object fact = objs[factHandlePos];
            return fact instanceof Object[] ? ((Object[])fact)[declarationPos] : fact;
        }
    }
}