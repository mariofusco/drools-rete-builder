package org.drools.retebuilder.adapters;

import org.drools.core.WorkingMemory;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.spi.Consequence;
import org.drools.core.spi.KnowledgeHelper;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.retebuilder.ArgumentMapper;
import org.drools.retebuilder.CanonicalBuildContext;

public class RuleImplAdapter extends RuleImpl {
    private final Consequence consequence;

    public RuleImplAdapter(Rule rule, CanonicalBuildContext context) {
        super("");
        ArgumentMapper[] args = findVarPosInPattern(rule.getConsequence().getDeclarations(), context);
        this.consequence = new ConsequenceAdapter(rule.getConsequence(), args);
    }

    private ArgumentMapper[] findVarPosInPattern(Variable[] consequenceDeclarations, CanonicalBuildContext context) {
        ArgumentMapper[] args = new ArgumentMapper[consequenceDeclarations.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = context.getVariableMapper(consequenceDeclarations[i]);
        }
        return args;
    }


    @Override
    public Consequence getConsequence() {
        return this.consequence;
    }

    public static class ConsequenceAdapter implements Consequence {

        private final org.drools.model.Consequence consequence;
        private final ArgumentMapper[] args;

        public ConsequenceAdapter(org.drools.model.Consequence consequence, ArgumentMapper[] args) {
            this.consequence = consequence;
            this.args = args;
        }

        @Override
        public String getName() {
            return RuleImpl.DEFAULT_CONSEQUENCE_NAME;
        }

        @Override
        public void evaluate(KnowledgeHelper knowledgeHelper, WorkingMemory workingMemory) throws Exception {
            InternalFactHandle[] factHandles = knowledgeHelper.getTuple().toFactHandles();
            Object[] facts = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                facts[i] = args[i].getFact(factHandles);
            }
            consequence.getBlock().execute(facts);
        }
    }
}
