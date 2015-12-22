package org.drools.retebuilder.adapters;

import org.drools.core.WorkingMemory;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.spi.Consequence;
import org.drools.core.spi.KnowledgeHelper;
import org.drools.model.Drools;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.functions.FunctionN;
import org.drools.retebuilder.CanonicalBuildContext;

public class RuleImplAdapter extends RuleImpl {
    private final Consequence consequence;

    public RuleImplAdapter(Rule rule, CanonicalBuildContext context) {
        super(rule.getName());
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
            Object[] objs = knowledgeHelper.getTuple().toObjects();

            Object[] facts;
            int i = 0;
            if (consequence.isUsingDrools()) {
                facts = new Object[consequenceDeclarations.length+1];
                facts[0] = new DroolsImpl(knowledgeHelper);
                i++;
            } else {
                facts = new Object[consequenceDeclarations.length];
            }

            for (int j = 0; j < consequenceDeclarations.length; i++, j++) {
                facts[i] = context.getVariableMapper(consequenceDeclarations[j]).getFact(objs);
            }

            consequence.getBlock().execute(facts);

            for (org.drools.model.Consequence.Update update : consequence.getUpdates()) {
                Object updatedFact = context.getVariableMapper(update.getUpdatedVariable()).getFact(objs);
                // TODO the Update specs has the changed fields so use update(FactHandle newObject, long mask, Class<?> modifiedClass) instead
                knowledgeHelper.update(updatedFact);
            }

            for (FunctionN insert : consequence.getInserts()) {
                Object insertedFact = insert.apply(facts);
                knowledgeHelper.insert(insertedFact);
            }

            for (Variable delete : consequence.getDeletes()) {
                Object deletedFact = context.getVariableMapper(delete).getFact(objs);
                knowledgeHelper.delete(deletedFact);
            }
        }
    }

    public static class DroolsImpl implements Drools {
        private final KnowledgeHelper knowledgeHelper;

        public DroolsImpl(KnowledgeHelper knowledgeHelper) {
            this.knowledgeHelper = knowledgeHelper;
        }

        @Override
        public void insert(Object object) {
            knowledgeHelper.insert(object);
        }

        @Override
        public void update(Object object) {
            knowledgeHelper.update(object);
        }

        @Override
        public void delete(Object object) {
            knowledgeHelper.delete(object);
        }
    }
}
