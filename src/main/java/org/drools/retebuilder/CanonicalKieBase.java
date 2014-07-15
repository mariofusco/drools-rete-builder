package org.drools.retebuilder;

import org.drools.core.RuleBaseConfiguration;
import org.drools.core.common.RuleBasePartitionId;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.KieComponentFactory;
import org.drools.core.reteoo.Rete;
import org.drools.core.reteoo.ReteooBuilder;
import org.drools.core.reteoo.builder.NodeFactory;
import org.drools.core.rule.EntryPointId;
import org.drools.model.Rule;
import org.drools.retebuilder.adapters.ReteooBuilderAdapter;
import org.kie.internal.KnowledgeBaseFactory;

import java.util.UUID;

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
    }

    @Override
    public Rete getRete() {
        return this.rete;
    }

    @Override
    public int getNodeCount() {
        return this.reteBuilder.getIdGenerator().getLastId() + 1;
    }

    public ReteooBuilder getReteooBuilder() {
        return this.reteooBuilder;
    }

    public void addRules(Rule... rules) {
        for (Rule rule : rules) {
            reteBuilder.addRule(rule);
        }
    }

    public NodeFactory getNodeFactory() {
        return kieComponentFactory.getNodeFactoryService();
    }
}
