package connectivity;

import graph.Graph;
import graph.Graph.Edge;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

public final class Conn {
    public static final class TopoResult {
        public final List<Integer> order;
        public final boolean acyclic;
        public final long edgeScans;

        private TopoResult(List<Integer> order, boolean acyclic, long edgeScans) {
            this.order = List.copyOf(order);
            this.acyclic = acyclic;
            this.edgeScans = edgeScans;
        }
    }

    public static final class SCCResult {
        public final List<List<Integer>> groups;
        public final long edgeScans;

        private SCCResult(List<List<Integer>> groups, long edgeScans) {
            List<List<Integer>> copy = new ArrayList<>();
            for (List<Integer> group : groups) {
                copy.add(List.copyOf(group));
            }
            this.groups = List.copyOf(copy);
            this.edgeScans = edgeScans;
        }
    }

    private Conn() {
    }

    public static TopoResult topo(Graph g) {
        requireDirected(g);
        int n = g.size();
        int[] indegree = new int[n];
        long scans = 0;
        for (Edge e : g.edges()) {
            indegree[e.to]++;
            scans++;
        }
        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (indegree[i] == 0) {
                q.offer(i);
            }
        }
        List<Integer> order = new ArrayList<>(n);
        while (!q.isEmpty()) {
            int u = q.poll();
            order.add(u);
            for (Edge e : g.adj(u)) {
                scans++;
                indegree[e.to]--;
                if (indegree[e.to] == 0) {
                    q.offer(e.to);
                }
            }
        }
        return new TopoResult(order, order.size() == n, scans);
    }

    public static SCCResult scc(Graph g) {
        requireDirected(g);
        int n = g.size();
        boolean[] seen = new boolean[n];
        List<Integer> order = new ArrayList<>(n);
        long[] scans = {0};
        for (int i = 0; i < n; i++) {
            if (!seen[i]) {
                dfs1(g, i, seen, order, scans);
            }
        }
        Graph r = g.reverse();
        scans[0] += g.edges().size();
        java.util.Arrays.fill(seen, false);
        Collections.reverse(order);
        List<List<Integer>> groups = new ArrayList<>();
        for (int v : order) {
            if (!seen[v]) {
                List<Integer> group = new ArrayList<>();
                dfs2(r, v, seen, group, scans);
                groups.add(group);
            }
        }
        return new SCCResult(groups, scans[0]);
    }

    private static void dfs1(Graph g, int u, boolean[] seen, List<Integer> order, long[] scans) {
        seen[u] = true;
        for (Edge e : g.adj(u)) {
            scans[0]++;
            if (!seen[e.to]) {
                dfs1(g, e.to, seen, order, scans);
            }
        }
        order.add(u);
    }

    private static void dfs2(Graph g, int u, boolean[] seen, List<Integer> group, long[] scans) {
        seen[u] = true;
        group.add(u);
        for (Edge e : g.adj(u)) {
            scans[0]++;
            if (!seen[e.to]) {
                dfs2(g, e.to, seen, group, scans);
            }
        }
    }

    private static void requireDirected(Graph g) {
        if (!g.isDirected()) {
            throw new IllegalArgumentException("This operation requires a directed graph");
        }
    }
}
