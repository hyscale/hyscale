package io.hyscale.troubleshooting.framework.models;

import org.jgrapht.graph.DefaultEdge;

public class DirectedEdge extends DefaultEdge {

    private boolean value;

    public DirectedEdge(boolean value) {
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }

    @Override
    protected Object getSource() {
        return super.getSource();
    }

    @Override
    protected Object getTarget() {
        return super.getTarget();
    }
}
