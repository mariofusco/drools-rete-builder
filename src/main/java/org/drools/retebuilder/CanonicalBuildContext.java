package org.drools.retebuilder;

import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.reteoo.ReteooBuilder;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.model.Variable;

import java.util.ArrayList;
import java.util.List;

public class CanonicalBuildContext extends BuildContext {

    private final List<Variable> boundVariables = new ArrayList<Variable>();

    public CanonicalBuildContext(InternalKnowledgeBase kBase, ReteooBuilder.IdGenerator idGenerator) {
        super(kBase, idGenerator);
    }

    public void addBoundVariable(Variable variable) {
        boundVariables.add(variable);
    }

    public List<Variable> getBoundVariables() {
        return boundVariables;
    }
}