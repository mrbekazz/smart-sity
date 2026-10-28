package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Graph {
    public static final class Edge {
        public final int from;
        public final int to;
        public final int weight;

        public Edge(int from, int to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    private final int n;
    private final boolean directed;
    private final List<Edge> edges;
    private final List<List<Edge>> adj;

    public Graph(int n, boolean directed) {
        if (n < 0) {
            throw new IllegalArgumentException("Number of vertices cannot be negative");
        }
        this.n = n;
        this.directed = directed;
        this.edges = new ArrayList<>();
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        Edge e = new Edge(from, to, weight);
        edges.add(e);
        adj.get(from).add(e);
        if (!directed) {
            adj.get(to).add(new Edge(to, from, weight));
        }
    }

    public int size() {
        return n;
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    public List<Edge> adj(int v) {
        checkVertex(v);
        return Collections.unmodifiableList(adj.get(v));
    }

    public Graph reverse() {
        if (!directed) {
            throw new IllegalStateException("Only directed graphs can be reversed");
        }
        Graph r = new Graph(n, true);
        for (Edge e : edges) {
            r.addEdge(e.to, e.from, e.weight);
        }
        return r;
    }

    private void checkVertex(int v) {
        if (v < 0 || v >= n) {
            throw new IndexOutOfBoundsException("Invalid vertex: " + v);
        }
    }
}
