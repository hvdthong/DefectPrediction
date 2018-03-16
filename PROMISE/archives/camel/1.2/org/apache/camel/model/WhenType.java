package org.apache.camel.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.Predicate;
import org.apache.camel.impl.RouteContext;
import org.apache.camel.model.language.ExpressionType;
import org.apache.camel.processor.FilterProcessor;

/**
 * @version $Revision: 1.1 $
 */
@XmlRootElement(name = "when")
public class WhenType<Type extends ProcessorType> extends ExpressionNode {
    public WhenType() {
    }

    public WhenType(Predicate predicate) {
        super(predicate);
    }

    public WhenType(ExpressionType expression) {
        super(expression);
    }

    @Override
    public String toString() {
        return "When[ " + getExpression() + " -> " + getOutputs() + "]";
    }

    @Override
    public FilterProcessor createProcessor(RouteContext routeContext) throws Exception {
        return createFilterProcessor(routeContext);
    }
}