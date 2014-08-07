package org.drools.retebuilder;

import org.drools.core.base.ClassObjectType;
import org.drools.core.common.BaseNode;
import org.drools.core.common.BetaConstraints;
import org.drools.core.common.RuleBasePartitionId;
import org.drools.core.reteoo.AccumulateNode;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.BetaNode;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.InitialFactImpl;
import org.drools.core.reteoo.LeftInputAdapterNode;
import org.drools.core.reteoo.LeftTupleSource;
import org.drools.core.reteoo.NodeTypeEnums;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.ReteooBuilder;
import org.drools.core.reteoo.TerminalNode;
import org.drools.core.reteoo.builder.BuildUtils;
import org.drools.core.rule.Accumulate;
import org.drools.core.rule.EntryPointId;
import org.drools.core.rule.From;
import org.drools.core.rule.GroupElement;
import org.drools.core.spi.Accumulator;
import org.drools.core.spi.AlphaNodeFieldConstraint;
import org.drools.core.spi.BetaNodeFieldConstraint;
import org.drools.core.spi.DataProvider;
import org.drools.core.spi.ObjectType;
import org.drools.model.AccumulatePattern;
import org.drools.model.Condition;
import org.drools.model.Constraint;
import org.drools.model.DataSource;
import org.drools.model.ExistentialPattern;
import org.drools.model.InvokerPattern;
import org.drools.model.Pattern;
import org.drools.model.Rule;
import org.drools.model.SingleConstraint;
import org.drools.model.Variable;
import org.drools.retebuilder.adapters.AccumulateAdapter;
import org.drools.retebuilder.adapters.FromAdapter;
import org.drools.retebuilder.adapters.RuleImplAdapter;
import org.drools.retebuilder.constraints.ConstraintEvaluator;
import org.drools.retebuilder.constraints.LambdaAccumulator;
import org.drools.retebuilder.constraints.LambdaConstraint;
import org.drools.retebuilder.constraints.LambdaDataProvider;
import org.drools.retebuilder.nodes.SyncInvokerNode;

