/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.retebuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.core.base.ClassObjectType;
import org.drools.core.base.EvaluatorWrapper;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.rule.Declaration;
import org.drools.core.rule.GroupElement;
import org.drools.core.rule.Pattern;
import org.drools.core.rule.RuleConditionElement;
import org.drools.core.rule.constraint.ConditionAnalyzer;
import org.drools.core.rule.constraint.ConditionAnalyzer.SingleCondition;
import org.drools.core.rule.constraint.MvelConstraint;
import org.drools.core.spi.Constraint;
import org.kie.api.builder.KieBuilder;
import org.kie.api.definition.KiePackage;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExecutableStatement;
import org.mvel2.integration.impl.MapVariableResolverFactory;

public class ModelGenerator {

    public static void generateModel( KieBuilder kieBuilder ) {
        Collection<KiePackage> pkgs = ( (MemoryKieModule) kieBuilder.getKieModule() ).getKnowledgePackagesForKieBase( "kbase" );
        for ( KiePackage pkg : pkgs ) {
            for ( org.kie.api.definition.rule.Rule rule : pkg.getRules() ) {
                RuleContext context = new RuleContext();
                GroupElement lhs = ( (RuleImpl) rule ).getLhs();
                visit(context, lhs);
            }
        }
    }

    private static void visit( RuleContext context, GroupElement element ) {
        switch (element.getType()) {
            case AND:
                element.getChildren().forEach( elem -> visit(context, elem) );
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static void visit(RuleContext context, RuleConditionElement conditionElement) {
        if (conditionElement instanceof Pattern) {
            visit( context, (Pattern) conditionElement );
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static void visit(RuleContext context, Pattern pattern) {
        Class<?> patternType = ( (ClassObjectType) pattern.getObjectType() ).getClassType();
        if (pattern.getDeclaration() != null) {
            context.declarations.put( pattern.getDeclaration().getBindingName(), patternType );
        }
        for (Constraint constraint : pattern.getConstraints()) {
            MvelConstraint mvelConstraint = ( (MvelConstraint) constraint );

            ParserContext parserContext = new ParserContext();
            parserContext.setStrictTypeEnforcement(true);
            parserContext.setStrongTyping(true);
            parserContext.addInput("this", patternType);

            Map<String, Object> variables = new HashMap<>();
            for (Declaration decl : mvelConstraint.getRequiredDeclarations()) {
                parserContext.addInput(decl.getBindingName(), decl.getDeclarationClass());
                try {
                    variables.put(decl.getBindingName(), decl.getDeclarationClass().newInstance());
                } catch (Exception e) {
                    throw new RuntimeException( e );
                }
            }

            ExecutableStatement statement = (ExecutableStatement)MVEL.compileExpression( mvelConstraint.getExpression(), parserContext );
            try {
                statement.getValue( patternType.newInstance(), new MapVariableResolverFactory( variables ) );
            } catch (Exception e) {
                throw new RuntimeException( e );
            }
            ConditionAnalyzer.Condition condition = new ConditionAnalyzer( statement, mvelConstraint.getRequiredDeclarations(), new EvaluatorWrapper[0], patternType.getCanonicalName()).analyzeCondition();
            System.out.println(condition);
            context.expressions.put( pattern.getDeclaration().getBindingName(), conditionToLambda(condition) );
        }
    }

    private static String conditionToLambda(ConditionAnalyzer.Condition condition) {
        SingleCondition singleCondition = ( (SingleCondition) condition );
        singleCondition.getLeft();
        return "_1 -> _1.getName().equals(\"Mark\")";
    }

    public static class RuleContext {
        Map<String, Class<?>> declarations = new HashMap<>();
        Map<String, String> expressions = new HashMap<>();
    }
}