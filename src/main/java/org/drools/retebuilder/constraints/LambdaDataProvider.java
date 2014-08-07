package org.drools.retebuilder.constraints;

import org.drools.core.WorkingMemory;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.DataProvider;
import org.drools.core.spi.PropagationContext;
import org.drools.core.spi.Tuple;
import org.drools.model.InvokerPattern;

import java.util.Arrays;
import java.util.Iterator;

import static org.drools.retebuilder.constraints.EvaluationUtil.findArgsPos;
import static org.drools.retebuilder.constraints.EvaluationUtil.getInvocationArgs;

public class LambdaDataProvider implements DataProvider {

    private final InvokerPattern pattern;
    private final int[] argsPos;

    public LambdaDataProvider(InvokerPattern pattern) {
        this.pattern = pattern;
        this.argsPos = findArgsPos(pattern, pattern.getInputVariables());
    }

    @Override
    public Declaration[] getRequiredDeclarations() {
        return null;
    }

    @Override
    public Object createContext() {
        return null;
    }

    @Override
    public Iterator getResults(Tuple tuple, WorkingMemory wm, PropagationContext ctx, Object providerContext) {
        Object result = pattern.getInvokedFunction().apply( getInvocationArgs(argsPos, null, tuple) );
        return Arrays.asList(result).iterator();
    }

    @Override
    public DataProvider clone() {
        return this;
    }

    @Override
    public void replaceDeclaration(Declaration declaration, Declaration resolved) { }
}