import java.util.ArrayList;
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
            buildPattern((Pattern) condition, context);
        } else if (condition.getType() instanceof Condition.AndType) {
            for (Condition subCondition : condition.getSubConditions()) {
                buildCondition(subCondition, context);
            }
        } else if (condition.getType() instanceof Condition.OrType) {
            // TODO
        }
    }

    private void buildPattern(Pattern pattern, CanonicalBuildContext context) {
        initConstraint(pattern, context);

        context.setObjectSource(getEntryPoint(context, pattern.getDataSource()));
        context.addBoundVariables(pattern);

        Class<?> patternClass = pattern.getPatternVariable().getType().asClass();
        createObjectTypeNode(context, patternClass);

        buildConstraints(pattern, context);

        context.incrementCurrentPatternOffset();

        createLeftInputAdapterNode(context);
    }

    private void initConstraint(Pattern pattern, CanonicalBuildContext context) {
        if (context.getTupleSource() == null &&
            (pattern instanceof AccumulatePattern || pattern instanceof ExistentialPattern)) {
            createObjectTypeNode(context, InitialFactImpl.class);
            createLeftInputAdapterNode(context);
        }
    }

    private void buildConstraints(Pattern pattern, CanonicalBuildContext context) {
        if (pattern instanceof InvokerPattern) {
            buildInvoker((InvokerPattern)pattern, context);
            return;
        }
        buildConstraint(pattern, pattern.getConstraint(), context);
        if (pattern instanceof AccumulatePattern) {
            buildAccumulate((AccumulatePattern) pattern, context);
        }
        if (context.getObjectSource() != null && context.getTupleSource() != null) {
            buildBetaConstraint(pattern, null, context);
        }
    }

    private void buildInvoker(InvokerPattern pattern, CanonicalBuildContext context) {
        DataProvider dataProvider = new LambdaDataProvider(pattern);
        From from = new FromAdapter(dataProvider, pattern);
        SyncInvokerNode node = new SyncInvokerNode(context, dataProvider, from);
        attachBetaNode(context, node);
    }

    private void buildConstraint(Pattern pattern, Constraint constraint, CanonicalBuildContext context) {
        if (constraint.getType() == Constraint.Type.SINGLE) {
            SingleConstraint singleConstraint = (SingleConstraint) constraint;
            ConstraintEvaluator constraintEvaluator = new ConstraintEvaluator(pattern, singleConstraint);
            if (singleConstraint.getVariables().length > 0) {
                if (isAlphaConstraint(pattern, singleConstraint)) {
                    buildAlphaConstraint(pattern, constraintEvaluator, context);
                } else {
                    buildBetaConstraint(pattern, constraintEvaluator, context);
                }
            }
        } else if (pattern.getConstraint().getType() == Constraint.Type.AND) {
            for (Constraint child : constraint.getChildren()) {
                buildConstraint(pattern, child, context);
            }
        }
    }

    private boolean isAlphaConstraint(Pattern pattern, SingleConstraint singleConstraint) {
        for (Variable variable : singleConstraint.getVariables()) {
            if (pattern.getPatternVariable() != variable) {
                return false;
            }
        }
        return true;
    }

    private void buildAccumulate(AccumulatePattern pattern, CanonicalBuildContext context) {
        List<BetaNodeFieldConstraint> accumulateConstraints = new ArrayList<BetaNodeFieldConstraint>();
        context.setBetaconstraints(accumulateConstraints);

        final BetaConstraints resultsBinder = utils.createBetaNodeConstraint( context,
                                                                              context.getBetaconstraints(),
                                                                              true );
        final BetaConstraints sourceBinder = utils.createBetaNodeConstraint( context,
                                                                             context.getBetaconstraints(),
                                                                             false );

        Accumulator[] accumulators = new Accumulator[pattern.getFunctions().length];
        for (int i = 0; i < pattern.getFunctions().length; i++) {
            accumulators[i] = new LambdaAccumulator(pattern.getFunctions()[i]);
        }

        Accumulate accumulate = new AccumulateAdapter(accumulators);

        AccumulateNode accNode = kieBase.getNodeFactory().buildAccumulateNode(context.getNextId(),
                                                                              context.getTupleSource(),
                                                                              context.getObjectSource(),
                                                                              new AlphaNodeFieldConstraint[0],
                                                                              sourceBinder,
                                                                              resultsBinder,
                                                                              accumulate,
                                                                              false, // existSubNetwort
                                                                              context);

        attachBetaNode(context, accNode);
    }

    private void buildAlphaConstraint(Pattern pattern, ConstraintEvaluator constraintEvaluator, CanonicalBuildContext context) {
        LambdaConstraint alphaConstraint = new LambdaConstraint(constraintEvaluator);

        AlphaNode alpha = kieBase.getNodeFactory().buildAlphaNode( context.getNextId(),
                                                                   alphaConstraint,
                                                                   context.getObjectSource(),
                                                                   context);

        context.setObjectSource( (ObjectSource) utils.attachNode( context, alpha ) );
    }

    private void buildBetaConstraint(Pattern pattern, ConstraintEvaluator constraintEvaluator, CanonicalBuildContext context) {
        BetaConstraints betaConstraints = buildBetaConstraints(constraintEvaluator, context);

        BetaNode beta = null;
        if (pattern instanceof ExistentialPattern) {
            switch (((ExistentialPattern)pattern).getExistentialType()) {
                case EXISTS:
                    beta = kieBase.getNodeFactory().buildExistsNode(context.getNextId(),
                                                                    context.getTupleSource(),
                                                                    context.getObjectSource(),
                                                                    betaConstraints,
                                                                    context);
                    break;
                case NOT:
                    beta = kieBase.getNodeFactory().buildNotNode(context.getNextId(),
                                                                 context.getTupleSource(),
                                                                 context.getObjectSource(),
                                                                 betaConstraints,
                                                                 context);
                    break;
            }
        } else {
            beta = kieBase.getNodeFactory().buildJoinNode( context.getNextId(),
                                                           context.getTupleSource(),
                                                           context.getObjectSource(),
                                                           betaConstraints,
                                                           context );
        }

        attachBetaNode(context, beta);
    }

    private void attachBetaNode(CanonicalBuildContext context, BaseNode beta) {
        context.setTupleSource( (LeftTupleSource) utils.attachNode( context, beta ) );
        context.setObjectSource( null );
    }

    private BetaConstraints buildBetaConstraints(ConstraintEvaluator constraintEvaluator, CanonicalBuildContext context) {
        List<BetaNodeFieldConstraint> betaConstraintsList = new LinkedList<BetaNodeFieldConstraint>();
        if (constraintEvaluator != null) {
            betaConstraintsList.add( new LambdaConstraint(constraintEvaluator) );
        }

        return utils.createBetaNodeConstraint( context,
                                               betaConstraintsList,
                                               false );
    }

    private EntryPointNode getEntryPoint(CanonicalBuildContext context, DataSource dataSource) {
        if (true) { // TODO now it always returns default entry point
            return context.getKnowledgeBase().getRete().getEntryPointNode( EntryPointId.DEFAULT );
        }
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

    private void createObjectTypeNode(CanonicalBuildContext context, Class<?> patternClass) {
        if (context.getObjectSource() == null) {
            EntryPointNode defaultEntryPoint = context.getKnowledgeBase().getRete().getEntryPointNode( EntryPointId.DEFAULT );
            context.setObjectSource(defaultEntryPoint);
        }

        ObjectType objectType = new ClassObjectType(patternClass);
        context.setLastBuiltPattern( new org.drools.core.rule.Pattern(context.getCurrentPatternOffset(), objectType) );

        ObjectTypeNode otn = kieBase.getNodeFactory().buildObjectTypeNode( context.getNextId(),
                                                                           (EntryPointNode) context.getObjectSource(),
                                                                           objectType,
                                                                           context );

        context.setObjectSource( (ObjectSource) utils.attachNode( context, otn ) );
    }

    private void createLeftInputAdapterNode(CanonicalBuildContext context) {
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
}
