package org.drools.retebuilder.adapters;

import org.drools.core.base.ClassObjectType;
import org.drools.core.rule.From;
import org.drools.core.rule.Pattern;
import org.drools.core.spi.DataProvider;
import org.drools.model.InvokerPattern;

public class FromAdapter extends From {

    public FromAdapter() { }

    public FromAdapter(DataProvider dataProvider, InvokerPattern pattern) {
        super(dataProvider);
        setResultPattern(new ResultPattern(pattern));
    }

    private static class ResultPattern extends Pattern {

        private ResultPattern(InvokerPattern pattern) {
            super(0, new ClassObjectType(pattern.getPatternVariable().getType().asClass()));
        }
    }
}
