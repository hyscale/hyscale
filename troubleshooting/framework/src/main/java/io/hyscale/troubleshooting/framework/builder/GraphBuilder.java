package io.hyscale.troubleshooting.framework.builder;

import io.hyscale.troubleshooting.framework.models.DirectedEdge;
import io.hyscale.troubleshooting.framework.nodes.Node;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedPseudograph;

public class GraphBuilder {

    private Graph<Node, DirectedEdge> graph;

    public GraphBuilder() {
        graph = new DirectedWeightedPseudograph<Node, DirectedEdge>(DirectedEdge.class);
    }

    public boolean registerNode(Node node) {
        return graph.addVertex(node);
    }

    public boolean registerEdge(Node source, Node target, DirectedEdge edge) {
        return graph.addEdge(source, target, edge);
    }

    public Graph<Node, DirectedEdge> build() {
        return graph;
    }


}
