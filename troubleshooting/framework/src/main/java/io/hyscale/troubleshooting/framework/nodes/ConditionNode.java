package io.hyscale.troubleshooting.framework.nodes;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.troubleshooting.framework.errors.TroubleshootingErrorCodes;
import io.hyscale.troubleshooting.framework.models.Condition;
import io.hyscale.troubleshooting.framework.models.NodeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the decision maker node.
 * The condition node always has two outbound edges on 'yes' &
 * 'no' condition. The output of the conditional node always emits
 * a boolean value.
 * <p>
 * The process() executes the condition defined in the node.
 */

public abstract class ConditionNode<C extends NodeContext> implements Node<Boolean, C> {

    private static final Logger logger = LoggerFactory.getLogger(ConditionNode.class);

    protected abstract Condition getCondition();

    @Override
    public Boolean process(C context) throws HyscaleException {
        Condition condition = getCondition();
        if (condition == null || condition.getPredicate() == null) {
            logger.error("Condition is undefined for the {}", this.getClass());
            throw new HyscaleException(TroubleshootingErrorCodes.CONDITION_NOT_DEFINED);
        }
        return condition.getPredicate().test(context);
    }

    @Override
    public NodeType getType() {
        return NodeType.CONDITION;
    }

}