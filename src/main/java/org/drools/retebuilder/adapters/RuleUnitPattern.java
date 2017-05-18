/*
 * Copyright 2017 JBoss Inc
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

package org.drools.retebuilder.adapters;

import org.drools.core.ruleunit.RuleUnitUtil;
import org.drools.model.Constraint;
import org.drools.model.DataSourceDefinition;
import org.drools.model.Pattern;
import org.drools.model.Variable;
import org.drools.model.patterns.AbstractSinglePattern;
import org.kie.api.runtime.rule.RuleUnit;

import static org.drools.model.DSL.type;
import static org.drools.model.DSL.variableOf;

public class RuleUnitPattern extends AbstractSinglePattern implements Pattern<RuleUnit> {

    public static final RuleUnitPattern INSTANCE = new RuleUnitPattern();

    private RuleUnitPattern() { }

    private final DataSourceDefinition dataSourceDefinition = new DataSourceDefinition() {
        @Override
        public String getName() {
            return RuleUnitUtil.RULE_UNIT_ENTRY_POINT;
        }

        @Override
        public boolean isObservable() {
            return false;
        }
    };

    private final Variable<RuleUnit> patternVariable = variableOf( type(RuleUnit.class) );

    @Override
    public DataSourceDefinition getDataSourceDefinition() {
        return dataSourceDefinition;
    }

    @Override
    public Variable<RuleUnit> getPatternVariable() {
        return patternVariable;
    }

    @Override
    public Variable[] getBoundVariables() {
        return new Variable[] { patternVariable };
    }

    @Override
    public Variable[] getInputVariables() {
        throw new UnsupportedOperationException( "org.drools.retebuilder.adapters.RuleUnitPattern.getInputVariables -> TODO" );
    }

    @Override
    public Constraint getConstraint() {
        return Constraint.EMPTY;
    }
}
