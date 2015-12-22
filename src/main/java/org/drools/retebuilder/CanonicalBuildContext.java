package org.drools.retebuilder;

import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.reteoo.ReteooBuilder;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.model.AccumulatePattern;
import org.drools.model.ExistentialPattern;
import org.drools.model.Pattern;
import org.drools.model.Variable;

import java.util.HashMap;
import java.util.Map;

public class CanonicalBuildContext extends BuildContext {

    private int patternCounter = 0;
    private final Map<Variable, ArgumentMapper> boundVariables = new HashMap<Variable, ArgumentMapper>();

    public CanonicalBuildContext(InternalKnowledgeBase kBase, ReteooBuilder.IdGenerator idGenerator) {
        super(kBase, idGenerator);
    }

    public void addBoundVariables(Pattern pattern) {
        if (patternCounter == 0 && requiresInitialfact(pattern)) {
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

    private boolean requiresInitialfact(Pattern pattern) {
        return pattern instanceof AccumulatePattern || pattern instanceof ExistentialPattern;
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