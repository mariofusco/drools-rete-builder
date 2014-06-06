package org.drools.retebuilder.adapters;

import org.drools.core.WorkingMemory;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.spi.Consequence;
import org.drools.core.spi.KnowledgeHelper;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.retebuilder.CanonicalBuildContext;

import java.util.List;

public class RuleImplAdapter extends RuleImpl {
    private final Consequence consequence;

    public RuleImplAdapter(Rule rule, CanonicalBuildContext context) {
        super("");
        int[] argsPos = findVarPosInPattern(rule.getConsequence().getDeclarations(), context.getBoundVariables());
        this.consequence = new ConsequenceAdapter(rule.getConsequence(), argsPos);
    }

    private int[] findVarPosInPattern(Variable[] consequenceDeclarations, List<Variable> boundVariables) {
        int[] pos = new int[consequenceDeclarations.length];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = boundVariables.indexOf(consequenceDeclarations[i]);
        }
        return pos;
    }


    @Override
    public Consequence getConsequence() {
        return this.consequence;
    }

    public static class ConsequenceAdapter implements Consequence {

        private final org.drools.model.Consequence consequence;
        private final int[] argsPos;

        public ConsequenceAdapter(org.drools.model.Consequence consequence, int[] argsPos) {
            this.consequence = consequence;
            this.argsPos = argsPos;
        }

        @Override
        public String getName() {
            return RuleImpl.DEFAULT_CONSEQUENCE_NAME;
        }

        @Override
        public void evaluate(KnowledgeHelper knowledgeHelper, WorkingMemory workingMemory) throws Exception {
            InternalFactHandle[] factHandles = knowledgeHelper.getTuple().toFactHandles();
            Object[] facts = new Object[argsPos.length];
            for (int i = 0; i < argsPos.length; i++) {
                facts[i] = factHandles[argsPos[i]].getObject();
            }
            consequence.getBlock().execute(facts);
        }
    }
}
