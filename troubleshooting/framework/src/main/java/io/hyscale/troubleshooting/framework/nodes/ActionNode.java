package io.hyscale.troubleshooting.framework.nodes;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.troubleshooting.framework.errors.TroubleshootingErrorCodes;
import io.hyscale.troubleshooting.framework.models.Action;
import io.hyscale.troubleshooting.framework.models.NodeContext;

/**
 * This class represents the action / process that needs to be done in the flowchart's
 * workflow. The action can be something like log message, publish an event etc.
 * This node might not always be connected with nodes. The workflow can just terminate with this node
 * ie action nodes can be leaf node . It can have a connecting edge when
 * required to perform certain checks after executing the action.
 */

public abstract class ActionNode<R, C extends NodeContext> implements Node<R, C> {

    protected abstract Action<R, C> getAction();

    @Override
    public R process(C context) throws HyscaleException {
        Action<R, C> action = getAction();
        if (action == null) {
            throw new HyscaleException(TroubleshootingErrorCodes.ACTION_NOT_DEFINED);
        }
        return action.act(context);
    }

    @Override
    public NodeType getType() {
        return NodeType.ACTION;
    }
}