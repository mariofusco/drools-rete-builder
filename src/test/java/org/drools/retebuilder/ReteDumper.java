package org.drools.retebuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.drools.core.common.BaseNode;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.LeftTupleSource;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.Rete;
import org.drools.core.reteoo.Sink;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.KieSession;

public class ReteDumper {

    private ReteDumper() { }

    public static void dumpRete(KieBase kbase ) {
        dumpRete((InternalKnowledgeBase) kbase);
    }

    public static void dumpRete(KieRuntime session ) {
        dumpRete((InternalKnowledgeBase)session.getKieBase());
    }

    public static void dumpRete(KieSession session) {
        dumpRete((InternalKnowledgeBase)session.getKieBase());
    }

    public static void dumpRete(InternalKnowledgeBase kBase) {
        dumpRete(kBase.getRete());
    }

    public static void dumpRete(Rete rete) {
        for (EntryPointNode entryPointNode : rete.getEntryPointNodes().values()) {
            dumpNode( entryPointNode, "", new HashSet<BaseNode>() );
        }
    }

    private static void dumpNode(BaseNode node, String ident, Set<BaseNode> visitedNodes ) {
        System.out.println(ident + node);
        if (!visitedNodes.add( node )) {
            return;
        }
        Sink[] sinks = getSinks( node );
        if (sinks != null) {
            for (Sink sink : sinks) {
                if (sink instanceof BaseNode) {
                    dumpNode((BaseNode)sink, ident + "    ", visitedNodes);
                }
            }
        }
    }

    public static Sink[] getSinks( BaseNode node ) {
        Sink[] sinks = null;
        if (node instanceof EntryPointNode ) {
            EntryPointNode source = (EntryPointNode) node;
            Collection<ObjectTypeNode> otns = source.getObjectTypeNodes().values();
            sinks = otns.toArray(new Sink[otns.size()]);
        } else if (node instanceof ObjectSource ) {
            ObjectSource source = (ObjectSource) node;
            sinks = source.getObjectSinkPropagator().getSinks();
        } else if (node instanceof LeftTupleSource ) {
            LeftTupleSource source = (LeftTupleSource) node;
            sinks = source.getSinkPropagator().getSinks();
        }
        return sinks;
    }
}
