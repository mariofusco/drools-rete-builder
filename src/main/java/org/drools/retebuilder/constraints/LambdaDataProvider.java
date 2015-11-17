package org.drools.retebuilder.constraints;

import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.DataProvider;
import org.drools.core.spi.PropagationContext;
import org.drools.model.InvokerMultiValuePattern;
import org.drools.model.InvokerPattern;
import org.drools.model.InvokerSingleValuePattern;

import java.util.Arrays;
import java.util.Iterator;

import static org.drools.retebuilder.constraints.EvaluationUtil.findArgsPos;
import static org.drools.retebuilder.constraints.EvaluationUtil.getInvocationArgs;

public class LambdaDataProvider<T> implements DataProvider {

    private final InvokerPattern<T> pattern;
    private final int[] argsPos;

    public LambdaDataProvider(InvokerPattern<T> pattern) {
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
    public Iterator getResults( LeftTuple tuple, InternalWorkingMemory wm, PropagationContext ctx, Object providerContext ) {
        if (pattern.isMultiValue()) {
            return ((InvokerMultiValuePattern<T>)pattern).getInvokedFunction().apply( getInvocationArgs(argsPos, null, tuple) ).iterator();
        } else {
            Object result = ((InvokerSingleValuePattern<T>)pattern).getInvokedFunction().apply( getInvocationArgs(argsPos, null, tuple) );
            return Arrays.asList(result).iterator();
        }
    }

    @Override
    public DataProvider clone() {
        return this;
    }

    @Override
    public void replaceDeclaration(Declaration declaration, Declaration resolved) { }
}
