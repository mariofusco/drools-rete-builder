package org.drools.retebuilder.adapters;

import org.drools.core.WorkingMemory;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.spi.Consequence;
import org.drools.core.spi.KnowledgeHelper;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.functions.FunctionN;
import org.drools.retebuilder.CanonicalBuildContext;

public class RuleImplAdapter extends RuleImpl {
    private final Consequence consequence;

    public RuleImplAdapter(Rule rule, CanonicalBuildContext context) {
        super("");
        this.consequence = new ConsequenceAdapter(rule.getConsequence(), context);
    }

    @Override
    public Consequence getConsequence() {
        return this.consequence;
    }

    public static class ConsequenceAdapter implements Consequence {

        private final org.drools.model.Consequence consequence;
        private final CanonicalBuildContext context;

        public ConsequenceAdapter(org.drools.model.Consequence consequence, CanonicalBuildContext context) {
            this.consequence = consequence;
            this.context = context;
        }

        @Override
        public String getName() {
            return RuleImpl.DEFAULT_CONSEQUENCE_NAME;
        }

        @Override
        public void evaluate(KnowledgeHelper knowledgeHelper, WorkingMemory workingMemory) throws Exception {
            Variable[] consequenceDeclarations = consequence.getDeclarations();
            InternalFactHandle[] factHandles = knowledgeHelper.getTuple().toFactHandles();
            Object[] facts = new Object[consequenceDeclarations.length];
            for (int i = 0; i < facts.length; i++) {
                facts[i] = context.getVariableMapper(consequenceDeclarations[i]).getFact(factHandles);
            }
            consequence.getBlock().execute(facts);

            for (org.drools.model.Consequence.Update update : consequence.getUpdates()) {
                Object updatedFact = context.getVariableMapper(update.getUpdatedVariable()).getFactHandle(factHandles).getObject();
                // TODO the Update specs has the changed fields so use update(FactHandle newObject, long mask, Class<?> modifiedClass) instead
                knowledgeHelper.update(updatedFact);
            }

            for (FunctionN insert : consequence.getInserts()) {
                Object insertedFact = insert.apply(facts);
                knowledgeHelper.insert(insertedFact);
            }

            for (Variable delete : consequence.getDeletes()) {
                InternalFactHandle deletedFactHandle = context.getVariableMapper(delete).getFactHandle(factHandles);
                knowledgeHelper.delete(deletedFactHandle);
            }
        }
    }
}
