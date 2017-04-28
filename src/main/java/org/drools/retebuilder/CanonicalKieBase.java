package org.drools.retebuilder;

import java.util.UUID;

import org.drools.core.RuleBaseConfiguration;
import org.drools.core.base.ClassObjectType;
import org.drools.core.common.RuleBasePartitionId;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.KieComponentFactory;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.Rete;
import org.drools.core.reteoo.ReteooBuilder;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.reteoo.builder.NodeFactory;
import org.drools.core.rule.EntryPointId;
import org.drools.model.Rule;
import org.drools.retebuilder.adapters.ReteooBuilderAdapter;
import org.drools.retebuilder.nodes.DataStreamNode;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.RuleUnit;
import org.kie.internal.KnowledgeBaseFactory;

public class CanonicalKieBase extends KnowledgeBaseImpl {

    private KieComponentFactory kieComponentFactory;
    private transient Rete rete;

    private CanonicalReteBuilder reteBuilder;

    private ReteooBuilder reteooBuilder;

    public CanonicalKieBase() {
        this(UUID.randomUUID().toString(),
             (RuleBaseConfiguration) KnowledgeBaseFactory.newKnowledgeBaseConfiguration());
    }

    public CanonicalKieBase(String id,
                            RuleBaseConfiguration config) {
        super(id, config);
    }

    @Override
    protected void setupRete() {
        this.kieComponentFactory = getConfiguration().getComponentFactory();
        this.rete = new Rete( this );
        this.reteBuilder = new CanonicalReteBuilder( this );
        this.reteooBuilder = new ReteooBuilderAdapter( reteBuilder );

        // always add the default entry point
        EntryPointNode epn = getNodeFactory().buildEntryPointNode( this.reteBuilder.getIdGenerator().getNextId(),
                                                                   RuleBasePartitionId.MAIN_PARTITION,
                                                                   this.getConfiguration().isMultithreadEvaluation(),
                                                                   this.rete,
                                                                   EntryPointId.DEFAULT );
        epn.attach();
        this.reteBuilder.registerEntryPoint(EntryPointId.DEFAULT, epn);

        BuildContext context = new BuildContext( this );
        context.setCurrentEntryPoint( epn.getEntryPoint() );
        context.setTupleMemoryEnabled( true );
        context.setObjectTypeNodeMemoryEnabled( true );

        ObjectTypeNode otn = getNodeFactory().buildObjectTypeNode(this.reteooBuilder.getIdGenerator().getNextId(),
                                                                  epn,
                                                                  ClassObjectType.InitialFact_ObjectType,
                                                                  context );
        otn.attach(context);


    }

    @Override
    public Rete getRete() {
        return this.rete;
    }

    @Override
    public int getNodeCount() {
        return this.reteBuilder.getIdGenerator().getLastId() + 1;
    }

    @Override
    public int getMemoryCount(String topic) {
        // may start in 0
        return this.reteBuilder.getIdGenerator().getLastId(topic) + 1;
    }

    @Override
    public KieSession newKieSession() {
        KieSession kSession = super.newKieSession();
        return kSession;
    }

    DataStreamNode getDataStreamNode(String dataSourceName) {
        return reteBuilder.getDataStreamNode(dataSourceName);
    }

    public ReteooBuilder getReteooBuilder() {
        return this.reteooBuilder;
    }

    public void addRules(Rule... rules) {
        for (Rule rule : rules) {
            reteBuilder.addRule(rule);
            registerRuleUnit( rule );
        }
    }

    private void registerRuleUnit(Rule rule) {
        if (rule.getUnit() != null) {
            String unitName = rule.getPackge() + "." + rule.getUnit();
            getRuleUnitRegistry().registerRuleUnit( unitName, () -> {
                try {
                    String unitClassName = rule.getPackge() + "." + rule.getUnit().replace( '.', '$' );
                    return (Class<? extends RuleUnit>) Class.forName( unitClassName, true, getRootClassLoader() );
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException( e );
                }
            } );
        }
    }

    public NodeFactory getNodeFactory() {
        return kieComponentFactory.getNodeFactoryService();
    }
}
