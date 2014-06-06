package org.drools.retebuilder;

import org.drools.core.base.ClassObjectType;
import org.drools.core.common.BaseNode;
import org.drools.core.common.BetaConstraints;
import org.drools.core.common.RuleBasePartitionId;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.JoinNode;
import org.drools.core.reteoo.LeftInputAdapterNode;
import org.drools.core.reteoo.LeftTupleSource;
import org.drools.core.reteoo.NodeTypeEnums;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.ReteooBuilder;
import org.drools.core.reteoo.TerminalNode;
import org.drools.core.reteoo.builder.BuildUtils;
import org.drools.core.rule.EntryPointId;
import org.drools.core.rule.GroupElement;
import org.drools.core.spi.BetaNodeFieldConstraint;
import org.drools.core.spi.ObjectType;
import org.drools.model.Condition;
import org.drools.model.Constraint;
import org.drools.model.DataSource;
import org.drools.model.Pattern;
import org.drools.model.Rule;
import org.drools.retebuilder.adapters.RuleImplAdapter;
import org.drools.retebuilder.constraints.ConstraintEvaluator;
import org.drools.retebuilder.constraints.LambdaConstraint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CanonicalReteBuilder {

    private final CanonicalKieBase kieBase;
    private final ReteooBuilder.IdGenerator idGenerator;

    private final Map<DataSource, EntryPointNode> entryPoints = new HashMap<DataSource, EntryPointNode>();

    private final BuildUtils utils = new BuildUtils();

    public CanonicalReteBuilder(CanonicalKieBase kieBase) {
        this.kieBase = kieBase;
        this.idGenerator = new ReteooBuilder.IdGenerator( 1 );
    }

    public ReteooBuilder.IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void addRule(Rule rule) {
        CanonicalBuildContext context = new CanonicalBuildContext(kieBase, idGenerator);
        buildCondition(rule.getView(), context);
        buildConsequence(rule, context);
    }

    private void buildConsequence(Rule rule, CanonicalBuildContext context) {
        TerminalNode terminalNode = kieBase.getNodeFactory().buildTerminalNode( context.getNextId(),
                                                                                context.getTupleSource(),
                                                                                new RuleImplAdapter(rule, context),
                                                                                new GroupElement(),
                                                                                0,
                                                                                context );
        ((BaseNode) terminalNode).attach(context);
    }

    private void buildCondition(Condition condition, CanonicalBuildContext context) {
        if (condition.getType() instanceof Condition.SingleType) {
            buildPattern((Pattern)condition, context);
        } else if (condition.getType() instanceof Condition.AndType) {
            for (Condition subCondition : condition.getSubConditions()) {
                buildCondition(subCondition, context);
            }
        } else if (condition.getType() instanceof Condition.OrType) {

        }
    }

    private void buildPattern(Pattern pattern, CanonicalBuildContext context) {
        context.setObjectSource(getEntryPoint(pattern.getDataSource()));
        context.addBoundVariable(pattern.getVariable());

        Class<?> patternClass = pattern.getVariable().getType().asClass();
        ObjectType objectType = new ClassObjectType(patternClass);
        context.setLastBuiltPattern( new org.drools.core.rule.Pattern(context.getCurrentPatternOffset(), objectType) );

        ObjectTypeNode otn = kieBase.getNodeFactory().buildObjectTypeNode( context.getNextId(),
                                                                           (EntryPointNode) context.getObjectSource(),
                                                                           objectType,
                                                                           context );

        context.setObjectSource( (ObjectSource) utils.attachNode( context, otn ) );

        buildConstraint(pattern, context);
        context.incrementCurrentPatternOffset();

        if (context.getObjectSource() != null && context.getTupleSource() == null) {
            ObjectSource source = context.getObjectSource();
            while ( !(source.getType() ==  NodeTypeEnums.ObjectTypeNode ) ) {
                source = source.getParentObjectSource();
            }
            context.setRootObjectTypeNode( (ObjectTypeNode) source );

            LeftInputAdapterNode lia = kieBase.getNodeFactory().buildLeftInputAdapterNode( context.getNextId(),
                                                                                           context.getObjectSource(),
                                                                                           context );
            context.setTupleSource( (LeftTupleSource) utils.attachNode( context, lia ) );
            context.setObjectSource(null);
        }
    }

    private void buildConstraint(Pattern pattern, CanonicalBuildContext context) {
        if (pattern.getConstraint().getType() == Constraint.Type.SINGLE) {
            ConstraintEvaluator constraintEvaluator = new ConstraintEvaluator(pattern);
            if (context.getTupleSource() == null) {
                buildAlphaConstraint(constraintEvaluator, context);
            } else {
                buildBetaConstraint(constraintEvaluator, context);
            }
        }
    }

    private void buildAlphaConstraint(ConstraintEvaluator constraintEvaluator, CanonicalBuildContext context) {
        LambdaConstraint alphaConstraint = new LambdaConstraint(constraintEvaluator);

        AlphaNode alpha = kieBase.getNodeFactory().buildAlphaNode( context.getNextId(),
                                                                   alphaConstraint,
                                                                   context.getObjectSource(),
                                                                   context);

        context.setObjectSource( (ObjectSource) utils.attachNode( context, alpha ) );
    }

    private void buildBetaConstraint(ConstraintEvaluator constraintEvaluator, CanonicalBuildContext context) {
        List<BetaNodeFieldConstraint> betaConstraintsList = new LinkedList<BetaNodeFieldConstraint>();
        betaConstraintsList.add( new LambdaConstraint(constraintEvaluator) );

        BetaConstraints betaConstraints = utils.createBetaNodeConstraint( context,
                                                                          betaConstraintsList,
                                                                          false );

        JoinNode beta = kieBase.getNodeFactory().buildJoinNode( context.getNextId(),
                                                                context.getTupleSource(),
                                                                context.getObjectSource(),
                                                                betaConstraints,
                                                                context );

        context.setTupleSource( (LeftTupleSource) utils.attachNode( context, beta ) );
        context.setObjectSource( null );
    }

    private EntryPointNode getEntryPoint(DataSource dataSource) {
        EntryPointNode epn = entryPoints.get(dataSource);
        if (epn == null) {
            epn = kieBase.getNodeFactory().buildEntryPointNode( idGenerator.getNextId(),
                                                                RuleBasePartitionId.MAIN_PARTITION,
                                                                kieBase.getConfiguration().isMultithreadEvaluation(),
                                                                kieBase.getRete(),
                                                                EntryPointId.DEFAULT );
            epn.attach();
            entryPoints.put(dataSource, epn);
        }
        return epn;
    }

}
